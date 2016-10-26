package info.solidsoft.gradle.pitest.functional

class PitestPluginFunctional1Spec extends AbstractPitestFunctionalSpec {

    def "setup and run simple build on pitest infrastructure"() {
        given:
            buildFile << """
                apply plugin: 'pl.droidsonroids.pitest'
                apply plugin: 'com.android.library'

                android {
                    buildToolsVersion '25.0.0'
                    compileSdkVersion 25
                    defaultConfig {
                        minSdkVersion 10
                        targetSdkVersion 25
                    }
                    lintOptions {
                        //ignore missing lint database
                        abortOnError false
                    }
                }
                repositories {
                    mavenCentral()
                    jcenter()
                }
                dependencies {
                    testCompile 'junit:junit:4.12'
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
            def result = runTasksSuccessfully('build')
        then:
            fileExists('build/intermediates/classes/release/gradle/pitest/test/hello/HelloWorld.class')
            result.wasExecuted(':test')
    }

    def "setup and run pitest task"() {
        given:
            buildFile << getBasicGradlePitestConfig()
        and:
            writeManifestFile()
            writeHelloWorld('gradle.pitest.test.hello')
            writeTest('src/test/java/', 'gradle.pitest.test.hello', false)
        when:
            def result = runTasksSuccessfully('pitestRelease')
        then:
            result.wasExecuted(':pitestRelease')
            result.getStandardOutput().contains('Generated 1 mutations Killed 0 (0%)')
    }

    def writeManifestFile(){
        def manifestFile = new File(projectDir, 'src/main/AndroidManifest.xml')
        manifestFile.parentFile.mkdirs()
        manifestFile.write('<?xml version="1.0" encoding="utf-8"?><manifest package="pl.droidsonroids.pitest.hello"/>')
    }

    private static String getBasicGradlePitestConfig() {
        return """
                apply plugin: 'pl.droidsonroids.pitest'
                apply plugin: 'com.android.library'

                android {
                    buildToolsVersion '25.0.0'
                    compileSdkVersion 25
                    defaultConfig {
                        minSdkVersion 10
                        targetSdkVersion 25
                    }
                }
                group = 'gradle.pitest.test'

                repositories {
                    mavenCentral()
                    jcenter()
                }
                buildscript {
                    repositories {
                        mavenCentral()
                        jcenter()
                    }
//                    //Local/current version of the plugin should be put on a classpath anyway
//                    //That cannot be also used to override the plugin version as the current version is earlier on a classpath
//                    dependencies {
//                        classpath 'info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.1.9'
//                    }
                }
                dependencies {
                    testCompile 'junit:junit:4.12'
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
            def result = runTasksSuccessfully('pitestRelease')
        then:
            result.wasExecuted(':pitestRelease')
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
            def result = runTasksSuccessfully('pitestRelease')
        then:
            result.wasExecuted(':pitestRelease')
            result.getStandardError().contains('with the following plugin configuration')
            result.getStandardError().contains('pitest-plugin-configuration-reporter-plugin.key1=value1')
            result.getStandardError().contains('pitest-plugin-configuration-reporter-plugin.key2=value2')
    }
}
