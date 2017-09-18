package info.solidsoft.gradle.pitest.functional

import nebula.test.functional.ExecutionResult

//Note: gradle-override-plugin has important limitations in support for collections
//See: https://github.com/nebula-plugins/gradle-override-plugin/issues/1 or https://github.com/nebula-plugins/gradle-override-plugin/issues/3
class OverridePluginFunctionalSpec extends AbstractPitestFunctionalSpec {

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
                dependencies { testCompile 'junit:junit:4.11' }
            """.stripIndent()
        and:
            writeHelloWorld('gradle.pitest.test.hello')
        when:
            ExecutionResult result = runTasksSuccessfully('pitest', '-Doverride.pitest.reportDir=build/treports')
        then:
            result.getStandardOutput().contains('Generated 1 mutations Killed 0 (0%)')
            fileExists('build/treports')
    }
}
