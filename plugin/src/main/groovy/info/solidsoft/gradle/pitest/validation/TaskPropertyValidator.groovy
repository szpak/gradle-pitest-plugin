package info.solidsoft.gradle.pitest.validation

import org.gradle.api.Task

interface TaskPropertyValidator<T extends Task> {
    void validateProperty(T task, List<String> errorMessages)
}
