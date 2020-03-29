package pitest.test

class Calculator(private val dependency: Dependency) {

    suspend fun foo() =
        dependency.foo()
}