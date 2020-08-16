package pl.droidsonroids.gradle.pitest

import groovy.transform.CompileDynamic
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

@CompileDynamic
@SuppressWarnings(["DuplicateNumberLiteral", "DuplicateMapLiteral"])
class AndroidUtils {

    static final String PITEST_RELEASE_TASK_NAME = "${PitestPlugin.PITEST_TASK_NAME}Release"

    static Project createSampleLibraryProject(File... rootDir) {
        ProjectBuilder builder = ProjectBuilder.builder()
        if (rootDir.length > 0) {
            builder.withProjectDir(rootDir[0])
        }
        Project project = builder.build()
        ClassLoader classLoader = AndroidUtils.classLoader
        URL resource = classLoader.getResource('lib/AndroidManifest.xml')
        File manifestFile = project.file('src/main/AndroidManifest.xml')
        manifestFile.parentFile.mkdirs()
        manifestFile.write(resource.text)
        project.buildscript.repositories {
            google()
            mavenCentral()
        }
        project.apply(plugin: "com.android.library")
        project.android.with {
            compileSdkVersion 30
            defaultConfig {
                minSdkVersion 10
                targetSdkVersion 30
            }
        }
        project.apply(plugin: "pl.droidsonroids.pitest")
        return project
    }

    static Project createSampleApplicationProject(File... rootDir) {
        ProjectBuilder builder = ProjectBuilder.builder()
        if (rootDir.length > 0) {
            builder.withProjectDir(rootDir[0])
        }
        Project project = builder.build()
        ClassLoader classLoader = AndroidUtils.classLoader
        URL resource = classLoader.getResource('app/AndroidManifest.xml')
        File manifestFile = project.file('src/main/AndroidManifest.xml')
        manifestFile.parentFile.mkdirs()
        manifestFile.write(resource.text)
        project.buildscript.repositories {
            google()
            mavenCentral()
        }
        project.apply(plugin: "com.android.application")
        project.android.with {
            compileSdkVersion 30
            defaultConfig {
                minSdkVersion 10
                targetSdkVersion 30
            }
            buildTypes {
                release { }
                debug { }
            }
            productFlavors {
                flavorDimensions 'tier', 'color'
                free {
                    dimension 'tier'
                }
                pro {
                    dimension 'tier'
                }
                blue {
                    dimension 'color'
                }
                red {
                    dimension 'color'
                }
            }
            testOptions {
                unitTests.returnDefaultValues = true
            }
        }
        project.apply(plugin: "pl.droidsonroids.pitest")
        return project
    }

}
