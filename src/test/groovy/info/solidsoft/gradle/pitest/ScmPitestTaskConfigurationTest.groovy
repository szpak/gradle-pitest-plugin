package info.solidsoft.gradle.pitest

class ScmPitestTaskConfigurationTest extends BasicProjectBuilderSpec implements WithScmPitestTaskInitialization {
    def "should configure scmRoot from string" () {
        given:
            project.scmPitest.scmRoot = "."
        expect:
            scmPitestTask.scmRoot == new File(".")
    }

    def "should configure scmRoot from file" () {
        given:
            project.scmPitest.scmRoot = new File(".")
        expect:
            scmPitestTask.scmRoot == new File(".")
    }

    def "should throw exception when invalid type for scmRoot is passed" () {
        when:
            project.scmPitest.scmRoot = 123456789
        then:
            thrown MissingPropertyException
    }
}
