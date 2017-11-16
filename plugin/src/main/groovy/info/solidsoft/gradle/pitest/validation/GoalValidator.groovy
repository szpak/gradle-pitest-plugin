package info.solidsoft.gradle.pitest.validation

import info.solidsoft.gradle.pitest.task.ScmPitestTask

class GoalValidator implements TaskPropertyValidator<ScmPitestTask> {

    private final List<String> supportedGoals = ['custom', 'localChanges', 'lastCommit']

    @Override
    void validateProperty(ScmPitestTask task, List<String> messages) {
        if (!isSupportedGoal(task.getGoal())) {
            messages.add("Invalid goal specified, expected one of ${supportedGoals}, got ${task.getGoal()}".toString())
        }
    }

    private boolean isSupportedGoal(String goal) {
        return supportedGoals.contains(goal)
    }
}
