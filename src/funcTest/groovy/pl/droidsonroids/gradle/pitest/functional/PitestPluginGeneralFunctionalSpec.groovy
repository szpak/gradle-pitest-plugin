package pl.droidsonroids.gradle.pitest.functional

import nebula.test.functional.ExecutionResult

class PitestPluginGeneralFunctionalSpec extends AbstractPitestFunctionalSpec {

    def "setup and run simple build on pitest infrastructure with AGP: #requestedAndroidGradlePluginVersion"() {
        given:
        buildFile << """
                buildscript {
                    repositories {
                        google()
                        jcenter()
                    }
                    dependencies {
                        classpath 'com.android.tools.build:gradle:$requestedAndroidGradlePluginVersion'
                    }
                }
                
                apply plugin: 'pl.droidsonroids.pitest'
                apply plugin: 'com.android.library'

                android {
                    compileSdkVersion 28
                    defaultConfig {
                        minSdkVersion 10
                        targetSdkVersion 28
                    }
                    lintOptions {
                        //ignore missing lint database
                        abortOnError false
                    }
                }
                repositories {
                    google()
                    mavenCentral()
                    jcenter()
                }
                dependencies {
                    testImplementation 'junit:junit:4.12'
                }
            """.stripIndent()
        and:
        writeManifestFile()
        when:
        writeHelloWorld('gradle.pitest.test.hello')
        then:
        fileExists('src/main/java/gradle/pitest/test/hello/HelloWorld.java')
        when:
        writeTest('src/test/java/', 'gradle.pitest.test.hello', false)
        then:
        fileExists('src/test/java/gradle/pitest/test/hello/HelloWorldTest.java')
        when:
        ExecutionResult result = runTasksSuccessfully('build')
        then:
        if (requestedAndroidGradlePluginVersion.startsWith("3.2")) {
            fileExists('build/intermediates/javac/release/compileReleaseJavaWithJavac/classes/gradle/pitest/test/hello/HelloWorld.class')
        } else {
            fileExists('build/intermediates/classes/release/gradle/pitest/test/hello/HelloWorld.class')
        }
        result.wasExecuted(':test')
        where:
        requestedAndroidGradlePluginVersion << resolveRequestedAndroidGradlePluginVersion()
    }

    def "enable PIT plugin when on classpath and pass plugin configuration to PIT"() {
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
                    excludedClasses = []
                    verbose = true
                    pluginConfiguration = ['pitest-plugin-configuration-reporter-plugin.key1': 'value1',
                                           'pitest-plugin-configuration-reporter-plugin.key2': 'value2']
                    features = ["-FANN", "+FINFIT(a[1] a[2])"]
                    outputFormats = ['pluginConfigurationReporter']
                }
            """.stripIndent()
        and:
            writeHelloPitClass()
            writeHelloPitTest()
        when:
            ExecutionResult result = runTasksSuccessfully('pitest')
        then:
            result.wasExecuted(':pitest')
        and: 'plugin enabled'
            assertStdOutOrStdErrContainsGivenText(result, 'with the following plugin configuration')
        and: 'plugin parameters passed'
            result.getStandardOutput().contains('pitest-plugin-configuration-reporter-plugin.key1=value1')
            result.getStandardOutput().contains('pitest-plugin-configuration-reporter-plugin.key2=value2')
        and: 'built-in features passed'
            result.getStandardOutput().contains("-FANN")
            result.getStandardOutput().contains("+FINFIT")
        and: 'verbose output enabled'
            assertStdOutOrStdErrContainsGivenText(result, "PIT >> FINE")
            //TODO: Add plugin features once available - https://github.com/hcoles/pitest-plugins/issues/2
    }

    def "use file to pass additional classpath to PIT if enabled"() {   //Needed? Already tested with ProjectBuilder in PitestTaskConfigurationSpec
        given:
            buildFile << getBasicGradlePitestConfig()
            buildFile << """
                pitest {
                    useClasspathFile = true
                }
            """.stripIndent()
        and:
            writeHelloPitClass()
            writeHelloPitTest()
        when:
            ExecutionResult result = runTasksSuccessfully('pitest')
        then:
            result.wasExecuted(':pitest')
            result.getStandardOutput().contains('--classPathFile=')
            //TODO: Verify file name with regex
            !result.getStandardOutput().find("--classPath=")
    }
}
