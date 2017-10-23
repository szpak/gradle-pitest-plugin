package info.solidsoft.gradle.pitest.scm;

class ChangeLogStrategyFactory {
    static ChangeLogStrategy fromType(String typeName) {
        switch (typeName) {
            case 'lastCommit':
                return new LastCommitStrategy()
            case 'localChanges':
                return new LocalChangesStrategy()
            case 'custom':
                return new CustomChangeLogStrategy()
            default:
                throw new InvalidChangeLogStrategyException("Invalid goal, received: $typeName, possible goals are [lastCommit, localChanges, custom]")
        }
    }
}
