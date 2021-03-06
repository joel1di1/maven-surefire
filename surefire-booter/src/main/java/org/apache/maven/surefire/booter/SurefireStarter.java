package org.apache.maven.surefire.booter;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.surefire.providerapi.SurefireProvider;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.apache.maven.surefire.util.NestedRuntimeException;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

/**
 * Invokes surefire with the correct classloader setup.
 * <p/>
 * This part of the booter is always guaranteed to be in the
 * same vm as the tests will be run in.
 *
 * @author Jason van Zyl
 * @author Brett Porter
 * @author Emmanuel Venisse
 * @author Dan Fabulich
 * @author Kristian Rosenvold
 * @version $Id$
 */
public class SurefireStarter
{
    private static final int NO_TESTS = 254;

    private final ProviderConfiguration providerConfiguration;

    private final StartupConfiguration startupConfiguration;

    private final String SUREFIRE_TEST_CLASSPATH = "surefire.test.class.path";

    public SurefireStarter( StartupConfiguration startupConfiguration, ProviderConfiguration providerConfiguration )
    {
        this.providerConfiguration = providerConfiguration;
        this.startupConfiguration = startupConfiguration;
    }

    public int runSuitesInProcess( Object testSet, File surefirePropertiesFile, Properties p )
        throws SurefireExecutionException, IOException
    {
        writeSurefireTestClasspathProperty();
        final ClasspathConfiguration classpathConfiguration = startupConfiguration.getClasspathConfiguration();

        ClassLoader testsClassLoader = classpathConfiguration.createTestClassLoaderConditionallySystem(
            startupConfiguration.useSystemClassLoader() );

        ClassLoader surefireClassLoader = classpathConfiguration.createSurefireClassLoader( testsClassLoader );

        final RunResult runResult = invokeProvider( testSet, testsClassLoader, surefireClassLoader );
        updateResultsProperties( runResult, p );
        SystemPropertyManager.writePropertiesFile( surefirePropertiesFile, "surefire", p );
        return processRunCount( runResult );
    }

    public int runSuitesInProcess()
        throws SurefireExecutionException
    {
        // The test classloader must be constructed first to avoid issues with commons-logging until we properly
        // separate the TestNG classloader
        ClassLoader testsClassLoader = createInProcessTestClassLoader();

        final ClasspathConfiguration classpathConfiguration = startupConfiguration.getClasspathConfiguration();

        ClassLoader surefireClassLoader = classpathConfiguration.createSurefireClassLoader( testsClassLoader );

        final RunResult runResult = invokeProvider( null, testsClassLoader, surefireClassLoader );
        return processRunCount( runResult);
    }

    private ClassLoader createInProcessTestClassLoader()
        throws SurefireExecutionException
    {
        writeSurefireTestClasspathProperty();
        ClasspathConfiguration classpathConfiguration = startupConfiguration.getClasspathConfiguration();
        if ( startupConfiguration.isManifestOnlyJarRequestedAndUsable() )
        {
            ClassLoader testsClassLoader = getClass().getClassLoader(); // ClassLoader.getSystemClassLoader()
            // SUREFIRE-459, trick the app under test into thinking its classpath was conventional
            // (instead of a single manifest-only jar)
            System.setProperty( "surefire.real.class.path", System.getProperty( "java.class.path" ) );
            classpathConfiguration.getTestClasspath().writeToSystemProperty( "java.class.path" );
            return testsClassLoader;
        }
        else
        {
            return classpathConfiguration.createTestClassLoader();
        }
    }

    private void writeSurefireTestClasspathProperty()
    {
        ClasspathConfiguration classpathConfiguration = startupConfiguration.getClasspathConfiguration();
        classpathConfiguration.getTestClasspath().writeToSystemProperty( SUREFIRE_TEST_CLASSPATH );
    }

    private static final String RESULTS_ERRORS = "errors";

    private static final String RESULTS_COMPLETED_COUNT = "completedCount";

    private static final String RESULTS_FAILURES = "failures";

    private static final String RESULTS_SKIPPED = "skipped";


    private void updateResultsProperties( RunResult runResult, Properties results )
    {
        results.setProperty( RESULTS_ERRORS, String.valueOf( runResult.getErrors() ) );
        results.setProperty( RESULTS_COMPLETED_COUNT, String.valueOf( runResult.getCompletedCount() ) );
        results.setProperty( RESULTS_FAILURES, String.valueOf( runResult.getFailures() ) );
        results.setProperty( RESULTS_SKIPPED, String.valueOf( runResult.getSkipped() ) );
    }

    private RunResult invokeProvider( Object testSet, ClassLoader testsClassLoader, ClassLoader surefireClassLoader )
    {
        final PrintStream orgSystemOut = System.out;
        final PrintStream orgSystemErr = System.err;
        // Note that System.out/System.err are also read in the "ReporterConfiguration" instatiation
        // in createProvider below. These are the same values as here.
        ProviderFactory providerFactory =
            new ProviderFactory( startupConfiguration, providerConfiguration, surefireClassLoader, testsClassLoader );
        final SurefireProvider provider = providerFactory.createProvider( );

        try
        {
            return provider.invoke( testSet );
        }
        catch ( TestSetFailedException e )
        {
            throw new NestedRuntimeException( e );
        }
        catch ( ReporterException e )
        {
            throw new NestedRuntimeException( e );
        }
        finally
        {
            System.setOut( orgSystemOut );
            System.setErr( orgSystemErr );
        }
    }

    /**
     * Returns the process return code based on the RunResult
     *
     * @param runCount The run result
     * @return The process result code
     * @throws SurefireExecutionException When an exception is found
     */
    private int processRunCount( RunResult runCount )
        throws SurefireExecutionException
    {

        if ( runCount.getCompletedCount() == 0 && providerConfiguration.isFailIfNoTests().booleanValue() )
        {
            return NO_TESTS;
        }

        return runCount.getBooterCode();
    }
}
