package pl.droidsonroids.gradle.pitest.functional

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
        ExecutionResult result = runTasksSuccessfully(':itest:pitestRelease')
        then:
        result.wasExecuted(':itest:pitestRelease')
        result.standardOutput.contains('Generated 2 mutations Killed 2 (100%)')
    }

    @RestoreSystemProperties
    void "should aggregate report from subproject"() {
        given:
        copyResources("testProjects/multiproject", "")
        when:
        ExecutionResult result = runTasks('pitestRelease', 'pitestReportAggregate', '-c', 'settings-report.gradle')
        then:
        !result.standardError.contains("Build failed with an exception")
        !result.failure
        result.wasExecuted(':shared:pitestRelease')
        result.wasExecuted(':for-report:pitestRelease')
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
        assertHtmlContains("<td>2</td>")
        assertHtmlContains("<td>95% ")
        assertHtmlContains("<td>40% ")
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
