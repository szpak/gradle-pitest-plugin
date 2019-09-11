package info.solidsoft.gradle.pitest.functional

import nebula.test.functional.ExecutionResult
import org.gradle.api.GradleException
import spock.lang.Issue
import spock.lang.PendingFeature

class OverridePluginFunctionalSpec extends AbstractPitestFunctionalSpec {

    //Note: gradle-override-plugin has important limitations in support for collections
    //See: https://github.com/nebula-plugins/gradle-override-plugin/issues/1 or https://github.com/nebula-plugins/gradle-override-plugin/issues/3
    //Update 201909. Gradle 4.6 introduced built-in support for overriding (with its on limitations, but also with support for lists):
    //    https://docs.gradle.org/5.6.2/userguide/custom_tasks.html#sec:declaring_and_using_command_line_options
    def "should allow to override String configuration parameter from command line"() {
        given:
            buildFile << """
                apply plugin: 'java'
                apply plugin: 'info.solidsoft.pitest'
                apply plugin: 'nebula-override'
                group = 'gradle.pitest.test'

                buildscript {
                    repositories { mavenCentral() }
                    dependencies { classpath 'com.netflix.nebula:gradle-override-plugin:1.12.+' }
                }
                repositories { mavenCentral() }
                dependencies { testImplementation 'junit:junit:4.11' }
            """.stripIndent()
        and:
            writeHelloWorld('gradle.pitest.test.hello')
        when:
            ExecutionResult result = runTasksSuccessfully('pitest', '-Doverride.pitest.reportDir=build/treports')
        then:
            result.standardOutput.contains('Generated 1 mutations Killed 0 (0%)')
            fileExists('build/treports')
    }

    @Issue("https://github.com/szpak/gradle-pitest-plugin/issues/139")
    @PendingFeature(exceptions = GradleException, reason = "Not implemented yet due to Gradle limitations described in linked issue")
    def "should allow to define features from command line and override those from configuration"() {
        given:
            buildFile << """
                ${getBasicGradlePitestConfig()}

                pitest {
                    failWhenNoMutations = false
                    timestampedReports = true
                }
            """.stripIndent()
        when:
            ExecutionResult result = runTasksSuccessfully('pitest', '--timestampedReports=false',
                '--features=+EXPORT', '--features=-FINFINC')
        then:
            result.standardOutput.contains("--timestampedReports=false")
        and:
            result.standardOutput.contains("--features=+EXPORT,-FINFINC")
    }

    def "should allow to add features from command line to those from configuration"() {
        given:
            buildFile << """
                ${getBasicGradlePitestConfig()}

                pitest {
                    failWhenNoMutations = false
                    features = ['-FINFINC']
                }
            """.stripIndent()
        when:
            ExecutionResult result = runTasksSuccessfully('pitest', '--additionalFeatures=+EXPORT')
        then:
            result.standardOutput.contains("--features=-FINFINC,+EXPORT")
    }
}
