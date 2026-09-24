import com.ionspin.kotlin.bignum.integer.BigInteger

/**
 * A tally that can be filled judgment by judgment, using the [collect] method.
 * This class is handy if you have access to the ballots and want to tally them.
 */
class CollectedPollTally(
    val amountOfCandidates: Int,
    val amountOfGrades: Int,
) : PollTallyInterface {

    override val candidatesTallies: List<CandidateTallyInterface> = List(size = amountOfCandidates) {
        CandidateTally(
            gradesTallies = Array(size = amountOfGrades) { BigInteger.ZERO },
        )
    }

    /**
     * Records that a single judgment of [grade] was given to [candidate].
     * The [grade] index goes from the "worst" grade (0), to the "best" grade ([amountOfGrades]-1).
     */
    fun collect(candidate: Int, grade: Int) {
        require(candidate >= 0) { "Candidate index must be ≥ zero." }
        require(candidate < amountOfCandidates) { "Candidate index is too high." }
        require(grade >= 0) { "Grade index must be ≥ zero." }
        require(grade < amountOfGrades) { "Grade index is too high." }

        candidatesTallies[candidate].gradesTallies[grade] += BigInteger.ONE
    }

}
