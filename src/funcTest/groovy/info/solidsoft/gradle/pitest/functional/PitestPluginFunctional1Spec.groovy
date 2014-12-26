package info.solidsoft.gradle.pitest.functional

import nebula.test.IntegrationSpec

class PitestPluginFunctional1Spec extends IntegrationSpec {

    def "setup and run simple build on pitest infrastructure"() {
        given:
            buildFile << """
                apply plugin: 'java'
                repositories {
                    mavenCentral()
                }
                dependencies {
                    testCompile 'junit:junit:4.11'
                }
            """.stripIndent()
        when:
            writeHelloWorld('gradle.pitest.test.hello')
        then:
            fileExists('src/main/java/gradle/pitest/test/hello/HelloWorld.java')
        when:
            writeTest('src/test/java/', 'gradle.pitest.test.hello', false)
        then:
            fileExists('src/test/java/gradle/pitest/test/hello/HelloWorldTest.java')
        when:
            def result = runTasksSuccessfully('build')
        then:
            fileExists('build/classes/main/gradle/pitest/test/hello/HelloWorld.class')
            result.wasExecuted(':test')
    }

    def "setup and run pitest task"() {
        given:
            buildFile << """
                apply plugin: 'java'
                apply plugin: 'info.solidsoft.pitest'
                group = 'gradle.pitest.test'

                repositories {
                    mavenCentral()
                }
                buildscript {
                    repositories {
                        mavenCentral()
                    }
//                    //Local/current version of the plugin should be put on a classpath anyway
//                    //That cannot be also used to override the plugin version as the current version is earlier on a classpath
//                    dependencies {
//                        classpath 'info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.0.0'
//                    }
                }
                dependencies {
                    testCompile 'junit:junit:4.11'
                }
            """.stripIndent()
        and:
            writeHelloWorld('gradle.pitest.test.hello')
            writeTest('src/test/java/', 'gradle.pitest.test.hello', false)
        when:
            def result = runTasksSuccessfully('pitest')
        then:
            result.wasExecuted(':pitest')
            result.getStandardOutput().contains('Generated 1 mutations Killed 0 (0%)')
    }
}
