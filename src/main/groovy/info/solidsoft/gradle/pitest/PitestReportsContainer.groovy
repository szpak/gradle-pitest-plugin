package info.solidsoft.gradle.pitest

import org.gradle.api.reporting.DirectoryReport
import org.gradle.api.reporting.Report
import org.gradle.api.reporting.ReportContainer
import org.gradle.api.reporting.SingleFileReport

/**
 * @author Marcin Zajączkowski, 2013-12-16
 */
public interface PitestReportsContainer extends ReportContainer<Report> {

    DirectoryReport getHtml()

    SingleFileReport getXml()

    SingleFileReport getCsv()
}
