package pl.droidsonroids.gradle.pitest.functional

import nebula.test.functional.ExecutionResult

import static com.android.builder.model.Version.ANDROID_GRADLE_PLUGIN_VERSION

class KotlinPitestPluginFunctionalSpec extends AbstractPitestFunctionalSpec {

    def "setup and run simple build on pitest infrastructure with kotlin plugin"() {
        given:
            buildFile << """
                apply plugin: 'pl.droidsonroids.pitest'
                apply plugin: 'com.android.library'
                apply plugin: 'kotlin-android'

                buildscript {
                    repositories {
                        google()
                        jcenter()
                    }
                    dependencies {
                        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.20"
                        classpath 'com.android.tools.build:gradle:3.3.0'
                    }
                }

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
                    implementation "org.jetbrains.kotlin:kotlin-stdlib:1.3.20"
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
            if (ANDROID_GRADLE_PLUGIN_VERSION.startsWith("3.2")) {
                fileExists('build/intermediates/javac/release/compileReleaseJavaWithJavac/classes/gradle/pitest/test/hello/HelloWorld.class')
            } else {
                fileExists('build/intermediates/classes/release/gradle/pitest/test/hello/HelloWorld.class')
            }
            result.wasExecuted(':test')
    }

    def "should run mutation analysis with Android Gradle plugin 3"() {
        when:
            copyResources("testProjects/simpleKotlin", "")
        then:
            fileExists('build.gradle')
        when:
            ExecutionResult result = runTasksSuccessfully('pitestRelease')
        then:
            result.wasExecuted(':pitestRelease')
            result.standardOutput.contains('Generated 3 mutations Killed 3 (100%)')
    }

    def "should run mutation analysis with Android Gradle plugin 2"() {
        when:
            copyResources("testProjects/simpleKotlin", "")
        then:
            fileExists('build.gradle')
        when:
            ExecutionResult result = runTasksSuccessfully('pitestRelease')
        then:
            result.wasExecuted(':pitestRelease')
            result.standardOutput.contains('Generated 3 mutations Killed 3 (100%)')
    }
}
