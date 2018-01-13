package pitest.test


class SimpleTest {
    @Test
    fun `can add`() {
        assertEquals(2, Simple().add(1,1))
    }
    @Test
    fun `still can add`() {
        assertEquals(0, Simple().methodwithImplicitNullChecking())
    }
}
