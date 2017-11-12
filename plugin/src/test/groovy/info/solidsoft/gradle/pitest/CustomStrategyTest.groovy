package info.solidsoft.gradle.pitest

import info.solidsoft.gradle.pitest.scm.ChangeLogException
import info.solidsoft.gradle.pitest.scm.CustomChangeLogStrategy
import org.apache.commons.lang.RandomStringUtils
import org.apache.maven.scm.ChangeFile
import org.apache.maven.scm.ChangeSet
import org.apache.maven.scm.ScmFileStatus
import org.apache.maven.scm.ScmResult
import org.apache.maven.scm.command.changelog.ChangeLogScmRequest
import org.apache.maven.scm.command.changelog.ChangeLogScmResult
import org.apache.maven.scm.command.changelog.ChangeLogSet
import org.apache.maven.scm.manager.NoSuchScmProviderException
import org.apache.maven.scm.manager.ScmManager
import org.apache.maven.scm.repository.ScmRepositoryException
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
            def message = 'Invalid url'
            managerMock.makeScmRepository(_) >> { throw new ScmRepositoryException(message) }
        when:
            strategy.getModifiedFilenames(managerMock, null, null)
        then:
            thrown ChangeLogException
    }

    def "should throw exception with invalid manager provider" () {
        given:
            CustomChangeLogStrategy strategy = basicCustomStrategyBuilder.build()
            managerMock.makeScmRepository(_) >> {throw new NoSuchScmProviderException()}
        when:
            strategy.getModifiedFilenames(managerMock, null, null)
        then:
            thrown ChangeLogException
    }

    def "should throw exception with invalid version type (startVersionType: '#startVersionType', endVersionType: '#endVersionType'" () {
        given:
            managerMock.makeScmRepository(_) >> null
            CustomChangeLogStrategy invalidStrategy = new CustomChangeLogStrategy.Builder()
                .fileSet(".")
                .startVersionType(startVersionType)
                .startVersion(RandomStringUtils.randomAlphanumeric(7))
                .endVersionType(endVersionType)
                .endVersion(RandomStringUtils.randomAlphanumeric(7))
                .build()
        when:
            invalidStrategy.getModifiedFilenames(managerMock, null, null)
        then:
            thrown ChangeLogException
        where:
            startVersionType | endVersionType
            null             | null
            null             | "tag"
            "tag"            | null
            "not supported"  | "tag"
            "revision"       | "not supported"
    }

    def "should throw exception on failure" () {
        given:
            CustomChangeLogStrategy strategy = basicCustomStrategyBuilder.build()
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

    def "should return correctly" () {
        given:
            CustomChangeLogStrategy strategy = new CustomChangeLogStrategy.Builder()
                .fileSet(path)
                .startVersionType("tag")
                .startVersion("Hello")
                .endVersionType("branch")
                .endVersion("World")
                .build()
            def include = ['added','modified']
            def changeSet = new ChangeSet()
            changeSet.setFiles(Collections.singleton(createChangeFile("custom",ScmFileStatus.ADDED)).toList())
            def changeLogSet = new ChangeLogSet(new Date(), new Date())
            changeLogSet.setChangeSets(Collections.singleton(changeSet).toList())
            def changelog = new ChangeLogScmResult(changeLogSet, new ScmResult(null,null,null,true))
        when:
            managerMock.makeScmRepository(_) >> null
            managerMock.changeLog(_ as ChangeLogScmRequest) >> changelog
            def result = strategy.getModifiedFilenames(managerMock, include as Set, null)
        then:
            result == ["$path/custom"]
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
