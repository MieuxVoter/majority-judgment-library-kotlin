package fr.mieuxvoter.kmj.analysis

import com.ionspin.kotlin.bignum.integer.BigInteger
import fr.mieuxvoter.kmj.tally.CandidateTally
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals

class CandidateTallyAnalysisTest {

    data class CollectDecisiveGroupsTestDatum(
        val rule: String,
        val candidateTally: CandidateTally,
        val expectedGroups: List<ParticipantGroup>,
    ) {
        override fun toString(): String {
            return rule
        }
    }

    companion object {
        @JvmStatic
        fun getDecisiveGroupsData(): List<CollectDecisiveGroupsTestDatum> {
            return listOf(
                CollectDecisiveGroupsTestDatum(
                    rule = "Basic case",
                    candidateTally = CandidateTally(
                        gradesTallies = arrayOf(1, 2, 3, 4),
                    ),
                    expectedGroups = listOf(
                        ParticipantGroup(
                            size = BigInteger.fromInt(3),
                            grade = 2,
                            type = ParticipantGroup.Type.Median,
                        ),
                        ParticipantGroup(
                            size = BigInteger.fromInt(4),
                            grade = 3,
                            type = ParticipantGroup.Type.Adhesion,
                        ),
                        ParticipantGroup(
                            size = BigInteger.fromInt(3),
                            grade = 1,
                            type = ParticipantGroup.Type.Contestation,
                        ),
                        ParticipantGroup(
                            size = BigInteger.fromInt(1),
                            grade = 0,
                            type = ParticipantGroup.Type.Contestation,
                        ),
                    ),
                ),
                CollectDecisiveGroupsTestDatum(
                    rule = "With some zeroes (1)",
                    candidateTally = CandidateTally(
                        gradesTallies = arrayOf(7, 0, 0, 4),
                    ),
                    expectedGroups = listOf(
                        ParticipantGroup(
                            size = BigInteger.fromInt(7),
                            grade = 0,
                            type = ParticipantGroup.Type.Median,
                        ),
                        ParticipantGroup(
                            size = BigInteger.fromInt(4),
                            grade = 3,
                            type = ParticipantGroup.Type.Adhesion,
                        ),
                    ),
                ),
                CollectDecisiveGroupsTestDatum(
                    rule = "With some zeroes (2)",
                    candidateTally = CandidateTally(
                        gradesTallies = arrayOf(0, 1, 0, 4),
                    ),
                    expectedGroups = listOf(
                        ParticipantGroup(
                            size = BigInteger.fromInt(4),
                            grade = 3,
                            type = ParticipantGroup.Type.Median,
                        ),
                        ParticipantGroup(
                            size = BigInteger.fromInt(1),
                            grade = 1,
                            type = ParticipantGroup.Type.Contestation,
                        ),
                    ),
                ),
                CollectDecisiveGroupsTestDatum(
                    rule = "With some zeroes (3)",
                    candidateTally = CandidateTally(
                        gradesTallies = arrayOf(0, 0, 1, 0),
                    ),
                    expectedGroups = listOf(
                        ParticipantGroup(
                            size = BigInteger.fromInt(1),
                            grade = 2,
                            type = ParticipantGroup.Type.Median,
                        ),
                    ),
                ),
                CollectDecisiveGroupsTestDatum(
                    rule = "Empty tally",
                    candidateTally = CandidateTally(
                        gradesTallies = arrayOf(0, 0, 0, 0),
                    ),
                    expectedGroups = listOf(),
                ),
                CollectDecisiveGroupsTestDatum(
                    rule = "Void tally",
                    candidateTally = CandidateTally(
                        gradesTallies = emptyArray<BigInteger>(),
                    ),
                    expectedGroups = emptyList(),
                ),
            )
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getDecisiveGroupsData")
    fun collectDecisiveGroups(datum: CollectDecisiveGroupsTestDatum) {
        val analysis = CandidateTallyAnalysis(
            tally = datum.candidateTally,
            favorContestation = true,
            deep = true,
        )

        val actualDecisiveGroups = analysis.collectDecisiveGroups()

        assertEquals(
            expected = datum.expectedGroups.size,
            actual = actualDecisiveGroups.size,
            message = "Size is as expected",
        )
        
        datum.expectedGroups.zip(other = actualDecisiveGroups).forEach { (expectedGroup, actualGroup) ->
            assertEquals(
                expected = expectedGroup,
                actual = actualGroup,
                message = "Group is as expected",
            )
        }
    }
}