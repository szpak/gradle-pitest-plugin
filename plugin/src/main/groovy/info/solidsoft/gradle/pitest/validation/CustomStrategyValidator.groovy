package info.solidsoft.gradle.pitest.validation

import info.solidsoft.gradle.pitest.task.ScmPitestTask

class CustomStrategyValidator implements TaskPropertyValidator<ScmPitestTask> {

    private final List<String> supportedVersionTypes = ['tag', 'revision', 'branch']

    @Override
    void validateProperty(ScmPitestTask task, List<String> messages) {
        if (task.getGoal() == 'custom') {
            validateVersionPresence(task, messages)
            validateTypes(task, messages)
        }
    }

    void validateTypes(ScmPitestTask scmPitestTask, List<String> messages) {
        if (!isSupportedVersionType(scmPitestTask.getStartVersionType())) {
            messages.add("Invalid start version type, expected one of $supportedVersionTypes, got ${scmPitestTask.getStartVersionType()}".toString())
        }
        if (!isSupportedVersionType(scmPitestTask.getEndVersionType())) {
            messages.add("Invalid end version type, expected one of $supportedVersionTypes, got ${scmPitestTask.getEndVersionType()}".toString())
        }
    }

    private boolean isSupportedVersionType(String versionType) {
        return supportedVersionTypes.contains(versionType)
    }

    private void validateVersionPresence(ScmPitestTask scmPitestTask, Collection<String> messages) {
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
