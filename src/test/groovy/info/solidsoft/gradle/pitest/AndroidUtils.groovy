package info.solidsoft.gradle.pitest

import org.gradle.testfixtures.ProjectBuilder

class AndroidUtils {
    static final String PITEST_RELEASE_TASK_NAME = "${PitestPlugin.PITEST_TASK_NAME}Release"

    static def createSampleLibraryProject() {
        def project = ProjectBuilder.builder().build()
        def manifestFile = project.file('src/main/AndroidManifest.xml')
        manifestFile.parentFile.mkdirs()
        manifestFile.write('<?xml version="1.0" encoding="utf-8"?><manifest package="com.example.test"/>')
        project.apply(plugin: "com.android.library")
        project.android.with {
            buildToolsVersion '24.0.0'
            compileSdkVersion 24
            defaultConfig {
                minSdkVersion 10
                targetSdkVersion 24
            }
        }
        project.apply(plugin: "pl.droidsonroids.pitest")
        project.evaluate()
        project
    }
}
