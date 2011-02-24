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


import java.io.File;
import java.util.List;

/**
 * Use -Dtest to run a single TestNG test, overriding the suite XML parameter.
 *
 * @author <a href="mailto:dfabulich@apache.org">Dan Fabulich</a>
 */
public class CheckTestNgSuiteXmlSingleIT
    extends SurefireVerifierTestClass
{
    public CheckTestNgSuiteXmlSingleIT()
    {
        super( "/testng-twoTestCaseSuite" );
    }

    public void testTestNGSuite()
        throws Exception
    {
        addGoal( "-Dtest=TestNGTestTwo"  );
        executeTest();
        verifyErrorFreeLog();

        List reports = HelperAssertions.extractReports( ( new File[]{ getTestDir() } ) );
        IntegrationTestSuiteResults results = HelperAssertions.parseReportList( reports );
        HelperAssertions.assertTestSuiteResults( 1, 0, 0, 0, results );
    }

}
