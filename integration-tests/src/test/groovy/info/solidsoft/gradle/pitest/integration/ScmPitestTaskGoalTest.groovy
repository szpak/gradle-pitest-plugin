package info.solidsoft.gradle.pitest.integration

class ScmPitestTaskGoalTest extends BasicProjectBuilderSpec implements WithScmPitestTaskInitialization {

    def "should assign from type: '#type'" () {
        given:
            project.scmPitest.goal = type
        expect:
            scmPitestTask.goal == type
        where:
            type << ['custom', 'lastCommit', 'localChanges']
    }
}
