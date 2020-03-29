package pitest.test

class DependencyImpl : Dependency {

    override suspend fun foo() {
        println("Do nothing")
    }
}