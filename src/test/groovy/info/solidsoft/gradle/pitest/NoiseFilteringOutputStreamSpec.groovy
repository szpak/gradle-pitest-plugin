/* Copyright (c) 2012 Marcin Zajączkowski
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
package info.solidsoft.gradle.pitest

import groovy.transform.CompileDynamic
import org.gradle.api.logging.Logger
import spock.lang.Specification
import spock.lang.Unroll

@CompileDynamic
class NoiseFilteringOutputStreamSpec extends Specification {

    ByteArrayOutputStream delegate = new ByteArrayOutputStream()
    Logger logger = Mock()
    OutputStream noiseFilteringOutputStream = new PitestTask.NoiseFilteringOutputStream(delegate, logger)

    @Unroll
    void "should redirect noisy line '#line' to the logger at info level instead of the delegate stream"() {
        when:
            writeLine(line)
        then:
            1 * logger.info(line)
        and:
            delegate.toString() == ""
        where:
            line << [
                "1:03:03 PM PIT >> INFO : Overriding configured number of threads (1) to be 8",
                "1:03:04 PM PIT >> INFO : Verbose logging is disabled. If you encounter a problem, please enable it before reporting an issue.",
                "1:03:05 PM PIT >> INFO : MINION : Mockito is currently self-attaching to enable the inline-mock-maker.",
            ]
    }

    void "should pass a non-noisy line through to the delegate stream unchanged"() {
        given:
            String line = "================================================================================"
        when:
            writeLine(line)
        then:
            0 * logger.info(_)
        and:
            delegate.toString() == line + "\n"
    }

    void "should pass the mutation report summary through to the delegate stream by default"() {
        given:
            String line = ">> Generated 517 mutations Killed 456 (88%)"
        when:
            writeLine(line)
        then:
            0 * logger.info(_)
        and:
            delegate.toString() == line + "\n"
    }

    void "should flush a pending, not yet newline-terminated line on close without closing the delegate"() {
        given:
            noiseFilteringOutputStream.write("no trailing newline".bytes)
        when:
            noiseFilteringOutputStream.close()
        then:
            delegate.toString() == "no trailing newline\n"
        and: "the delegate is not closed, as it may be the shared System.out/System.err stream"
            delegate.write('still usable'.bytes)   //would throw if the delegate had been closed
    }

    private void writeLine(String line) {
        noiseFilteringOutputStream.write(line.bytes)
        noiseFilteringOutputStream.write('\n' as char as int)
    }

}
