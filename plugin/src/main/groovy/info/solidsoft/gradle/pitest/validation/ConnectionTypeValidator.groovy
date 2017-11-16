package info.solidsoft.gradle.pitest.validation

import info.solidsoft.gradle.pitest.task.ScmPitestTask

class ConnectionTypeValidator implements TaskPropertyValidator<ScmPitestTask> {

    private final List<String> supportedConnectionTypes = ['connection', 'developerConnection']

    @Override
    void validateProperty(ScmPitestTask task, List<String> errorMessages) {
        if (!isSupportedConnectionType(task.getConnectionType())) {
            errorMessages.add("Invalid connectionType specified, expected one of $supportedConnectionTypes," +
                " got ${task.getConnectionType()}".toString())
        }
    }

    private boolean isSupportedConnectionType(String connectionType) {
        return supportedConnectionTypes.contains(connectionType)
    }
}
