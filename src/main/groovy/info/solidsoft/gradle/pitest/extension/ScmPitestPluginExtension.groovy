package info.solidsoft.gradle.pitest.extension

import info.solidsoft.gradle.pitest.scm.ChangeLogStrategy
import info.solidsoft.gradle.pitest.scm.ScmConnection
import org.gradle.api.Project

class ScmPitestPluginExtension extends PitestPluginExtension {

    private static final NAME = "scmPitest"

    ScmConnection scm
    String connectionType
    Set<String> includes
    def goal

    ScmPitestPluginExtension(Project project) {
        super(project)
        scm = new ScmConnection()
    }

    @Override
    static String getName() {
        return NAME
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
}
