import com.ionspin.kotlin.bignum.integer.BigInteger
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertContentEquals

class NormalizedBalancedPollTallyTest {

    data class ScaleNormalizationTestDatum(
        val rule: String,
        val tallies: List<List<Int>>,
        val expected: List<List<Int>>,
    ) {
        override fun toString(): String {
            return "${rule} — ${tallies} to ${expected}"
        }
    }

    companion object {
        @JvmStatic
        fun getData(): List<ScaleNormalizationTestDatum> {
            return listOf(
//                ScaleNormalizationTestDatum(
//                    rule = "Easy",
//                    tally = listOf(1, 2, 3, 4),
//                    expected = listOf(10, 20, 30, 40),
//                    scale = 100,
//                ),
//                ScaleNormalizationTestDatum(
//                    rule = "Hard one: Thirds",
//                    tally = listOf(1, 1, 1),
//                    expected = listOf(34, 33, 33), //  URGH
//                    scale = 100,
//                ),
                ScaleNormalizationTestDatum(
                    rule = "Simple case",
                    tallies = listOf(
                        listOf(1, 1, 0),
                        listOf(1, 1, 1),
                        listOf(2, 2, 2),
                    ),
                    expected = listOf(
                        listOf(3, 3, 0),
                        listOf(2, 2, 2),
                        listOf(2, 2, 2),
                    ),
                ),
                ScaleNormalizationTestDatum(
                    rule = "Primes",
                    tallies = listOf(
                        listOf(0, 1, 0),
                        listOf(1, 1, 0),
                        listOf(1, 1, 1),
                        listOf(0, 2, 2),
                        listOf(2, 2, 1),
                        listOf(2, 2, 2),
                        listOf(2, 2, 3),
                    ),
                    expected = listOf(
                        listOf(0, 420, 0),
                        listOf(210, 210, 0),
                        listOf(140, 140, 140),
                        listOf(0, 210, 210),
                        listOf(168, 168, 84),
                        listOf(140, 140, 140),
                        listOf(120, 120, 180),
                    ),
                ),
                ScaleNormalizationTestDatum(
                    // Not sure about this, maybe we should throw
                    rule = "Handle empty tallies gracefully",
                    tallies = listOf(
                        listOf(1, 2, 0),
                        listOf(0, 0, 0),
                    ),
                    expected = listOf(
                        listOf(1, 2, 0),
                        listOf(3, 0, 0),
                    ),
                ),
            )
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getData")
    fun testScaleNormalization(datum: ScaleNormalizationTestDatum) {
        val tally = NormalizedBalancedPollTally(
            candidatesTallies = datum.tallies.map {
                CandidateTally(gradesTallies = it)
            },
//            candidatesTallies = listOf(
//                CandidateTally(
//                    gradesTallies = datum.tally,
//                ),
//            ),
//            normalizationScale = BigInteger.fromInt(datum.scale),
        )

        tally.candidatesTallies.forEachIndexed { candidateIndex, candidateTally ->
            assertContentEquals(
                expected = datum.expected[candidateIndex].map { BigInteger.fromInt(it) }.toTypedArray(),
                actual = candidateTally.gradesTallies,
                message = "scaled tally of candidate ${candidateIndex} should match expectations",
            )
        }

//        assertEquals(
//            expected = BigInteger.fromInt(datum.scale),
//            actual = tally.candidatesTallies[0].gradesTallies.sumOf { it },
//            message = "sum must match the scale"
//        )
    }
}