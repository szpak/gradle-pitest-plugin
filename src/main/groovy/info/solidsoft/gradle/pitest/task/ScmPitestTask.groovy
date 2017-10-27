package info.solidsoft.gradle.pitest.task

import info.solidsoft.gradle.pitest.PitestPlugin
import info.solidsoft.gradle.pitest.scm.ChangeLogStrategy
import info.solidsoft.gradle.pitest.scm.ScmConnection
import org.apache.maven.scm.manager.BasicScmManager
import org.apache.maven.scm.manager.ScmManager
import org.apache.maven.scm.provider.git.gitexe.GitExeScmProvider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

import java.util.logging.Logger

class ScmPitestTask extends AbstractPitestTask {

    static final Logger LOG = Logger.getLogger(ScmPitestTask.class.typeName)
    public static final String NAME = "scmPitest"

    @Input
    ScmConnection scm

    @Input
    String connectionType

    @Input
    ChangeLogStrategy goal
    File scmRoot

    @Input
    @Optional
    Set<String> includes

    @Input
    @Optional
    String startVersionType

    @Input
    @Optional
    String startVersion

    @Input
    @Optional
    String endVersionType

    @Input
    @Optional
    String endVersion

    ScmManager manager

    ScmPitestTask() {
        LOG.info("ScmPitest task configured")
        description = "Run PIT analysis against SCM for java classes"
        group = PitestPlugin.PITEST_TASK_GROUP
        manager = new BasicScmManager()
        manager.setScmProvider("git", new GitExeScmProvider())
    }

    @Override
    void exec() {
        String url = ""
        if (connectionType == "connection") {
            url = scm.connection
        }
        if (connectionType == "developerConnection") {
            url = scm.developerConnection
        }
        LOG.info("########## INCLUDES: ${getIncludes()} ##########")
        targetClasses = getGoal().getModifiedFilenames(manager, includes, url)
        args = createListOfAllArgumentsForPit()
        jvmArgs = (getMainProcessJvmArgs() ?: getJvmArgs())
        main = "org.pitest.mutationtest.commandline.MutationCoverageReport"
        classpath = getLaunchClasspath()
        super.exec()
    }
}
