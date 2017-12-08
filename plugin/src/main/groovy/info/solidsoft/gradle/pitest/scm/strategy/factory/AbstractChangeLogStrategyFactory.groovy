package info.solidsoft.gradle.pitest.scm.strategy.factory

import info.solidsoft.gradle.pitest.scm.strategy.ChangeLogStrategy

interface AbstractChangeLogStrategyFactory {
    ChangeLogStrategy fromType(String type)
}
