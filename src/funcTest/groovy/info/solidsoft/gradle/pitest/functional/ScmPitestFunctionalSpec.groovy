package info.solidsoft.gradle.pitest.functional

import nebula.test.functional.ExecutionResult

class ScmPitestFunctionalSpec extends AbstractPitestFunctionalSpec {

    def "should execute scmPitest task" () {
        when:
            copyResources("testProjects/scmProject","")
        then:
            fileExists("build.gradle")
        when:
            ExecutionResult result = runTasksSuccessfully("scmPitest")
        then:
            result.wasExecuted("scmProject:pitest")
            result.standardOutput
    }

}
