package info.solidsoft.gradle.pitest.scm;

class ChangeLogStrategyFactory {

    private File rootFileSet

    ChangeLogStrategyFactory (String filePath) {
        rootFileSet = new File(filePath)
    }

    ChangeLogStrategyFactory (File file) {
        rootFileSet = file
    }

    ChangeLogStrategy fromType(String typeName) {
        switch (typeName) {
            case 'lastCommit':
                return new LastCommitStrategy(rootFileSet)
            case 'localChanges':
                return new LocalChangesStrategy(rootFileSet)
            case 'custom':
                return new CustomChangeLogStrategy(rootFileSet)
            default:
                throw new InvalidChangeLogStrategyException("Invalid goal, received: $typeName, possible goals are [lastCommit, localChanges, custom]")
        }
    }
}
