package info.solidsoft.gradle.pitest

import org.gradle.api.Task
import org.gradle.api.reporting.ConfigurableReport
import org.gradle.api.reporting.DirectoryReport
import org.gradle.api.reporting.Report
import org.gradle.api.reporting.SingleFileReport
import org.gradle.api.reporting.internal.TaskGeneratedSingleDirectoryReport
import org.gradle.api.reporting.internal.TaskGeneratedSingleFileReport
import org.gradle.api.reporting.internal.TaskReportContainer

/**
 * @author Marcin Zajączkowski, 2013-12-16
 */
public class PitestReportsContainerImpl extends TaskReportContainer<Report> implements PitestReportsContainer {

    public PitestReportsContainerImpl(Task task) {
        super(ConfigurableReport.class, task)
        add(TaskGeneratedSingleDirectoryReport.class, "html", task, "index.html")
        add(TaskGeneratedSingleFileReport.class, "xml", task)
        add(TaskGeneratedSingleFileReport.class, "csv", task)
    }

    @Override
    public DirectoryReport getHtml() {
        return (DirectoryReport)getByName("html")
    }

    @Override
    public SingleFileReport getXml() {
        return (SingleFileReport)getByName("xml")
    }

    @Override
    public SingleFileReport getCsv() {
        return (SingleFileReport)getByName("csv")
    }
}
