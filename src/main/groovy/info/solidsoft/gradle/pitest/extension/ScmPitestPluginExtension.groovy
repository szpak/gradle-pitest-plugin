package info.solidsoft.gradle.pitest.extension

import info.solidsoft.gradle.pitest.scm.ChangeLogStrategy
import info.solidsoft.gradle.pitest.scm.ScmConnection
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil

import java.util.logging.Logger

class ScmPitestPluginExtension extends PitestPluginExtension {

    public static final EXTENSION_NAME = "scmPitest"
    private static final Logger LOG = Logger.getLogger(ScmPitestPluginExtension.class.typeName)

    File scmRoot
    ScmConnection scm
    String connectionType
    Set<String> includes
    String startVersionType
    String startVersion
    String endVersionType
    String endVersion
    def goal

    ScmPitestPluginExtension(Project project) {
        super(project)
        LOG.info("Scm extension configured")
        scm = new ScmConnection()
    }

    void setScm(ScmConnection value) {
        this.scm = value
    }

    void setConnectionType(String value) {
        this.connectionType = value
    }

    void setIncludes(Collection<String> value) {
        this.includes = new HashSet<>(value)
    }

    void setGoal(ChangeLogStrategy value) {
        LOG.info("Has value: '${value.getClass()}'")
        this.goal = value
    }

    void setGoal(String type) {
        LOG.info("Goal has value: $type")
        this.goal = type
    }

    void setScmRoot(String scmRoot) {
        this.scmRoot = new File(scmRoot)
    }

    void setScmRoot(File value) {
        this.scmRoot = value
    }

    def scm(Closure closure) {
        ConfigureUtil.configure(closure, scm)
    }
}
