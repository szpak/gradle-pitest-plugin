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

    private final ByteArrayOutputStream delegate = new ByteArrayOutputStream()
    private final Logger logger = Mock()
    private final OutputStream noiseFilteringOutputStream = new PitestTask.NoiseFilteringOutputStream(delegate, logger)

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
            ]
    }

    void "should pass a non-noisy line through to the delegate stream unchanged"() {
        given:
            String line = "Some other, unrecognized build message"
        when:
            writeLine(line)
        then:
            0 * logger.info(_)
        and:
            delegate.toString() == line + "\n"
    }

    @Unroll
    void "should pass minion output through unfiltered, in case it reports a real timeout or failure: '#line'"() {
        when:
            writeLine(line)
        then:
            0 * logger.info(_)
        and:
            delegate.toString() == line + "\n"
        where:
            line << [
                "1:03:05 PM PIT >> INFO : MINION : Mockito is currently self-attaching to enable the inline-mock-maker.",
                "1:03:05 PM PIT >> INFO : MINION : Test timed out after 4000 ms",
                "1:03:05 PM PIT >> INFO : MINION : java.lang.AssertionError: expected <5> but was <4>",
            ]
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

    void "should hide the Mutators and Timings sections (including their headers) but keep Statistics and anything after it visible"() {
        given:
            List<String> loggedInfoLines = []
            logger.info(_) >> { String l -> loggedInfoLines << l }
        and:
            List<String> reportLines = [
                "================================================================================",
                "- Mutators",
                "================================================================================",
                "> org.pitest.mutationtest.engine.gregor.mutators.returns.PrimitiveReturnsMutator",
                ">> Generated 16 Killed 12 (75%)",
                "> KILLED 12 SURVIVED 4 TIMED_OUT 0 NON_VIABLE 0",
                "> MEMORY_ERROR 0 NOT_STARTED 0 STARTED 0 RUN_ERROR 0",
                "> NO_COVERAGE 0",
                "--------------------------------------------------------------------------------",
                "================================================================================",
                "- Timings",
                "================================================================================",
                "> pre-scan for mutations : < 1 second",
                "--------------------------------------------------------------------------------",
                "================================================================================",
                "- Statistics",
                "================================================================================",
                ">> Line Coverage (for mutated classes only): 1191/1290 (92%)",
                "Enhanced functionality available at https://www.arcmutate.com/",
            ]
        when:
            reportLines.each { String line -> writeLine(line) }
        then:
            delegate.toString() == ([
                "================================================================================",
                "- Statistics",
                "================================================================================",
                ">> Line Coverage (for mutated classes only): 1191/1290 (92%)",
                "Enhanced functionality available at https://www.arcmutate.com/",
            ].join("\n") + "\n")
        and: "everything from Mutators and Timings (including their headers) was demoted to info instead of dropped"
            //Note: reportLines minus the visible tail, NOT via List.minus() - it removes every occurrence of a
            //matched element (there are several identical "====" separator lines), not just the intended ones.
            loggedInfoLines == reportLines[0..<(reportLines.size() - 5)]
    }

    void "should keep a mutator block visible when it reports a real RUN_ERROR or MEMORY_ERROR"() {
        given:
            List<String> loggedInfoLines = []
            logger.info(_) >> { String l -> loggedInfoLines << l }
        and:
            List<String> boringBlock = [
                "> org.pitest.mutationtest.engine.gregor.mutators.returns.PrimitiveReturnsMutator",
                ">> Generated 16 Killed 12 (75%)",
                "> KILLED 12 SURVIVED 4 TIMED_OUT 0 NON_VIABLE 0",
                "> MEMORY_ERROR 0 NOT_STARTED 0 STARTED 0 RUN_ERROR 0",
                "> NO_COVERAGE 0",
                "--------------------------------------------------------------------------------",
            ]
            List<String> blockWithRunError = [
                "> org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator",
                ">> Generated 94 Killed 83 (88%)",
                "> KILLED 83 SURVIVED 8 TIMED_OUT 0 NON_VIABLE 0",
                "> MEMORY_ERROR 0 NOT_STARTED 0 STARTED 0 RUN_ERROR 2",
                "> NO_COVERAGE 3",
                "--------------------------------------------------------------------------------",
            ]
        and:
            List<String> mutatorsHeader = ["================================================================================", "- Mutators",
                                            "================================================================================"]
        when: "entering the Mutators section, then writing a boring block and one reporting a real error"
            mutatorsHeader.each { String line -> writeLine(line) }
            (boringBlock + blockWithRunError).each { String line -> writeLine(line) }
        then:
            delegate.toString() == blockWithRunError.join("\n") + "\n"
        and: "the header and the boring block were demoted to info in full - not checked via list membership, " +
            "since the boring block's delimiter line is textually identical to the error block's"
            loggedInfoLines == mutatorsHeader + boringBlock
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
        noiseFilteringOutputStream.write(('\n' as char) as int)
    }

}
