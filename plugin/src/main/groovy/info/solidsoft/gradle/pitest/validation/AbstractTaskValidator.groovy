package info.solidsoft.gradle.pitest.validation

import org.gradle.api.Task

abstract class AbstractTaskValidator<T extends Task> implements TaskValidator<T> {

    protected List<String> errorMessages

}
