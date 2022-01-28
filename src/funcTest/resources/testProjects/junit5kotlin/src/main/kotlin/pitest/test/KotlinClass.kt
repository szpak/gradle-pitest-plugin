package pitest.test

class Simple {
    fun add(a: Int, b: Int) = a + b
    fun methodwithImplicitNullChecking(): Int {
        return add(Integer.decode("0"), 0)
    }
}
