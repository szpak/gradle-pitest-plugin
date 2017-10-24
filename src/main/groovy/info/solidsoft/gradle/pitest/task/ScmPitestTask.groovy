package info.solidsoft.gradle.pitest.task

import info.solidsoft.gradle.pitest.PitestPlugin
import info.solidsoft.gradle.pitest.scm.ChangeLogStrategy
import info.solidsoft.gradle.pitest.scm.ScmConnection

class ScmPitestTask extends AbstractPitestTask {

    public static final String NAME = "scmPitest"

    ScmPitestTask() {
        description = "Run PIT analysis against SCM for java classes"
        group = PitestPlugin.PITEST_TASK_GROUP
    }

    ScmConnection scm
    String connectionType
    ChangeLogStrategy goal
    File scmRoot
    String startVersionType
    String startVersion
    String endVersionType
    String endVersion
}
