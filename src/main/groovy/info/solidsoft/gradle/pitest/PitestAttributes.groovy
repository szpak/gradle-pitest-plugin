package info.solidsoft.gradle.pitest

import groovy.transform.CompileStatic
import org.gradle.api.attributes.Attribute

@CompileStatic
class PitestAttributes {

    static final Attribute<String> ARTIFACT_TYPE = Attribute.of("info.solidsoft.pitest.artifactType", String)
    static final String REPORT = "report"
    static final String SOURCES = "sources"
    static final String CLASSES = "classes"
    static final String MUTATION_FILE_NAME = "mutations.xml"
    static final String LINE_COVERAGE_FILE_NAME = "linecoverage.xml"

    private PitestAttributes() { }

}
