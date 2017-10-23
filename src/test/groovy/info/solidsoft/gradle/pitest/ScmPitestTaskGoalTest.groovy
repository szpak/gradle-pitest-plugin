package info.solidsoft.gradle.pitest

import info.solidsoft.gradle.pitest.scm.ChangeLogStrategy
import info.solidsoft.gradle.pitest.scm.CustomChangeLogStrategy
import info.solidsoft.gradle.pitest.scm.LastCommitStrategy
import info.solidsoft.gradle.pitest.scm.LocalChangesStrategy
import org.apache.maven.scm.manager.ScmManager

class ScmPitestTaskGoalTest extends BasicProjectBuilderSpec implements WithScmPitestTaskInitialization {

    private class ChangeLogStrategyDouble implements ChangeLogStrategy {

        @Override
        List<String> getModifiedFilenames(ScmManager manager, Set<String> includes, String url) {
            return Collections.emptyList()
        }
    }

    def "should assign from class" () {
        given:
            project.scmPitest.goal = new ChangeLogStrategyDouble()
        expect:
            scmPitestTask.goal.class == ChangeLogStrategyDouble.class
    }

    def "should assign from type: '#map.key'" () {
        given:
            project.scmPitest.goal = map.key
        expect:
            scmPitestTask.goal.class == map.value
        where:
            map << [custom:CustomChangeLogStrategy.class
                   , lastCommit: LastCommitStrategy.class
                   , localChanges: LocalChangesStrategy.class]
    }
}
