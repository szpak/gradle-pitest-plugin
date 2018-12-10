package pl.droidsonroids.gradle.pitest.functional

import nebula.test.IntegrationSpec
import nebula.test.functional.ExecutionResult

abstract class AbstractPitestFunctionalSpec extends IntegrationSpec {

    void setup() {
        fork = true
        //to make stdout assertion work with Gradle 2.x - http://forums.gradle.org/gradle/topics/unable-to-catch-stdout-stderr-when-using-tooling-api-i-gradle-2-x#reply_15357743
        memorySafeMode = true   //shutdown Daemon after a few seconds of inactivity
        copyResources('AndroidManifest.xml', 'src/main/AndroidManifest.xml')

        ExecutionResult.metaClass.anyOutputContains {
            delegate.standardOutput.contains(it) || delegate.standardError.contains(it)
        }
    }

    def writeManifestFile() {
        def manifestFile = new File(projectDir, 'src/main/AndroidManifest.xml')
        manifestFile.parentFile.mkdirs()
        manifestFile.write('<?xml version="1.0" encoding="utf-8"?><manifest package="pl.droidsonroids.pitest.hello"/>')
    }

    static List<String> resolveRequestedAndroidGradlePluginVersion() {
        return ["3.2.1"]
    }
}
