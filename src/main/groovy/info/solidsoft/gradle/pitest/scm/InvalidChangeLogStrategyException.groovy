package info.solidsoft.gradle.pitest.scm

class InvalidChangeLogStrategyException extends Exception {
    InvalidChangeLogStrategyException(String message) {
        super(message)
    }

    InvalidChangeLogStrategyException(String message, Throwable throwable) {
        super(message, throwable)
    }
}
