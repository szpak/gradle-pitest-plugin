package info.solidsoft.gradle.pitest.functional

class KotlinPitestPluginFunctionalSpec extends AbstractPitestFunctionalSpec {

    def "setup and run simple build on pitest infrastructure with kotlin plugin and AGP #requestedAndroidGradlePluginVersion"() {
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
                        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.2.41"
                        classpath 'com.android.tools.build:gradle:$requestedAndroidGradlePluginVersion'
                    }
                }

                android {
                    compileSdkVersion 27
                    defaultConfig {
                        minSdkVersion 10
                        targetSdkVersion 27
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
                    testCompile 'junit:junit:4.12'
                    compile "org.jetbrains.kotlin:kotlin-stdlib:1.2.41"
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
            if (requestedAndroidGradlePluginVersion.startsWith("3.2")) {
                fileExists('build/intermediates/javac/release/compileReleaseJavaWithJavac/classes/gradle/pitest/test/hello/HelloWorld.class')
            } else {
                fileExists('build/intermediates/classes/release/gradle/pitest/test/hello/HelloWorld.class')
            }
            result.wasExecuted(':test')
        where:
            requestedAndroidGradlePluginVersion << resolveRequestedAndroidGradlePluginVersion()
    }

    def "should run mutation analysis with Android Gradle plugin 3"() {
        when:
            copyResources("testProjects/simpleKotlin", "")
        then:
            fileExists('build.gradle')
        when:
            def result = runTasksSuccessfully('pitestRelease')
        then:
            result.wasExecuted(':pitestRelease')
            result.getStandardOutput().contains('Generated 3 mutations Killed 3 (100%)')
    }

    def "should run mutation analysis with Android Gradle plugin 2"() {
        when:
            copyResources("testProjects/simpleKotlin", "")
        then:
            fileExists('build.gradle')
        when:
            def result = runTasksSuccessfully('pitestRelease')
        then:
            result.wasExecuted(':pitestRelease')
            result.getStandardOutput().contains('Generated 3 mutations Killed 3 (100%)')
    }
}
