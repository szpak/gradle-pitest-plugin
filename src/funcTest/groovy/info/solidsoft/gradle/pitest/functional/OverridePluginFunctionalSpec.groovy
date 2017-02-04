package info.solidsoft.gradle.pitest.functional

//Note: gradle-override-plugin has important limitations in support for collections
//See: https://github.com/nebula-plugins/gradle-override-plugin/issues/1 or https://github.com/nebula-plugins/gradle-override-plugin/issues/3
class OverridePluginFunctionalSpec extends AbstractPitestFunctionalSpec {

    def "should allow to override String configuration parameter from command line"() {
        given:
            buildFile << """
                apply plugin: 'nebula-override'
                apply plugin: 'com.android.library'
                apply plugin: 'pl.droidsonroids.pitest'

                android {
                    buildToolsVersion '25.0.2'
                    compileSdkVersion 25
                    defaultConfig {
                        minSdkVersion 10
                        targetSdkVersion 25
                    }
                }
                group = 'gradle.pitest.test'

                buildscript {
                    repositories { jcenter() }
                    dependencies {
                        classpath 'com.netflix.nebula:gradle-override-plugin:1.12.+'
                        classpath 'com.android.tools.build:gradle:2.2.3'
                    }
                }
                repositories { mavenCentral() }
                dependencies { testCompile 'junit:junit:4.12' }
            """.stripIndent()
        and:
            writeHelloWorld('gradle.pitest.test.hello')
        when:
            def result = runTasksSuccessfully('pitestRelease', '-Doverride.pitest.reportDir=build/treports')
        then:
            result.getStandardOutput().contains('Generated 1 mutations Killed 0 (0%)')
            fileExists('build/treports')
    }
}
