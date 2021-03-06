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


/**
 * Constants used by the serializer/deserializer
 *
 * @author Kristian Rosenvold
 */
public interface BooterConstants
{
    String INCLUDES_PROPERTY_PREFIX = "includes";
    String EXCLUDES_PROPERTY_PREFIX = "excludes";
    String DIRSCANNER_PROPERTY_PREFIX = "dirscanner.";
    String DIRSCANNER_OPTIONS = "directoryScannerOptions";
    String REPORT_PROPERTY_PREFIX = "report.";
    String PARAMS_SUFIX = ".params";
    String TYPES_SUFIX = ".types";
    String CLASSPATH_URL = "classPathUrl.";
    String SUREFIRE_CLASSPATHURL = "surefireClassPathUrl.";
    String CHILD_DELEGATION = "childDelegation";
    String ENABLE_ASSERTIONS = "enableAssertions";
    String USESYSTEMCLASSLOADER = "useSystemClassLoader";
    String USEMANIFESTONLYJAR = "useManifestOnlyJar";
    String FAILIFNOTESTS = "failIfNoTests";
    String ISTRIMSTACKTRACE = "isTrimStackTrace";
    String REPORTSDIRECTORY = "reportsDirectory";
    String FORKTIMEOUT = "forkTimeout";
    String TESTARTIFACT_VERSION = "testFwJarVersion";
    String TESTARTIFACT_CLASSIFIER = "testFwJarClassifier";
    String REQUESTEDTEST = "requestedTest";
    String REQUESTEDTESTMETHOD = "requestedTestMethod";
    String SOURCE_DIRECTORY = "testSuiteDefinitionTestSourceDirectory";
    String TEST_CLASSES_DIRECTORY = "testClassesDirectory";
    String RUN_ORDER = "runOrder";
    String TEST_SUITE_XML_FILES = "testSuiteXmlFiles";
    String PROVIDER_CONFIGURATION = "providerConfiguration";
    String FORKTESTSET = "forkTestSet";
}
