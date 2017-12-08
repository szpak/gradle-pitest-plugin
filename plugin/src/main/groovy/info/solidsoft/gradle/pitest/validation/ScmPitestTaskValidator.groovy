package info.solidsoft.gradle.pitest.validation

import info.solidsoft.gradle.pitest.exception.PitestTaskValidationException
import info.solidsoft.gradle.pitest.task.ScmPitestTask

class ScmPitestTaskValidator extends AbstractTaskValidator<ScmPitestTask> {

    List<TaskPropertyValidator> propertyValidators = new ArrayList<>()

    ScmPitestTaskValidator() {
        errorMessages = new ArrayList<>()
        propertyValidators.addAll(new ConnectionTypeValidator(),
            new GoalValidator(),
            new CustomStrategyValidator())
    }

    @Override
    void validate(ScmPitestTask task) {
        propertyValidators.each {
            propertyValidator ->
                propertyValidator.validateProperty(task, errorMessages)
        }
        if (!errorMessages.isEmpty()) {
            throw new PitestTaskValidationException(errorMessages.first())
        }
    }
}
