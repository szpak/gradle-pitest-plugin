package pitest.test


import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SimpleTest {
    @Test
    fun `can add`() {
        assertEquals(2, Simple().add(1,1))
    }
}
