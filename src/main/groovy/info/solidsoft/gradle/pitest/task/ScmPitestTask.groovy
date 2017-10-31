package info.solidsoft.gradle.pitest.task

import info.solidsoft.gradle.pitest.ConnectionTypeValidator
import info.solidsoft.gradle.pitest.CustomStrategyValidator
import info.solidsoft.gradle.pitest.GoalValidator
import info.solidsoft.gradle.pitest.PitestPlugin

import info.solidsoft.gradle.pitest.scm.ChangeLogStrategy
import info.solidsoft.gradle.pitest.scm.ChangeLogStrategyFactory
import info.solidsoft.gradle.pitest.scm.ScmConnection
import org.apache.maven.scm.manager.BasicScmManager
import org.apache.maven.scm.manager.ScmManager
import org.apache.maven.scm.provider.git.gitexe.GitExeScmProvider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional


class ScmPitestTask extends AbstractPitestTask {

    @Input
    ScmConnection scm

    @Input
    String connectionType

    @Input
    String goal

    @Input
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
    ChangeLogStrategy strategy

    ScmPitestTask() {
        description = "Run PIT analysis against SCM for java classes"
        group = PitestPlugin.PITEST_TASK_GROUP
        addValidator(new GoalValidator(this))
        addValidator(new CustomStrategyValidator(this))
        addValidator(new ConnectionTypeValidator(this))
    }

    @Override
    void exec() {
        setManagerToDefaultIfNoneProvided()
        strategy = new ChangeLogStrategyFactory(getScmRoot()).fromType(getGoal())
        String url = getConnectionUrl()
        targetClasses = strategy.getModifiedFilenames(manager, getIncludes(), url)
        args = createListOfAllArgumentsForPit()
        jvmArgs = (getMainProcessJvmArgs() ?: getJvmArgs())
        main = "org.pitest.mutationtest.commandline.MutationCoverageReport"
        classpath = getLaunchClasspath()
        super.exec()
    }

    private void setManagerToDefaultIfNoneProvided() {
        if (manager == null) {
            manager = new BasicScmManager()
            manager.setScmProvider("git", new GitExeScmProvider())
        }
    }

    private String getConnectionUrl() {
        switch (getConnectionType()) {
            case 'connection':
                return getScm().connection
            default:
                return getScm().developerConnection
        }
    }
}
