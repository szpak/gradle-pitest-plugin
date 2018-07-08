package info.solidsoft.gradle.pitest.functional

import nebula.test.IntegrationSpec

abstract class AbstractPitestFunctionalSpec extends IntegrationSpec {

    void setup() {
        fork = true //to make stdout assertion work with Gradle 2.x - http://forums.gradle.org/gradle/topics/unable-to-catch-stdout-stderr-when-using-tooling-api-i-gradle-2-x#reply_15357743
        memorySafeMode = true   //shutdown Daemon after a few seconds of inactivity
        copyResources('AndroidManifest.xml', 'src/main/AndroidManifest.xml')
    }

    static List<String> resolveRequestedAndroidGradlePluginVersion() {
        return ["3.0.1", "3.1.0", "3.1.1", "3.1.2", "3.1.3", "3.2.0-alpha17"]
    }
}
