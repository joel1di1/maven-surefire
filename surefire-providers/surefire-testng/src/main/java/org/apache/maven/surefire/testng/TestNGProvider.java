package org.apache.maven.surefire.testng;

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

import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.providerapi.SurefireProvider;
import org.apache.maven.surefire.report.ReporterConfiguration;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.report.ReporterFactory;
import org.apache.maven.surefire.report.ReporterManagerFactory;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.DirectoryScannerParameters;
import org.apache.maven.surefire.testset.TestArtifactInfo;
import org.apache.maven.surefire.testset.TestRequest;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.apache.maven.surefire.util.DirectoryScanner;
import org.apache.maven.surefire.util.NestedRuntimeException;
import org.apache.maven.surefire.util.TestsToRun;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

/**
 * @author Kristian Rosenvold
 * @noinspection UnusedDeclaration
 */
public class TestNGProvider
    implements SurefireProvider
{
    private final Properties providerProperties;

    private final TestArtifactInfo testArtifactInfo;

    private final ReporterConfiguration reporterConfiguration;

    private final ReporterFactory reporterFactory;

    private final ClassLoader testClassLoader;

    private final DirectoryScannerParameters directoryScannerParameters;

    private final DirectoryScanner directoryScanner;

    private final TestRequest testRequest;

    private TestsToRun testsToRun;

    private final File basedir;

    public TestNGProvider( ProviderParameters booterParameters )
    {
        this.reporterFactory = booterParameters.getReporterFactory();
        this.testClassLoader = booterParameters.getTestClassLoader();
        this.directoryScannerParameters = booterParameters.getDirectoryScannerParameters();
        this.providerProperties = booterParameters.getProviderProperties();
        this.testRequest = booterParameters.getTestRequest();
        basedir = directoryScannerParameters != null ? directoryScannerParameters.getTestClassesDirectory() : null;
        testArtifactInfo = booterParameters.getTestArtifactInfo();
        reporterConfiguration = booterParameters.getReporterConfiguration();
        this.directoryScanner = booterParameters.getDirectoryScanner();
    }

    public Boolean isRunnable()
    {
        return Boolean.TRUE;
    }

    public RunResult invoke( Object forkTestSet )
        throws TestSetFailedException, ReporterException
    {

        if ( isTestNGXmlTestSuite( testRequest ) )
        {
            TestNGXmlTestSuite testNGXmlTestSuite = getXmlSuite();
            testNGXmlTestSuite.locateTestSets( testClassLoader );
            if ( forkTestSet != null && testRequest == null )
            {
                testNGXmlTestSuite.execute( (String) forkTestSet, (ReporterManagerFactory) reporterFactory,
                                            testClassLoader );
            }
            else
            {
                testNGXmlTestSuite.execute( (ReporterManagerFactory) reporterFactory );
            }
        }
        else
        {
            if ( testsToRun == null )
            {
                testsToRun = forkTestSet == null ? scanClassPath() : TestsToRun.fromClass( (Class) forkTestSet );
            }
            TestNGDirectoryTestSuite suite = getDirectorySuite();
            suite.execute( testsToRun, reporterFactory );
        }

        return reporterFactory.close();
    }

    boolean isTestNGXmlTestSuite( TestRequest testSuiteDefinition )
    {
        return testSuiteDefinition.getSuiteXmlFiles() != null && testSuiteDefinition.getSuiteXmlFiles().size() > 0
            && testSuiteDefinition.getRequestedTest() == null;

    }


    private TestNGDirectoryTestSuite getDirectorySuite()
    {
        return new TestNGDirectoryTestSuite( basedir, new ArrayList( directoryScannerParameters.getIncludes() ),
                                             new ArrayList( directoryScannerParameters.getExcludes() ),
                                             testRequest.getTestSourceDirectory().toString(),
                                             testArtifactInfo.getVersion(), providerProperties,
                                             reporterConfiguration.getReportsDirectory(), testRequest.getRequestedTestMethod() );
    }

    private TestNGXmlTestSuite getXmlSuite()
    {
        return new TestNGXmlTestSuite( testRequest.getSuiteXmlFiles(), testRequest.getTestSourceDirectory().toString(),
                                       testArtifactInfo.getVersion(), providerProperties,
                                       reporterConfiguration.getReportsDirectory() );
    }


    public Iterator getSuites()
    {
        if ( isTestNGXmlTestSuite( testRequest ) )
        {
            try
            {
                return getXmlSuite().locateTestSets( testClassLoader ).keySet().iterator();
            }
            catch ( TestSetFailedException e )
            {
                throw new NestedRuntimeException( e );
            }
        }
        else
        {
            testsToRun = scanClassPath();
            return testsToRun.iterator();
        }
    }

    private TestsToRun scanClassPath()
    {
        return directoryScanner.locateTestClasses( testClassLoader, null );
    }


}
