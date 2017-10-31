package info.solidsoft.gradle.pitest

import info.solidsoft.gradle.pitest.task.ScmPitestTask
import org.gradle.api.internal.TaskInternal
import org.gradle.api.internal.tasks.execution.TaskValidator

class ConnectionTypeValidator implements TaskValidator {

    private ScmPitestTask scmPitestTask

    ConnectionTypeValidator(ScmPitestTask scmPitestTask) {
        this.scmPitestTask = scmPitestTask
    }

    @Override
    void validate(TaskInternal task, Collection<String> messages) {
        def supportedConnectionTypes = ['connection','developerConnection']
        if (!supportedConnectionTypes.contains(scmPitestTask.getConnectionType())) {
            messages.add("Invalid connectionType specified, expected one of $supportedConnectionTypes," +
                " got ${scmPitestTask.getConnectionType()}".toString())
        }
    }
}
