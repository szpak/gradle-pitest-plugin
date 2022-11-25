package info.solidsoft.gradle.pitest

import groovy.transform.CompileStatic
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

import javax.inject.Inject

@CompileStatic
class ReportAggregatorProperties {

    final Property<Integer> aggregatedTestStrengthThreshold
    final Property<Integer> aggregatedMutationThreshold
    final Property<Integer> aggregatedMaxSurviving

    @Inject
    ReportAggregatorProperties(ObjectFactory objects) {
        aggregatedTestStrengthThreshold = objects.property(Integer)
        aggregatedMutationThreshold = objects.property(Integer)
        aggregatedMaxSurviving = objects.property(Integer)
    }

}
