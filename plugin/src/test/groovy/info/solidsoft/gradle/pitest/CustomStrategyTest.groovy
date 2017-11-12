package info.solidsoft.gradle.pitest

import info.solidsoft.gradle.pitest.scm.ChangeLogException
import info.solidsoft.gradle.pitest.scm.CustomChangeLogStrategy
import org.apache.maven.scm.ChangeFile
import org.apache.maven.scm.ChangeSet
import org.apache.maven.scm.ScmFileStatus
import org.apache.maven.scm.ScmResult
import org.apache.maven.scm.command.changelog.ChangeLogScmRequest
import org.apache.maven.scm.command.changelog.ChangeLogScmResult
import org.apache.maven.scm.command.changelog.ChangeLogSet
import org.apache.maven.scm.manager.ScmManager
import spock.lang.Shared
import spock.lang.Specification

class CustomStrategyTest extends Specification {

    def managerMock = Mock(ScmManager)
    @Shared
    def path = System.getProperty("user.dir")
    def basicCustomStrategyBuilder = new CustomChangeLogStrategy.Builder().fileSet(path)

    def "should throw exception with invalid repository" () {
        given:
            CustomChangeLogStrategy strategy = basicCustomStrategyBuilder.build()
            managerMock.validateScmRepository(_) >> ['Invalid url']
        when:
            strategy.getModifiedFilenames(managerMock, null, null)
        then:
            thrown ChangeLogException
    }

    def "should throw exception on failure" () {
        given:
            CustomChangeLogStrategy strategy = basicCustomStrategyBuilder.build()
            managerMock.validateScmRepository(_) >> Collections.emptyList()
            managerMock.makeScmRepository(_) >> null
            managerMock.changeLog(_) >> createFailingChangeLog()
        when:
            strategy.getModifiedFilenames(managerMock,null,null)
        then:
            thrown ChangeLogException
    }

    def "should behave correctly on non matching include" () {
        given:
            CustomChangeLogStrategy strategy = new CustomChangeLogStrategy.Builder()
                .fileSet(path)
                .startVersionType("tag")
                .startVersion("Hello")
                .endVersionType("branch")
                .endVersion("World")
                .build()
            managerMock.validateScmRepository(_) >> Collections.emptyList()
            managerMock.makeScmRepository(_) >> null
            managerMock.changeLog(_ as ChangeLogScmRequest) >> createChangeLogResult(files)
        when:
            def result = strategy.getModifiedFilenames(managerMock, includes as Set, "")
        then:
            result == expectedFiles
        where:
            files                                                           | includes  | expectedFiles
            Arrays.asList(
                createChangeFile("custom",ScmFileStatus.ADDED),
                createChangeFile("local", ScmFileStatus.ADDED))       | ['added'] | ["$path/custom","$path/local"]
            Arrays.asList(
                createChangeFile("first"
                    , ScmFileStatus.ADDED),
                createChangeFile("second"
                    ,ScmFileStatus.MODIFIED),
                createChangeFile("third"
                    , ScmFileStatus.DELETED))                                | ['added','deleted','modified'] | ["$path/first",
                                                                                                                 "$path/second",
                                                                                                                 "$path/third"]

    }

    private static ChangeFile createChangeFile(String name, ScmFileStatus status) {
        ChangeFile result = new ChangeFile(name)
        result.setAction(status)
        return result
    }

    private static ChangeLogScmResult createChangeLogResult(List<ChangeFile> files) {
        def changeLogResult = new ScmResult(null,null,null, true)
        def changeSet = new ChangeSet()
        changeSet.setFiles(files)
        return new ChangeLogScmResult(new ChangeLogSet(Collections.singletonList(changeSet),new Date(), new Date()), changeLogResult)
    }

    private static ChangeLogScmResult createFailingChangeLog() {
        return new ChangeLogScmResult(null,null,null,false)
    }
}
