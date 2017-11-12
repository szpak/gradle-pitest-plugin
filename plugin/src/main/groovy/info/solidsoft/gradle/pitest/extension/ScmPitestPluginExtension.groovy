package info.solidsoft.gradle.pitest.extension

import info.solidsoft.gradle.pitest.scm.ChangeLogStrategy
import info.solidsoft.gradle.pitest.scm.ScmConnection
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.util.ConfigureUtil

import java.util.logging.Logger

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
    String goal
    FileCollection managerClasspath

    ScmPitestPluginExtension(Project project) {
        super(project)
        scm = new ScmConnection()
    }

    void setScmRoot(String filePath) {
        this.scmRoot = new File(filePath)
    }

    void setScmRoot(File root) {
        this.scmRoot = root
    }

    def scm(Closure closure) {
        ConfigureUtil.configure(closure, scm)
    }

    void setManagerClasspath(FileCollection collection) {
        Logger.getLogger(ScmPitestPluginExtension.class.getName()).info("@@@@@@@@@@@@@SETTING MANAGERCLASSPATH: $collection.files")
        this.managerClasspath = collection
    }
}
