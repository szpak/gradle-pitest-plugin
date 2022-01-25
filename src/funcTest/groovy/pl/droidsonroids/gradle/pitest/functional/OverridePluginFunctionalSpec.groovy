package pl.droidsonroids.gradle.pitest.functional

import groovy.transform.CompileDynamic
import nebula.test.functional.ExecutionResult
import org.gradle.api.GradleException
import spock.lang.Issue
import spock.lang.PendingFeature

//Note: gradle-override-plugin has important limitations in support for collections
//See: https://github.com/nebula-plugins/gradle-override-plugin/issues/1 or https://github.com/nebula-plugins/gradle-override-plugin/issues/3
@CompileDynamic
class OverridePluginFunctionalSpec extends AbstractPitestFunctionalSpec {

    @PendingFeature(exceptions = GradleException, reason = "gradle-override-plugin nor @Option don't work with DirectoryProperty")
    void "should allow to override String configuration parameter from command line"() {
        given:
            buildFile << """
                apply plugin: 'nebula-override'
                apply plugin: 'com.android.library'
                apply plugin: 'pl.droidsonroids.pitest'

                android {
                    compileSdkVersion 30
                    defaultConfig {
                        minSdkVersion 10
                        targetSdkVersion 30
                    }
                }
                group = 'gradle.pitest.test'

                buildscript {
                    repositories {
                        google()
                        mavenCentral()
                    }
                    dependencies {
                        classpath 'com.netflix.nebula:gradle-override-plugin:1.12.+'
                        classpath 'com.android.tools.build:gradle:7.0.0'
                    }
                }
                repositories {
                    google()
                    mavenCentral()
                }
                dependencies { testImplementation 'junit:junit:4.13.2' }
            """.stripIndent()
        and:
        writeHelloWorld('gradle.pitest.test.hello')
        when:
        ExecutionResult result = runTasksSuccessfully('pitestRelease', '-Doverride.pitest.reportDir=build/treports')
        then:
        result.standardOutput.contains('Generated 1 mutations Killed 0 (0%)')
        fileExists('build/treports')
    }

    @Issue("https://github.com/szpak/gradle-pitest-plugin/issues/139")
    @PendingFeature(exceptions = GradleException, reason = "Not implemented yet due to Gradle limitations described in linked issue")
    void "should allow to define features from command line and override those from configuration"() {
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

    @Issue("https://github.com/szpak/gradle-pitest-plugin/issues/143")
    void "should allow to add features from command line to those from configuration and override selected tests"() {
        given:
        final String overriddenTargetTests = "com.foo.*"
        buildFile << """
                ${getBasicGradlePitestConfig()}

                pitest {
                    failWhenNoMutations = false
                    features = ['-FINFINC']
                    targetTests = ['com.example.tests.*']
                }
            """.stripIndent()
        when:
        ExecutionResult result = runTasksSuccessfully('pitestRelease', '--additionalFeatures=+EXPORT', "--targetTests=$overriddenTargetTests")
        then:
        result.standardOutput.contains("--features=-FINFINC,+EXPORT")
        and:
        result.standardOutput.contains("--targetTests=$overriddenTargetTests")
    }

}
