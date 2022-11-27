package info.solidsoft.gradle.pitest.functional

import groovy.transform.CompileDynamic
import nebula.test.functional.ExecutionResult
import spock.util.environment.RestoreSystemProperties

import java.nio.file.Paths

@CompileDynamic
class AcceptanceTestsInSeparateSubprojectFunctionalSpec extends AbstractPitestFunctionalSpec {

    private String htmlReport = null

    void "should mutate production code in another subproject"() {
        given:
            copyResources("testProjects/multiproject", "")
        when:
            ExecutionResult result = runTasks('pitest')
        then:
            !result.standardError.contains("Build failed with an exception")
            !result.failure
            result.wasExecuted(':itest:pitest')
            result.getStandardOutput().contains('Generated 4 mutations Killed 3 (75%)')
    }

    @RestoreSystemProperties
    void "should aggregate report from subproject"() {
        given:
            copyResources("testProjects/multiproject", "")
        when:
            ExecutionResult result = runTasks('pitest', 'pitestReportAggregate', '-c', 'settings-report.gradle')
        then:
            !result.standardError.contains("Build failed with an exception")
            !result.failure
            result.wasExecuted(':shared:pitest')
            result.wasExecuted(':for-report:pitest')
            result.wasExecuted(':pitestReportAggregate')
        and:
            result.getStandardOutput().contains('Aggregating pitest reports')
            result.getStandardOutput().contains("Aggregated report ${getOutputReportPath()}")
            fileExists("build/reports/pitest/index.html")
        and:
            assertHtmlContains("<h1>Pit Test Coverage Report</h1>")
            assertHtmlContains("<th>Number of Classes</th>")
            assertHtmlContains("<th>Line Coverage</th>")
            assertHtmlContains("<th>Mutation Coverage</th>")
            assertHtmlContains("<th>Test Strength</th>")
            assertHtmlContains("<td>2</td>")
            assertHtmlContains("<td>95% ")
            assertHtmlContains("<td>40% ")
            assertHtmlContains("<td>50% ")
            assertHtmlContains("<td><a href=\"./pitest.sample.multimodule.forreport/index.html\">pitest.sample.multimodule.forreport</a></td>")
            assertHtmlContains("<td><a href=\"./pitest.sample.multimodule.shared/index.html\">pitest.sample.multimodule.shared</a></td>")
    }

    private void assertHtmlContains(String content) {
        if (htmlReport == null) {
            htmlReport = new File(projectDir, "build/reports/pitest/index.html").text
        }
        assert htmlReport.contains(content)
    }

    private String getOutputReportPath() {
        return Paths.get(projectDir.absolutePath, "build", "reports", "pitest", "index.html").toString()
    }

}
