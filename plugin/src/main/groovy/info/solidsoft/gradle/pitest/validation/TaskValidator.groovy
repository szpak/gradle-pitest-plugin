package info.solidsoft.gradle.pitest.validation

import org.gradle.api.Task

interface TaskValidator<T extends Task> {
    void validate(T task)
}
