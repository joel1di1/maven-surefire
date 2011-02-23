package org.apache.maven.surefire.junit4;

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

import org.apache.maven.surefire.Surefire;
import org.apache.maven.surefire.common.junit4.JUnit4RunListener;
import org.apache.maven.surefire.common.junit4.JUnit4RunListenerFactory;
import org.apache.maven.surefire.common.junit4.JUnit4TestChecker;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.providerapi.SurefireProvider;
import org.apache.maven.surefire.report.PojoStackTraceWriter;
import org.apache.maven.surefire.report.ReportEntry;
import org.apache.maven.surefire.report.Reporter;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.report.ReporterFactory;
import org.apache.maven.surefire.report.SimpleReportEntry;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.apache.maven.surefire.util.DefaultDirectoryScanner;
import org.apache.maven.surefire.util.DirectoryScanner;
import org.apache.maven.surefire.util.TestsToRun;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;


/**
 * @author Kristian Rosenvold
 */
@SuppressWarnings( { "UnusedDeclaration" } )
public class JUnit4Provider
    implements SurefireProvider
{

    private static ResourceBundle bundle = ResourceBundle.getBundle( Surefire.SUREFIRE_BUNDLE_NAME );

    private final ReporterFactory reporterFactory;

    private final ClassLoader testClassLoader;

    private final DirectoryScanner directoryScanner;

    private final List<RunListener> customRunListeners;

    private final JUnit4TestChecker jUnit4TestChecker;
    
    private final String requestedTestMethod;

    private TestsToRun testsToRun;

    public JUnit4Provider( ProviderParameters booterParameters )
    {
        this.reporterFactory = booterParameters.getReporterFactory();
        this.testClassLoader = booterParameters.getTestClassLoader();
        this.directoryScanner = booterParameters.getDirectoryScanner();
        customRunListeners = JUnit4RunListenerFactory.
            createCustomListeners( booterParameters.getProviderProperties().getProperty( "listener" ) );
        jUnit4TestChecker = new JUnit4TestChecker( testClassLoader );
        requestedTestMethod = booterParameters.getTestRequest().getRequestedTestMethod();

    }

    @SuppressWarnings( { "UnnecessaryUnboxing" } )
    public RunResult invoke( Object forkTestSet )
        throws TestSetFailedException, ReporterException
    {
        if ( testsToRun == null )
        {
            testsToRun = forkTestSet == null ? scanClassPath() : TestsToRun.fromClass( (Class) forkTestSet );
        }

        upgradeCheck();

        Reporter reporter = reporterFactory.createReporter();
        JUnit4RunListener jUnit4TestSetReporter = new JUnit4RunListener( reporter );
        RunNotifier runNotifer = getRunNotifer( jUnit4TestSetReporter, customRunListeners );

        for ( Class clazz : testsToRun.getLocatedClasses() )
        {
            executeTestSet( clazz, reporter, testClassLoader, runNotifer );
        }

        closeRunNotifer( jUnit4TestSetReporter, customRunListeners );

        return reporterFactory.close();

    }

    private void executeTestSet( Class clazz, Reporter reporter, ClassLoader classLoader, RunNotifier listeners )
        throws ReporterException, TestSetFailedException
    {
        final ReportEntry report = new SimpleReportEntry( this.getClass().getName(), clazz.getName() );

        reporter.testSetStarting( report );

        try
        {
            JUnit4TestSet.execute( clazz, listeners, this.requestedTestMethod );
        }
        catch ( TestSetFailedException e )
        {
            throw e;
        }
        catch ( Throwable e )
        {
            reporter.testError( new SimpleReportEntry( report.getSourceName(), report.getName(),
                                                       new PojoStackTraceWriter( report.getSourceName(),
                                                                                 report.getName(), e ) ) );
        }
        finally
        {
            reporter.testSetCompleted( report );
        }
    }

    private RunNotifier getRunNotifer( RunListener main, List<RunListener> others )
    {
        RunNotifier fNotifier = new RunNotifier();
        fNotifier.addListener( main );
        for ( RunListener listener : others )
        {
            fNotifier.addListener( listener );
        }
        return fNotifier;
    }

    // I am not entierly sure as to why we do this explicit freeing, it's one of those
    // pieces of code that just seem to linger on in here ;)
    private void closeRunNotifer( RunListener main, List<RunListener> others )
    {
        RunNotifier fNotifier = new RunNotifier();
        fNotifier.removeListener( main );
        for ( RunListener listener : others )
        {
            fNotifier.removeListener( listener );
        }
    }

    public Iterator getSuites()
    {
        testsToRun = scanClassPath();
        return testsToRun.iterator();
    }

    private TestsToRun scanClassPath()
    {
        return directoryScanner.locateTestClasses( testClassLoader, jUnit4TestChecker );
    }

    private void upgradeCheck()
        throws TestSetFailedException
    {
        if ( isJunit4UpgradeCheck()
            && ( (DefaultDirectoryScanner) directoryScanner ).getClassesSkippedByValidation().size() > 0 )
        {
            StringBuilder reason = new StringBuilder();
            reason.append( "Updated check failed\n" );
            reason.append( "There are tests that would be run with junit4 / surefire 2.6 but not with [2.7,):\n" );
            //noinspection unchecked
            for ( Class testClass : (List<Class>) ( (DefaultDirectoryScanner) directoryScanner ).getClassesSkippedByValidation() )
            {
                reason.append( "   " );
                reason.append( testClass.getCanonicalName() );
                reason.append( "\n" );
            }
            throw new TestSetFailedException( reason.toString() );
        }
    }

    private boolean isJunit4UpgradeCheck()
    {
        final String property = System.getProperty( "surefire.junit4.upgradecheck" );
        return property != null;
    }

    public Boolean isRunnable()
    {
        return Boolean.TRUE;
    }
}
