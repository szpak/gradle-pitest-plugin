/* Copyright (c) 2014 Marcin Zajączkowski
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.droidsonroids.gradle.pitest

class PitestPluginTypesConversionTest extends BasicProjectBuilderSpec implements WithPitestTaskInitialization {

    def "accept BigDecimal as timeoutFactor configuration parameter"() {
        given:
            project.pitest.timeoutFactor = 1.23
        expect:
            task.timeoutFactor == 1.23
    }

    def "accept String as timeoutFactor configuration parameter"() {
        given:
            project.pitest.timeoutFactor = "1.23"
        expect:
            task.timeoutFactor == 1.23
    }
}
