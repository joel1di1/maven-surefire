package org.apache.maven.surefire.booter;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor lice
nse agreements.  See the NOTICE file
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

import org.apache.maven.surefire.report.ReporterConfiguration;
import org.apache.maven.surefire.testset.DirectoryScannerParameters;
import org.apache.maven.surefire.testset.TestArtifactInfo;
import org.apache.maven.surefire.testset.TestRequest;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import junit.framework.TestCase;
import org.apache.maven.surefire.util.NestedRuntimeException;

/**
 * @author Kristian Rosenvold
 */
public class SurefireReflectorTest
    extends TestCase
{
    public void testSetDirectoryScannerParameters()
        throws Exception
    {
        SurefireReflector surefireReflector = getReflector();
        Object foo = getFoo();

        DirectoryScannerParameters directoryScannerParameters =
            new DirectoryScannerParameters( new File( "ABC" ), new ArrayList(), new ArrayList(), Boolean.FALSE,
                                            "hourly" );
        surefireReflector.setDirectoryScannerParameters( foo, directoryScannerParameters );
        assertTrue( isCalled( foo ).booleanValue() );

    }

    public void testTestSuiteDefinition()
        throws Exception
    {
        SurefireReflector surefireReflector = getReflector();
        Object foo = getFoo();

        TestRequest testSuiteDefinition =
            new TestRequest( Arrays.asList( new File[]{ new File( "file1" ), new File( "file2" ) } ),
                             new File( "TestSOurce" ), "aUserRequestedTest", "aMethodRequested" );
        surefireReflector.setTestSuiteDefinition( foo, testSuiteDefinition );
        assertTrue( isCalled( foo ).booleanValue() );
    }

    public void testProviderProperties()
        throws Exception
    {
        SurefireReflector surefireReflector = getReflector();
        Object foo = getFoo();

        surefireReflector.setProviderProperties( foo, new Properties() );
        assertTrue( isCalled( foo ).booleanValue() );
    }

    public void testReporterConfiguration()
        throws Exception
    {
        SurefireReflector surefireReflector = getReflector();
        Object foo = getFoo();

        ReporterConfiguration reporterConfiguration = getReporterConfiguration();
        surefireReflector.setReporterConfigurationAware( foo, reporterConfiguration );
        assertTrue( isCalled( foo ).booleanValue() );
    }

    private ReporterConfiguration getReporterConfiguration()
    {
        return new ReporterConfiguration( new ArrayList(), new File( "CDE" ), Boolean.TRUE, null );
    }

    public void testTestClassLoaderAware()
        throws Exception
    {
        SurefireReflector surefireReflector = getReflector();
        Object foo = getFoo();

        surefireReflector.setTestClassLoader( foo, getClass().getClassLoader(), getClass().getClassLoader() );
        assertTrue( isCalled( foo ).booleanValue() );
    }

    public void testArtifactInfoAware()
        throws Exception
    {
        SurefireReflector surefireReflector = getReflector();
        Object foo = getFoo();

        TestArtifactInfo testArtifactInfo = new TestArtifactInfo( "12.3", "test" );
        surefireReflector.setTestArtifactInfo( foo, testArtifactInfo );
        assertTrue( isCalled( foo ).booleanValue() );
    }

    private SurefireReflector getReflector()
    {
        return new SurefireReflector( this.getClass().getClassLoader() );
    }

    public Object getFoo()
    { // Todo: Setup a different classloader so we can really test crossing
        return new Foo();
    }


    private Boolean isCalled( Object foo )
    {
        final Method isCalled;
        try
        {
            isCalled = foo.getClass().getMethod( "isCalled", new Class[0] );
            return (Boolean) isCalled.invoke( foo, new Object[0] );
        }
        catch ( IllegalAccessException e )
        {
            throw new NestedRuntimeException( e );
        }
        catch ( InvocationTargetException e )
        {
            throw new NestedRuntimeException( e );
        }
        catch ( NoSuchMethodException e )
        {
            throw new NestedRuntimeException( e );
        }
    }


}
