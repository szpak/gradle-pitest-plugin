package info.solidsoft.gradle.pitest.functional

class PitestPluginFunctional1Spec extends AbstractPitestFunctionalSpec {

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
            buildFile << getBasicGradlePitestConfig()
        and:
            writeHelloWorld('gradle.pitest.test.hello')
            writeTest('src/test/java/', 'gradle.pitest.test.hello', false)
        when:
            def result = runTasksSuccessfully('pitest')
        then:
            result.wasExecuted(':pitest')
            result.getStandardOutput().contains('Generated 1 mutations Killed 0 (0%)')
    }

    private static String getBasicGradlePitestConfig() {
        return """
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
//                        classpath 'info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.1.9'
//                    }
                }
                dependencies {
                    testCompile 'junit:junit:4.11'
                }
        """.stripIndent()
    }

    def "enable PIT plugin when on classpath"() {
        given:
            buildFile << getBasicGradlePitestConfig()
            buildFile << """
                buildscript {
                    repositories {
                        maven { url "https://dl.bintray.com/szpak/pitest-plugins/" }
                    }
                    configurations.maybeCreate("pitest")
                    dependencies {
                        pitest 'org.pitest.plugins:pitest-high-isolation-plugin:0.0.2'
                    }
                }
                pitest {
                    verbose = true
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
            result.getStandardError().contains('Marking all mutations as requiring isolation')
    }

    def "enable pass plugin configuration to PIT"() {
        given:
            buildFile << getBasicGradlePitestConfig()
            buildFile << """
                buildscript {
                    repositories {
                        maven { url "https://dl.bintray.com/szpak/pitest-plugins/" }
                    }
                    configurations.maybeCreate("pitest")
                    dependencies {
                        pitest 'org.pitest.plugins:pitest-plugin-configuration-reporter-plugin:0.0.2'
                    }
                }
                pitest {
                    verbose = true
                    pluginConfiguration = ['pitest-plugin-configuration-reporter-plugin.key1': 'value1',
                                           'pitest-plugin-configuration-reporter-plugin.key2': 'value2']
                    outputFormats = ['pluginConfigurationReporter']
                }
            """.stripIndent()
        and:
            writeHelloWorld('gradle.pitest.test.hello')
            writeTest('src/test/java/', 'gradle.pitest.test.hello', false)
        when:
            def result = runTasksSuccessfully('pitest')
        then:
            result.wasExecuted(':pitest')
            result.getStandardError().contains('with the following plugin configuration')
            result.getStandardError().contains('pitest-plugin-configuration-reporter-plugin.key1=value1')
            result.getStandardError().contains('pitest-plugin-configuration-reporter-plugin.key2=value2')
    }
}
