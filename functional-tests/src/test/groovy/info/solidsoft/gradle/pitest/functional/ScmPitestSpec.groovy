package info.solidsoft.gradle.pitest.functional

class ScmPitestSpec extends AbstractPitestFunctionalSpec {

    def "Last commit strategy" () {
        given:
            copyResources("testProjects/scmProject","")
        when:
            def result = runTasksSuccessfully("scmPitest")
        then:
            result.standardOutput.isEmpty()
    }
}
