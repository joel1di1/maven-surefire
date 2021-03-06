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


import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.apache.maven.reporting.MavenReportException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Verifies that the providers get the result summary at the bottom of the run correctly, in different forkmodes
 * SUREFIRE-613 Asserts proper test counts when running in parallel
 *
 * @author Kristian Rosenvold
 */
public class ResultCountingIT
    extends AbstractSurefireIntegrationTestClass
{
    public void testCountingWithJunit481ForkNever()
        throws Exception
    {
        assertForkMode( "never" );
    }

    public void testCountingWithJunit481ForkOnce()
        throws Exception
    {
        assertForkMode( "once" );
    }


    public void testCountingWithJunit481ForkAlways()
        throws Exception
    {
        assertForkMode( "always" );
    }

    private void assertForkMode( String forkMode )
        throws IOException, VerificationException, MavenReportException
    {
        File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/result-counting" );

        Verifier verifier = new Verifier( testDir.getAbsolutePath() );
        String[] opts = { "-fn" };
        verifier.setCliOptions( new ArrayList( Arrays.asList( opts ) ) );
        List goals = getGoals( forkMode );
        this.executeGoals( verifier, goals );

        verifier.resetStreams();

        HelperAssertions.assertTestSuiteResults( 36, 23, 4, 2, testDir );

        verifier.verifyTextInLog( "Tests run: 36, Failures: 4, Errors: 23, Skipped: 2" );
    }

    private List getGoals( String forkMode )
    {
        List goals = this.getInitialGoals();
        goals.add( "test" );
        goals.add( "-DforkMode=" + forkMode );
        return goals;
    }
}