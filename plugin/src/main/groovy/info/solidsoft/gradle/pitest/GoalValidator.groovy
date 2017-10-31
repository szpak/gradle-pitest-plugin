package info.solidsoft.gradle.pitest

import info.solidsoft.gradle.pitest.task.ScmPitestTask
import org.gradle.api.internal.TaskInternal
import org.gradle.api.internal.tasks.execution.TaskValidator

class GoalValidator implements TaskValidator {

    ScmPitestTask scmPitestTask

    GoalValidator(ScmPitestTask scmPitestTask) {
        this.scmPitestTask = scmPitestTask
    }

    @Override
    void validate(TaskInternal task, Collection<String> messages) {
        def supportedGoals = ['custom','localChanges','lastCommit']
        if (!supportedGoals.contains(scmPitestTask.getGoal())) {
            messages.add("Invalid goal specified, expected one of ${supportedGoals}, got ${scmPitestTask.getGoal()}".toString())
        }
    }
}
