package info.solidsoft.gradle.pitest.functional

import nebula.test.IntegrationSpec

/**
 * TODO: Possible extensions:
 *  - Move functional tests to a separate sourceSet and not run them in every build
 *  - Add nice gradle.build builder
 *  - Add Connector clean up in tear down in IntegrationSpec
 *  - Add testing against latest nightly Gradle version?
 *
 *  - Add running with selected Gradle version - PR - https://github.com/nebula-plugins/nebula-test/pull/23
 *  - Allow to test with Gradle 2.x a plugin built with Gradle 1.x - classpath problem - https://github.com/nebula-plugins/nebula-test/issues/13 - ugly hacked locally
 */
class PitestPluginFunctional2Spec extends IntegrationSpec {

    def "should run mutation analysis"() {
        when:
            copyResources("testProjects/simple1", "")
        then:
            fileExists('build.gradle')
        when:
            def result = runTasksSuccessfully('pitest')
        then:
            result.wasExecuted(':pitest')
            result.getStandardOutput().contains('Generated 1 mutations Killed 1 (100%)')
    }
}
