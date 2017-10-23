package pitest.test

import org.junit.Test

import org.assertj.core.api.Assertions.assertThat

class CounterTest {

    @Test
    fun returnsTrueWhenComparedToASmallerAngle() {
        assertThat(Counter(1).isLessThan(Counter(2))).isTrue()
    }

    @Test
    fun returnsFalseWhenComparedToAGreaterAngle() {
        assertThat(Counter(2).isLessThan(Counter(1))).isFalse()
    }

    @Test
    fun returnsFalseWhenComparedToAngleWithSameDegrees() {
        assertThat(Counter(1).isLessThan(Counter(1))).isFalse()
    }
}
