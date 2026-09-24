import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class CollectedPollTallyTest {
    @Test
    fun defaultValueIsZero() {

        val tally = CollectedPollTally(
            amountOfCandidates = 2,
            amountOfGrades = 3,
        )

        assertEquals(
            expected = 2,
            actual = tally.candidatesTallies.size,
            message = "Correct amount of candidates",
        )
        assertContentEquals(
            expected = arrayOf(0, 0, 0),
            actual = tally.candidatesTallies[0].gradesTallies.map { it.intValue() }.toTypedArray(),
            message = "Merit profile of the first candidate"
        )
        assertContentEquals(
            expected = arrayOf(0, 0, 0),
            actual = tally.candidatesTallies[1].gradesTallies.map { it.intValue() }.toTypedArray(),
            message = "Merit profile of the second candidate"
        )
    }

    @Test
    fun collect() {

        val tally = CollectedPollTally(
            amountOfCandidates = 3,
            amountOfGrades = 5,
        )

        // First voter
        tally.collect(0, 0)
        tally.collect(1, 1)
        tally.collect(2, 4)

        // Second voter
        tally.collect(0, 3)
        tally.collect(1, 4)
        tally.collect(2, 0)

        // Third voter
        tally.collect(0, 1)
        tally.collect(1, 2)
        tally.collect(2, 1)

        // Fourth voter
        tally.collect(0, 1)
        tally.collect(1, 2)
        tally.collect(2, 1)

        // Merit profiles
        // 1, 2, 0, 1, 0
        // 0, 1, 2, 0, 1
        // 1, 2, 0, 0, 1

        assertEquals(
            expected = 3,
            actual = tally.candidatesTallies.size,
            message = "Correct amount of candidates",
        )
        assertContentEquals(
            expected = arrayOf(1, 2, 0, 1, 0),
            actual = tally.candidatesTallies[0].gradesTallies.map { it.intValue() }.toTypedArray(),
            message = "Merit profile of the first candidate"
        )
        assertContentEquals(
            expected = arrayOf(0, 1, 2, 0, 1),
            actual = tally.candidatesTallies[1].gradesTallies.map { it.intValue() }.toTypedArray(),
            message = "Merit profile of the second candidate"
        )
        assertContentEquals(
            expected = arrayOf(1, 2, 0, 0, 1),
            actual = tally.candidatesTallies[2].gradesTallies.map { it.intValue() }.toTypedArray(),
            message = "Merit profile of the third candidate"
        )
    }

}