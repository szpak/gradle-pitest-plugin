package info.solidsoft.gradle.pitest.scm

interface AbstractChangeLogStrategyFactory {
    ChangeLogStrategy fromType(String type)
}
