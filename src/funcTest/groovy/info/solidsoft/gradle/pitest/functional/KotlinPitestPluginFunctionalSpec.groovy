package info.solidsoft.gradle.pitest.functional

class KotlinPitestPluginFunctionalSpec extends AbstractPitestFunctionalSpec {

    def "setup and run simple build on pitest infrastructure with kotlin plugin"() {
        given:
            buildFile << """
                apply plugin: 'pl.droidsonroids.pitest'
                apply plugin: 'com.android.library'
                //apply plugin: 'kotlin-android'

                buildscript {
                    repositories {
                        jcenter()
                    }
                    dependencies {
                        //It passes without this line
                        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.1.0"
                    }
                }

                android {
                    buildToolsVersion '25.0.2'
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
                    //compile "org.jetbrains.kotlin:kotlin-stdlib:1.1.0"
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

    def writeManifestFile(){
        def manifestFile = new File(projectDir, 'src/main/AndroidManifest.xml')
        manifestFile.parentFile.mkdirs()
        manifestFile.write('<?xml version="1.0" encoding="utf-8"?><manifest package="pl.droidsonroids.pitest.hello"/>')
    }
}
