package info.solidsoft.gradle.pitest.task

import info.solidsoft.gradle.pitest.ConnectionTypeValidator
import info.solidsoft.gradle.pitest.CustomStrategyValidator
import info.solidsoft.gradle.pitest.GoalValidator
import info.solidsoft.gradle.pitest.PitestPlugin
import info.solidsoft.gradle.pitest.extension.ScmPitestPluginExtension
import info.solidsoft.gradle.pitest.scm.ChangeLogStrategy
import info.solidsoft.gradle.pitest.scm.ChangeLogStrategyFactory
import info.solidsoft.gradle.pitest.scm.ManagerService
import info.solidsoft.gradle.pitest.scm.PathToClassNameConverter
import info.solidsoft.gradle.pitest.scm.ScmConnection
import org.apache.maven.scm.manager.BasicScmManager
import org.apache.maven.scm.manager.ScmManager
import org.apache.maven.scm.provider.git.gitexe.GitExeScmProvider
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
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

    @InputFiles
    @Optional
    FileCollection managerClasspath

    ScmPitestTask() {
        description = "Run PIT analysis against SCM for java classes"
        group = PitestPlugin.PITEST_TASK_GROUP
        addValidator(new GoalValidator(this))
        addValidator(new CustomStrategyValidator(this))
        addValidator(new ConnectionTypeValidator(this))
    }

    @Override
    void exec() {
        ScmManager manager = getManager()
        ChangeLogStrategy strategy = new ChangeLogStrategyFactory(getScmRoot()).fromType(getGoal())
        String url = getConnectionUrl()
        def modifiedFilePaths = strategy.getModifiedFilenames(manager, getIncludes(), url)
        logger.info("FILES: $modifiedFilePaths")
        targetClasses = new PathToClassNameConverter(sourceDirs.collect {
            it.absolutePath
        }).convertPathNamesToClassName(modifiedFilePaths)
        args = createListOfAllArgumentsForPit()
        logger.info("$args")
        jvmArgs = (getMainProcessJvmArgs() ?: getJvmArgs())
        main = "org.pitest.mutationtest.commandline.MutationCoverageReport"
        classpath = getLaunchClasspath()
        super.exec()
    }

    private ScmManager getManager() {
        def currentClassloader = this.class.classLoader
        if (getManagerClasspath()) {
            def urls = getManagerClasspath().files.collect { it.toURI().toURL() } as URL[]
            def classloader = new URLClassLoader(urls, currentClassloader)
            return ManagerService.getInstance(classloader).getManager()
        }
        return ManagerService.getInstance(currentClassloader).getManager()
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
