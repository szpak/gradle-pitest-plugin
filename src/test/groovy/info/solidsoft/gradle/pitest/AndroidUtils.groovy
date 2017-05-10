package info.solidsoft.gradle.pitest

import org.gradle.testfixtures.ProjectBuilder

class AndroidUtils {
    static final String PITEST_RELEASE_TASK_NAME = "${PitestPlugin.PITEST_TASK_NAME}Release"

    static createSampleLibraryProject(File... rootDir) {
        def builder = ProjectBuilder.builder()
        if (rootDir.length > 0) {
            builder.withProjectDir(rootDir[0])
        }
        def project = builder.build()
        ClassLoader classLoader = AndroidUtils.class.classLoader
        def resource = classLoader.getResource('lib/AndroidManifest.xml')
        def manifestFile = project.file('src/main/AndroidManifest.xml')
        manifestFile.parentFile.mkdirs()
        manifestFile.write(resource.text)
        project.buildscript.repositories {
            mavenCentral()
        }
        project.apply(plugin: "com.android.library")
        project.android.with {
            buildToolsVersion '25.0.3'
            compileSdkVersion 25
            defaultConfig {
                minSdkVersion 10
                targetSdkVersion 25
            }
        }
        project.apply(plugin: "pl.droidsonroids.pitest")
        return project
    }

    static createSampleApplicationProject(File... rootDir) {
        def builder = ProjectBuilder.builder()
        if (rootDir.length > 0) {
            builder.withProjectDir(rootDir[0])
        }
        def project = builder.build()
        ClassLoader classLoader = AndroidUtils.class.classLoader
        def resource = classLoader.getResource('app/AndroidManifest.xml')
        def manifestFile = project.file('src/main/AndroidManifest.xml')
        manifestFile.parentFile.mkdirs()
        manifestFile.write(resource.text)
        project.buildscript.repositories {
            mavenCentral()
        }
        project.apply(plugin: "com.android.application")
        project.android.with {
            buildToolsVersion '25.0.3'
            compileSdkVersion 25
            defaultConfig {
                minSdkVersion 10
                targetSdkVersion 25
            }
            buildTypes {
                release { }
                debug { }
            }
            productFlavors {
                variant1 { }
                variant2 { }
            }
        }
        project.apply(plugin: "pl.droidsonroids.pitest")
        return project
    }
}
