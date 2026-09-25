import com.ionspin.kotlin.bignum.integer.BigInteger
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.fail

class MajorityJudgmentTest {

    data class MajorityJudgmentTestDatum(
        val rule: String,
        val meritProfiles: List<List<Int>>,
        val staticDefaultGrade: Int? = null,
        val expectedRanks: Array<Int>? = null,
        val expectedException: KClass<*>? = null,
    ) {
        override fun toString(): String {
            return rule
        }
    }

    // To use the getData data provider method into @MethodSource it has to be static.
    companion object {
        @JvmStatic
        fun getData(): List<MajorityJudgmentTestDatum> {
            return listOf(
                MajorityJudgmentTestDatum(
                    rule = "Rejects unbalanced tallies",
                    meritProfiles = listOf(
                        listOf(1, 2, 3),
                        listOf(1, 0, 0),
                    ),
                    expectedException = UnbalancedTallyException::class,
                ),
                MajorityJudgmentTestDatum(
                    rule = "Rejects tallies with different amounts of grades",
                    meritProfiles = listOf(
                        listOf(1, 2),
                        listOf(1, 0, 2),
                    ),
                    expectedException = IncoherentTallyException::class,
                ),
                MajorityJudgmentTestDatum(
                    rule = "Rejects tallies with negative amounts of grades",
                    meritProfiles = listOf(
                        listOf(10, 20, -7),
                        listOf(20, 10, -7),
                    ),
                    expectedException = IncoherentTallyException::class,
                ),
                MajorityJudgmentTestDatum(
                    rule = "A single candidate is allowed",
                    meritProfiles = listOf(
                        listOf(0, 1, 2, 3, 4, 5, 6),
                    ),
                    expectedRanks = arrayOf(
                        1,
                    ),
                ),
                MajorityJudgmentTestDatum(
                    rule = "Simple case with two candidates and three grades",
                    meritProfiles = listOf(
                        listOf(11, 2, 7),
                        listOf(10, 4, 6),
                    ),
                    expectedRanks = arrayOf(
                        2,
                        1,
                    ),
                ),
                MajorityJudgmentTestDatum(
                    rule = "Simple case with three candidates and five grades",
                    meritProfiles = listOf(
                        listOf(30, 30, 30, 30, 30),
                        listOf(20, 35, 35, 40, 20),
                        listOf(25, 35, 30, 40, 20),
                    ),
                    expectedRanks = arrayOf(
                        3,
                        1,
                        2,
                    ),
                ),
                MajorityJudgmentTestDatum(
                    rule = "Millions of voters are allowed",
                    meritProfiles = listOf(
                        listOf(10_000_000, 20_000_000, 15_111_222),
                        listOf(30_000_000, 10_111_000, 5_000_222),
                        listOf(5_000_000, 15_111_000, 25_000_222),
                    ),
                    expectedRanks = arrayOf(
                        2,
                        3,
                        1,
                    ),
                ),
                MajorityJudgmentTestDatum(
                    rule = "Perfect equality is allowed",
                    meritProfiles = listOf(
                        listOf(300, 300, 300, 300, 300),
                        listOf(150, 150, 0, 600, 600),
                        listOf(150, 150, 0, 600, 600),
                        listOf(350, 250, 350, 250, 300),
                    ),
                    expectedRanks = arrayOf(
                        3,
                        1,
                        1,
                        4,
                    ),
                ),
                MajorityJudgmentTestDatum(
                    rule = "Balance with default static grade",
                    meritProfiles = listOf(
                        listOf(0, 0, 3),
                        listOf(0, 2, 1),
                        listOf(0, 0, 1),
                        listOf(0, 1, 0),
                        listOf(0, 0, 0),
                    ),
                    staticDefaultGrade = 0,
                    expectedRanks = arrayOf(
                        1,
                        2,
                        3,
                        4,
                        5,
                    ),
                ),

            )
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getData")
    fun testMajorityJudgment(datum: MajorityJudgmentTestDatum) {
        val mj = MajorityJudgment()
        var pollTally: PollTallyInterface = PollTally(
            candidatesTallies = datum.meritProfiles.map {
                CandidateTally(gradesTallies = it)
            },
        )

        if (datum.staticDefaultGrade != null) {
            pollTally = StaticDefaultBalancedPollTally(
                candidatesTallies = pollTally.candidatesTallies,
                defaultGrade = datum.staticDefaultGrade,
            )
        }

        if (datum.expectedException != null) {
            // I tried a bunch of assertion utils, nothing worked for me but this.
            // It's not pretty, but it does the job — feel free to improve.  :)

            val throwable: Throwable? = try {
                mj.deliberate(pollTally)
            } catch (e: Throwable) {
                e
            } as? Throwable

            if (throwable == null) {
                fail("Expected an exception ${datum.expectedException}")
            } else {
                assertEquals(
                    actual = throwable::class.java.typeName,
                    expected = datum.expectedException.java.typeName,
                )
            }

            return
        }

        val pollResult = mj.deliberate(pollTally)

        if (datum.expectedRanks != null) {
            assertContentEquals(
                expected = datum.expectedRanks,
                actual = pollResult.candidateResults.map { it.rank }.toTypedArray(),
                message = "ranks are not as expected",
            )
        }
    }

    @Test
    fun testReadmeExample() {
        val mj = MajorityJudgment()
        val tally = PollTally(
            candidatesTallies = listOf(
                CandidateTally(gradesTallies = arrayOf(4, 5, 2, 1, 3, 1, 2)),
                CandidateTally(gradesTallies = arrayOf(3, 6, 2, 2, 2, 1, 2)),
                CandidateTally(gradesTallies = arrayOf(5, 3, 0, 2, 3, 2, 3)),
            ),
        )
        val result = mj.deliberate(tally)

        println(result.candidateResults.map { it.rank }) // [ 2, 3, 1 ]
        println(result.candidateResultsRanked.map { it.index }) // [ 2, 0, 1 ]
        
        assertContentEquals(
            expected = arrayOf(2, 3, 1),
            actual = result.candidateResults.map { it.rank }.toTypedArray(),
            message = "Correct ranks",
        )
        assertContentEquals(
            expected = arrayOf(2, 0, 1),
            actual = result.candidateResultsRanked.map { it.index }.toTypedArray(),
            message = "Correct indices",
        )
    }

    @Test
    fun testSimpleExampleWithBigIntegers() {
        val meritProfiles = listOf(
            // Arancini
            CandidateTally(
                gradesTallies = arrayOf(
                    BigInteger.fromInt(3),
                    BigInteger.fromInt(3),
                    BigInteger.fromInt(3),
                    BigInteger.fromInt(3),
                    BigInteger.fromInt(3),
                )
            ),
            // Burger
            CandidateTally(
                gradesTallies = arrayOf(
                    BigInteger.fromInt(2),
                    BigInteger.fromInt(2),
                    BigInteger.fromInt(4),
                    BigInteger.fromInt(6),
                    BigInteger.fromInt(1),
                )
            ),
            // Chips
            CandidateTally(
                gradesTallies = arrayOf(
                    BigInteger.fromInt(0),
                    BigInteger.fromInt(0),
                    BigInteger.fromInt(10),
                    BigInteger.fromInt(5),
                    BigInteger.fromInt(0),
                )
            ),
        )
        val pollTally = PollTally(meritProfiles)

        val mj = MajorityJudgment()
        val pollResult = mj.deliberate(pollTally)

//        print(pollResult.proposalResults.map { it.index })
//        print(pollResult.proposalResults.map { it.rank })
//        print(pollResult.proposalResultsRanked.map { it.index })
//        print(pollResult.proposalResultsRanked.map { it.rank })

        assertEquals(
            expected = meritProfiles.size,
            actual = pollResult.candidateResults.size,
            message = "The size of the results is the same as the amount of proposals",
        )

        assertEquals(
            expected = 3,
            actual = pollResult.candidateResults[0].rank,
            message = "The proposals are ranked (proposal A has rank 3)",
        )

        assertEquals(
            expected = 1,
            actual = pollResult.candidateResults[1].rank,
            message = "The proposals are ranked (proposal B has rank 1)",
        )

        assertEquals(
            expected = 2,
            actual = pollResult.candidateResults[2].rank,
            message = "The proposals are ranked (proposal C has rank 2)",
        )

        assertEquals(
            expected = meritProfiles.size,
            actual = pollResult.candidateResultsRanked.size,
            message = "The size of the ranked results is the same as the amount of proposals",
        )

        assertEquals(
            expected = 1,
            actual = pollResult.candidateResultsRanked[0].rank,
            message = "The ranked proposals are ranked (rank 1)",
        )

        assertEquals(
            expected = 2,
            actual = pollResult.candidateResultsRanked[1].rank,
            message = "The ranked proposals are ranked (rank 2)",
        )

        assertEquals(
            expected = 3,
            actual = pollResult.candidateResultsRanked[2].rank,
            message = "The ranked proposals are ranked (rank 3)",
        )

        assertEquals(
            expected = 1,
            actual = pollResult.candidateResultsRanked[0].index,
            message = "The ranked proposals are ranked (proposal B has rank 1)",
        )

        assertEquals(
            expected = 2,
            actual = pollResult.candidateResultsRanked[1].index,
            message = "The ranked proposals are ranked (proposal C has rank 2)",
        )

        assertEquals(
            expected = 0,
            actual = pollResult.candidateResultsRanked[2].index,
            message = "The ranked proposals are ranked (proposal A has rank 3)",
        )
    }

}
