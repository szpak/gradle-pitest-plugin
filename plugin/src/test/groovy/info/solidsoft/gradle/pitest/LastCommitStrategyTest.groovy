package info.solidsoft.gradle.pitest

import info.solidsoft.gradle.pitest.exception.ChangeLogException
import info.solidsoft.gradle.pitest.scm.strategy.LastCommitStrategy
import org.apache.maven.scm.ChangeFile
import org.apache.maven.scm.ChangeSet
import org.apache.maven.scm.ScmFileStatus
import org.apache.maven.scm.ScmResult
import org.apache.maven.scm.command.changelog.ChangeLogScmRequest
import org.apache.maven.scm.command.changelog.ChangeLogScmResult
import org.apache.maven.scm.command.changelog.ChangeLogSet
import org.apache.maven.scm.manager.ScmManager
import spock.lang.Specification

class LastCommitStrategyTest extends Specification {

    def managerMock = Mock(ScmManager)
    def path = System.getProperty("user.dir")

    def "should throw exception when failure" () {
        given:
            LastCommitStrategy strategy = new LastCommitStrategy(path)
            managerMock.validateScmRepository(_) >> Collections.emptyList()
            managerMock.makeScmRepository(_) >> null
            managerMock.changeLog(_ as ChangeLogScmRequest) >> createFailingChangeLog()
        when:
            strategy.getModifiedFilenames(managerMock, null, null)
        then:
            thrown ChangeLogException
    }

    def "should throw exception with invalid url" () {
        given:
            LastCommitStrategy strategy = new LastCommitStrategy(path)
            managerMock.validateScmRepository(_) >> ["Invalid url"]
        when:
            strategy.getModifiedFilenames(managerMock, null, null)
        then:
            thrown ChangeLogException
    }

    def "should return empty collection when no last commit present" () {
        given:
            managerMock.validateScmRepository(_) >> Collections.emptyList()
            managerMock.changeLog(_ as ChangeLogScmRequest) >> createChangeLogResult(Collections.emptyList())
            LastCommitStrategy strategy = new LastCommitStrategy(path)
        when:
            def result = strategy.getModifiedFilenames(managerMock, ['added'] as Set, null)
        then:
            result.isEmpty()
    }

    def "should return last commit files with prefix" () {
        given:
            LastCommitStrategy strategy = new LastCommitStrategy(path)
            managerMock.validateScmRepository(_) >> Collections.emptyList()
            managerMock.changeLog(_ as ChangeLogScmRequest) >> createChangeLogResult(
                Arrays.asList(createChangeSet(Arrays.asList(createChangeFile("custom",ScmFileStatus.ADDED))),
                createChangeSet(Arrays.asList(createChangeFile("precedingCommit", ScmFileStatus.ADDED)))))
        when:
            def result = strategy.getModifiedFilenames(managerMock, ['added'] as Set, null)
        then:
            result == ["$path/custom"]
    }

    private ChangeLogScmResult createChangeLogResult(List<ChangeSet> changeSets) {
        ChangeLogSet changeLogSet = new ChangeLogSet(changeSets, new Date(), new Date())
        ChangeLogScmResult result = new ChangeLogScmResult(changeLogSet, new ScmResult(null,
            null, null, true))
        return result
    }

    private ChangeSet createChangeSet(List<ChangeFile> files) {
        def result = new ChangeSet()
        result.setFiles(files)
        return result
    }

    private ChangeFile createChangeFile(String name, ScmFileStatus status) {
        def result = new ChangeFile(name)
        result.setAction(status)
        return result
    }

    private static ChangeLogScmResult createFailingChangeLog() {
        return new ChangeLogScmResult(null,null,null,false)
    }
}
