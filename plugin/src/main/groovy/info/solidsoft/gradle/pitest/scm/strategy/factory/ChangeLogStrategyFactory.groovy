package info.solidsoft.gradle.pitest.scm.strategy.factory

import info.solidsoft.gradle.pitest.exception.InvalidChangeLogStrategyException
import info.solidsoft.gradle.pitest.scm.ScmContext
import info.solidsoft.gradle.pitest.scm.strategy.ChangeLogStrategy
import info.solidsoft.gradle.pitest.scm.strategy.CustomChangeLogStrategy
import info.solidsoft.gradle.pitest.scm.strategy.LastCommitStrategy
import info.solidsoft.gradle.pitest.scm.strategy.LocalChangesStrategy

class ChangeLogStrategyFactory implements AbstractChangeLogStrategyFactory{

    private ScmContext scmContext

    ChangeLogStrategyFactory (ScmContext scmContext) {
        this.scmContext = scmContext
    }

    @Override
    ChangeLogStrategy fromType(String goal) {
        switch (goal) {
            case 'lastCommit':
                return new LastCommitStrategy(scmContext.getScmRoot())
            case 'localChanges':
                return new LocalChangesStrategy(scmContext.getScmRoot())
            case 'custom':
                return new CustomChangeLogStrategy.Builder()
                            .fileSet(scmContext.getScmRoot())
                            .startVersion(scmContext.getStartVersion())
                            .startVersionType(scmContext.getStartVersionType())
                            .endVersion(scmContext.getEndVersion())
                            .endVersionType(scmContext.getEndVersionType())
                            .build()
            default:
                throw new InvalidChangeLogStrategyException("Invalid goal, received: $goal, possible goals are [lastCommit, localChanges, custom]")
        }
    }
}
