package info.solidsoft.gradle.pitest

import groovy.transform.CompileDynamic
import spock.lang.Issue

@CompileDynamic
@Issue("https://github.com/szpak/gradle-pitest-plugin/issues/277")
class PitestTaskTestPluginConfigurationSpec extends BasicProjectBuilderSpec {

    void "should not pass testPlugin for PIT 1.6.7+ (#pitVersion, #testPlugin, #junit5PluginVersion)"() {
        given:
            project.pitest.pitestVersion = pitVersion
            project.pitest.testPlugin = testPlugin
            project.pitest.junit5PluginVersion = junit5PluginVersion
        and:
            PitestTask task = getJustOnePitestTaskOrFail()  //TODO: Think if task initialization in PitestTaskConfigurationSpec is done in right place
        expect:
            task.taskArgumentMap()['testPlugin'] == null
        where:
            pitVersion       | testPlugin | junit5PluginVersion
            "1.6.7"          | "junit5"   | null
            "1.6.8-SNAPSHOT" | "junit5"   | null
            "1.7.0"          | "junit5"   | null
            "2.0.0"          | "junit5"   | null
            "2.0"            | "junit5"   | null
            "1.6.7"          | null       | "0.23"
            "1.6.8-SNAPSHOT" | null       | "0.23"
            "1.7.0"          | null       | "0.23"
            "2.0.0"          | null       | "0.23"
            "2.0"            | null       | "0.23"
    }

    void "should pass testPlugin for backward compatibility for PIT <1.6.7 (#pitVersion, #testPlugin, #junit5PluginVersion)"() {
        given:
            project.pitest.pitestVersion = pitVersion
            project.pitest.testPlugin = testPlugin
            project.pitest.junit5PluginVersion = junit5PluginVersion
        and:
            PitestTask task = getJustOnePitestTaskOrFail()
        expect:
            task.taskArgumentMap()['testPlugin'] == "junit5"
        where:
            pitVersion       | testPlugin | junit5PluginVersion
            "1.0.0"          | "junit5"   | null
            "1.6.6"          | "junit5"   | null
            "1.6.6-SNAPSHOT" | "junit5"   | null
            "1.0.0"          | "junit5"   | "0.23"
            "1.6.6"          | "junit5"   | "0.23"
            "1.6.6-SNAPSHOT" | "junit5"   | "0.23"
    }

    void "should automatically set testPlugin if junit5PluginVersion is used for any version"() {
        given:
            project.pitest.pitestVersion = pitVersion
            project.pitest.junit5PluginVersion = "0.23"
        and:
            PitestTask task = getJustOnePitestTaskOrFail()
        expect:
            project.pitest.testPlugin.getOrNull() == null
        and:
            task.taskArgumentMap()['testPlugin'] == null
        where:
            pitVersion << ["1.0.0", "1.8.0"]
    }

    void "should not fail for unrecognised PIT version and assume newer one"() {
        given:
            project.pitest.pitestVersion = "incorrect-version"
            project.pitest.testPlugin = "testng"
        and:
            PitestTask task = getJustOnePitestTaskOrFail()
        expect:
            task.taskArgumentMap()['testPlugin'] == "testng"
    }

}
