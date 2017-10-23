package info.solidsoft.gradle.pitest.task

import info.solidsoft.gradle.pitest.scm.ChangeLogStrategy
import info.solidsoft.gradle.pitest.scm.ScmConnection

class ScmPitestTask extends AbstractPitestTask {

    public static final String NAME = "scmPitest"

    ScmConnection scm
    String connectionType
    ChangeLogStrategy goal
    File scmRoot
}
