package info.solidsoft.gradle.pitest

import info.solidsoft.gradle.pitest.task.ScmPitestTask
import org.gradle.api.internal.TaskInternal
import org.gradle.api.internal.tasks.execution.TaskValidator

class CustomStrategyValidator implements TaskValidator {

    ScmPitestTask scmPitestTask

    CustomStrategyValidator(ScmPitestTask scmPitestTask) {
        this.scmPitestTask = scmPitestTask
    }

    @Override
    void validate(TaskInternal task, Collection<String> messages) {
        if (scmPitestTask.getGoal() == 'custom') {
            validatePresence(messages)
            validateTypes(messages)
        }
    }

    void validateTypes(Collection<String> messages) {
        def supportedVersionTypes = ['tag','revision','branch']
        if (!supportedVersionTypes.contains(scmPitestTask.getStartVersionType())) {
            messages.add("Invalid start version type, expected one of $supportedVersionTypes, got ${scmPitestTask.getStartVersionType()}".toString())
        }
        if (!supportedVersionTypes.contains(scmPitestTask.getEndVersionType())) {
            messages.add("Invalid end version type, expected one of $supportedVersionTypes, got ${scmPitestTask.getEndVersionType()}".toString())
        }
    }

    private void validatePresence(Collection<String> messages) {
        if (scmPitestTask.getStartVersion() == null) {
            messages.add("Start version for 'custom' goal must be specified")
        }
        if (scmPitestTask.getStartVersionType() == null) {
            messages.add("Start version type for 'custom' goal must be specified")
        }
        if (scmPitestTask.getEndVersion() == null) {
            messages.add("End version for 'custom' goal must be specified")
        }
        if (scmPitestTask.getEndVersionType() == null) {
            messages.add("End version type for 'custom' goal must be specified")
        }
    }
}
