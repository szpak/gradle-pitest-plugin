package info.solidsoft.gradle.pitest.task

import info.solidsoft.gradle.pitest.PitestPlugin
import info.solidsoft.gradle.pitest.scm.*
import info.solidsoft.gradle.pitest.scm.strategy.ChangeLogStrategy
import info.solidsoft.gradle.pitest.scm.strategy.factory.ChangeLogStrategyFactory
import info.solidsoft.gradle.pitest.validation.ScmPitestTaskValidator
import org.apache.maven.scm.manager.ScmManager
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
    }

    @Override
    void exec() {
        validateTaskConfiguration()
        ScmManager manager = getManager()
        ScmContext scmContext = createScmContext()
        ChangeLogStrategy strategy = new ChangeLogStrategyFactory(scmContext).fromType(getGoal())
        String url = getConnectionUrl()
        def modifiedFilePaths = strategy.getModifiedFilenames(manager, getIncludes(), url)
        def classNames = new PathToClassNameConverter(sourceDirs.collect {
            it.absolutePath
        }).convertPathNamesToClassName(modifiedFilePaths)
        targetClasses = classNames
        args = createListOfAllArgumentsForPit()
        jvmArgs = (getMainProcessJvmArgs() ?: getJvmArgs())
        main = "org.pitest.mutationtest.commandline.MutationCoverageReport"
        classpath = getLaunchClasspath()
        super.exec()
    }

    void validateTaskConfiguration() {
        new ScmPitestTaskValidator().validate(this)
    }

    private ScmContext createScmContext() {
        new ScmContext.Builder()
            .scmRoot(getScmRoot())
            .startVersionType(getStartVersionType())
            .startVersion(getStartVersion())
            .endVersionType(getEndVersionType())
            .endVersion(getEndVersion())
            .build()
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
