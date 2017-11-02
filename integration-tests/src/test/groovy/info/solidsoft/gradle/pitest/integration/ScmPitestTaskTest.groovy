package info.solidsoft.gradle.pitest.integration

import info.solidsoft.gradle.pitest.PluginConstants
import info.solidsoft.gradle.pitest.scm.ChangeLogStrategy
import info.solidsoft.gradle.pitest.scm.ScmConnection
import info.solidsoft.gradle.pitest.task.ScmPitestTask
import org.gradle.api.file.FileCollection
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification;

class ScmPitestTaskTest extends Specification {

    private ScmPitestTask task

    def setup() {
        def project = ProjectBuilder.builder().build()
        project.tasks.create(PluginConstants.SCM_PITEST_TASK_NAME, ScmPitestTask)
        task = project.tasks[PluginConstants.SCM_PITEST_TASK_NAME] as ScmPitestTask
    }

    def "should" () {
        given:
            def fileCollectionMock = Mock(FileCollection)
            fileCollectionMock.getFiles() >> Collections.emptySet()
        when:
            task.setAdditionalClasspath(fileCollectionMock)
        then:
            task.getAdditionalClasspath().getFiles().isEmpty()

    }

    def "should register targetClasses" () {
        given:
            def changeLogMock = Mock(ChangeLogStrategy)
            changeLogMock.getModifiedFilenames(_, _, _) >> Collections.emptyList()
        when:
            task.setStrategy(changeLogMock)
            task.setTargetClasses(changeLogMock.getModifiedFilenames(null,null,null) as Set)
        then:
            task.targetClasses.isEmpty()
    }
}
