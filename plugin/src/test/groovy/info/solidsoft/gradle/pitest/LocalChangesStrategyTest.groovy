package info.solidsoft.gradle.pitest

import info.solidsoft.gradle.pitest.scm.ChangeLogException
import info.solidsoft.gradle.pitest.scm.LocalChangesStrategy
import org.apache.maven.scm.ScmFile
import org.apache.maven.scm.ScmFileStatus
import org.apache.maven.scm.command.status.StatusScmResult
import org.apache.maven.scm.manager.NoSuchScmProviderException
import org.apache.maven.scm.manager.ScmManager
import org.apache.maven.scm.repository.ScmRepositoryException
import spock.lang.Specification

class LocalChangesStrategyTest extends Specification {

    def managerMock = Mock(ScmManager)
    def path = System.getProperty("user.dir")

    def "should throw exception when failure" () {
        given:
            def strategy = new LocalChangesStrategy(path)
            managerMock.validateScmRepository(_) >> ['Error occured']
            managerMock.status(_,_) >> createFailingChangeLog()
        when:
            strategy.getModifiedFilenames(managerMock, null, null)
        then:
            thrown ChangeLogException
    }

    def "should return local changes files" () {
        given:
            def strategy = new LocalChangesStrategy(path)
            managerMock.validateScmRepository(_) >> Collections.emptyList()
            managerMock.makeScmRepository(_) >> null
            managerMock.status(_,_) >> createScmResult(Arrays.asList(
                new ScmFile("custom",ScmFileStatus.ADDED)
            ))
        when:
            def result = strategy.getModifiedFilenames(managerMock,['added'] as Set, null)
        then:
            result == ["$path/custom"]
    }

    private static StatusScmResult createFailingChangeLog() {
        return new StatusScmResult(null,null,null,false)
    }

    private static StatusScmResult createScmResult(List<ScmFile> changeFiles) {
        return new StatusScmResult(changeFiles, new StatusScmResult(null,null,null,true))
    }
}
