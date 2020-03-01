package info.solidsoft.gradle.pitest.functional

import groovy.transform.CompileDynamic
import nebula.test.functional.ExecutionResult

@CompileDynamic
class PitestPluginGeneralFunctionalSpec extends AbstractPitestFunctionalSpec {

    void "enable PIT plugin when on classpath and pass plugin configuration to PIT"() {
        given:
        buildFile << getBasicGradlePitestConfig()
        buildFile << """
            buildscript {
                repositories {
                    maven { url "https://dl.bintray.com/szpak/pitest-plugins/" }
                }
                configurations.maybeCreate("pitest")
                dependencies {
                    pitest 'org.pitest.plugins:pitest-plugin-configuration-reporter-plugin:0.0.2'
                }
            }
            pitest {
                excludedClasses = []
                verbose = true
                pluginConfiguration = ['pitest-plugin-configuration-reporter-plugin.key1': 'value1',
                                       'pitest-plugin-configuration-reporter-plugin.key2': 'value2']
                features = ["-FANN", "+FINFIT(a[1] a[2])"]
                outputFormats = ['pluginConfigurationReporter']
            }
        """.stripIndent()

        and:
        writeHelloPitClass()
        writeHelloPitTest()

        when:
        ExecutionResult result = runTasksSuccessfully('pitest')

        then:
        result.wasExecuted(':pitest')

        and: 'plugin enabled'
        assertStdOutOrStdErrContainsGivenText(result, 'with the following plugin configuration')

        and: 'plugin parameters passed'
        result.getStandardOutput().contains('pitest-plugin-configuration-reporter-plugin.key1=value1')
        result.getStandardOutput().contains('pitest-plugin-configuration-reporter-plugin.key2=value2')

        and: 'built-in features passed'
        result.getStandardOutput().contains('-FANN')
        result.getStandardOutput().contains('+FINFIT')

        and: 'verbose output enabled'
        assertStdOutOrStdErrContainsGivenText(result, 'PIT >> FINE')
        //TODO: Add plugin features once available - https://github.com/hcoles/pitest-plugins/issues/2
    }

    void "use file to pass additional classpath to PIT if enabled"() {   //Needed? Already tested with ProjectBuilder in PitestTaskConfigurationSpec
        given:
        buildFile << getBasicGradlePitestConfig()
        buildFile << '''
            pitest {
                useClasspathFile = true
            }
        '''.stripIndent()

        and:
        writeHelloPitClass()
        writeHelloPitTest()

        when:
        ExecutionResult result = runTasksSuccessfully('pitest')

        then:
        result.wasExecuted(':pitest')
        result.getStandardOutput().contains('--classPathFile=')
        //TODO: Verify file name with regex
        !result.getStandardOutput().find('--classPath=')
    }

}
