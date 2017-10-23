package pitest.test

internal data class Counter(private val value: Int) {
    fun isLessThan(otherCounter: Counter) = this.value < otherCounter.value
}
