package pitest.test

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class CalculatorTest {

    @MockK
    lateinit var dependency: Dependency

    @InjectMockKs
    private lateinit var calculator: Calculator

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }


    @Test
    fun onSum_returnCorrect() = runBlockingTest {
        // Uncomment to pitest succeed
//        coEvery {
//            dependency.foo()
//        } returns Unit

        calculator.foo()

        coVerify(exactly = 1) { dependency.foo() }
        confirmVerified()
    }
}
