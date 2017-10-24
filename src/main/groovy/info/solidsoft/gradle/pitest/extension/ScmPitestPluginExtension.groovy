package info.solidsoft.gradle.pitest.extension

import info.solidsoft.gradle.pitest.scm.ChangeLogStrategy
import info.solidsoft.gradle.pitest.scm.ScmConnection
import org.gradle.api.Project
import org.gradle.api.tasks.Nested
import org.gradle.util.ConfigureUtil

class ScmPitestPluginExtension extends PitestPluginExtension {

    public static final EXTENSION_NAME = "scmPitest"

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
        this.goal = value
    }

    void setGoal(String type) {
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
