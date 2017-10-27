package info.solidsoft.gradle.pitest

class ScmPitestTaskScmConnectionTest extends BasicProjectBuilderSpec implements WithScmPitestTaskInitialization {
    def "should configure scm correctly" () {
        given:
            project.scmPitest.scm {
                url = "https://hello-world.com"
                connection = "https://production.com"
                developerConnection = "https://developer.com"
                tag = "HEAD"
            }
        expect:
            scmPitestTask.scm.url == "https://hello-world.com"
            scmPitestTask.scm.connection == "https://production.com"
            scmPitestTask.scm.developerConnection == "https://developer.com"
            scmPitestTask.scm.tag == "HEAD"
    }

    def "should throw exception with invalid property provided" () {
        when:
            project.scmPitest.scm {
                unknown = "unknown"
                url = "url"
                connection = "connection"
                developerConnection = "developer"
                tag = "tag"
            }
        then:
            thrown MissingPropertyException
    }

    def "should not throw exception with missing properties" () {
        when:
            project.scmPitest.scm {
                url = "url"
                tag = "tag"
            }
        then:
            notThrown(MissingPropertyException.class)
            scmPitestTask.scm.url == "url"
            scmPitestTask.scm.tag == "tag"
    }

    def "should configure scm from project property if already defined" () {
        given:
            project.scm = {

            }
        expect:
            scmPitestTask.scm.url == "url"
            scmPitestTask.scm.connection == "connection"
    }
}
