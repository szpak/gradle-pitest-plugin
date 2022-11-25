package info.solidsoft.gradle.pitest

import groovy.transform.CompileStatic
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

import javax.inject.Inject

@CompileStatic
class ReportAggregatorProperties {

    final Property<Integer> testStrengthThreshold
    final Property<Integer> mutationThreshold
    final Property<Integer> maxSurviving

    @Inject
    ReportAggregatorProperties(ObjectFactory objects) {
        testStrengthThreshold = objects.property(Integer)
        mutationThreshold = objects.property(Integer)
        maxSurviving = objects.property(Integer)
    }

}
