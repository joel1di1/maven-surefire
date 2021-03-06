package org.apache.maven.surefire.its;

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

import junit.framework.TestCase;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.surefire.util.NestedRuntimeException;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class of all integration test cases. Mainly used to pickup surefire version
 * from system property
 *
 * @author Dan T. Tran
 */
public abstract class AbstractSurefireIntegrationTestClass
    extends TestCase
{
    private String surefireVersion = System.getProperty( "surefire.version" );

    private String testNgVersion = System.getProperty( "testng.version" );

    protected List<String> getInitialGoals()
    {
        return getInitialGoals( testNgVersion );
    }

    protected List<String> getInitialGoals( String testNgVersion )
    {
        List<String> goals = new ArrayList<String>();
        goals.add( "-Dsurefire.version=" + surefireVersion );

        if ( testNgVersion != null )
        {
            goals.add( "-DtestNgVersion=" + testNgVersion );

            ArtifactVersion v = new DefaultArtifactVersion( testNgVersion );
            try
            {
                if ( VersionRange.createFromVersionSpec( "(,5.12.1)" ).containsVersion( v ) )
                {
                    goals.add( "-DtestNgClassifier=jdk15" );
                }
            }
            catch ( InvalidVersionSpecificationException e )
            {
                throw new NestedRuntimeException( e.getMessage(), e );
            }
        }

        return goals;
    }

    protected void executeGoal( Verifier verifier, String goal )
        throws VerificationException
    {
        List<String> goals = getInitialGoals();
        goals.add( goal );
        executeGoals( verifier, goals );
    }

    @SuppressWarnings( { "unchecked" } )
    protected void executeGoals( Verifier verifier, List goals )
        throws VerificationException
    {
        if ( !verifier.getCliOptions().contains( "-s" ) )
        {
            String settingsPath = System.getProperty( "maven.settings.file" );
            if ( settingsPath.indexOf( ' ' ) >= 0 )
            {
                settingsPath = '"' + settingsPath + '"';
            }
            verifier.getCliOptions().add( "-s" );
            verifier.getCliOptions().add( settingsPath );
        }

        verifier.executeGoals( goals );
    }

    protected String getSurefireVersion()
    {
        return surefireVersion;
    }
}
