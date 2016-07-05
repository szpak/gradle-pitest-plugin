package info.solidsoft.gradle.pitest.functional

import nebula.test.IntegrationSpec

abstract class AbstractPitestFunctionalSpec extends IntegrationSpec {

    void setup() {
        fork = true //to make stdout assertion work with Gradle 2.x - http://forums.gradle.org/gradle/topics/unable-to-catch-stdout-stderr-when-using-tooling-api-i-gradle-2-x#reply_15357743
    }

    def writeManifestFile() {
        def manifestFile = new File(projectDir, 'src/main/AndroidManifest.xml')
        manifestFile.parentFile.mkdirs()
        manifestFile.write('<?xml version="1.0" encoding="utf-8"?><manifest package="com.example.test"/>')
    }
}
