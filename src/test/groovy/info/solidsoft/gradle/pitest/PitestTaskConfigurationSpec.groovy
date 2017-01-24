package info.solidsoft.gradle.pitest

class PitestTaskConfigurationSpec extends BasicProjectBuilderSpec {

    private PitestTask task

    def "should pass classPathFile parameter to PIT execution"() {
        given:
            File testClassPathFile = tmpProjectDir.newFile('classPathFile')
            String testClassPathFileAsString = testClassPathFile.path
        and:
            pitestConfig.classPathFile = testClassPathFile
        and:
            task = getJustOnePitestTaskOrFail()
        expect:
            task.createTaskArgumentMap()['classPathFile'] == testClassPathFileAsString
    }

    def "should not pass to PIT parameters not set by default if not set explicitly"() {
        given:
            task = getJustOnePitestTaskOrFail()
        expect:
            !task.createTaskArgumentMap().containsKey(paramName)
        where:
            paramName << ['classPathFile']
    }
}
