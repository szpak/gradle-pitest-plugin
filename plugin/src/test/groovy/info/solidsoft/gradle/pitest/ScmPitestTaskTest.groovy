package info.solidsoft.gradle.pitest

import info.solidsoft.gradle.pitest.scm.ChangeLogStrategy
import info.solidsoft.gradle.pitest.scm.ScmConnection
import info.solidsoft.gradle.pitest.task.ScmPitestTask
import org.apache.maven.scm.manager.ScmManager
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification;

class ScmPitestTaskTest extends Specification {
    def "should" () {
        given:
            def project = ProjectBuilder.builder().build()
            project.tasks.create(PluginConstants.SCM_PITEST_TASK_NAME, ScmPitestTask)
            ScmPitestTask task = project.tasks[PluginConstants.SCM_PITEST_TASK_NAME] as ScmPitestTask
            task.setStrategy(new ChangeLogStrategy() {
                @Override
                List<String> getModifiedFilenames(ScmManager manager, Set<String> includes, String url) {
                    return Collections.emptyList()
                }
            })
            ScmConnection connection = new ScmConnection()
            connection.setConnection("https://sample.com")
            task.setConnectionType("connection")
            task.setScm(new ScmConnection())
            task.setScmRoot(new File("."))
            task.exec()
        expect:
            task.targetClasses == null
    }
}
