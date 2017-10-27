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

    def "should set startVersionType correctly ('#versionType')" () {
        given:
            project.scmPitest.startVersionType = versionType
        expect:
            scmPitestTask.startVersionType == versionType
        where:
            versionType << ['branch','tag','revision']
    }

    def "should set startVersion correctly ('#version')" () {
        given:
            project.scmPitest.startVersion = version
        expect:
            scmPitestTask.startVersion == version
        where:
            version << ['1.0.0.Final','releases/1.2.2','smoke-tests']
    }

    def "should set endVersionType correctly ('#versionType')" () {
        given:
            project.scmPitest.endVersionType = versionType
        expect:
            scmPitestTask.endVersionType == versionType
        where:
            versionType << ['branch','tag','revision']
    }

    def "should set endVersion correctly ('#version')" () {
        given:
            project.scmPitest.endVersion = version
        expect:
            scmPitestTask.endVersion == version
        where:
            version << ['1.0.0.Final','releases/1.2.2','smoke-tests']
    }

    def "should map connectionType '#connectionType' correctly" () {
        given:
            project.scmPitest.connectionType = connectionType
        expect:
            scmPitestTask.connectionType == connectionType
        where:
            connectionType << ["connection","developerConnection"]
    }
}
