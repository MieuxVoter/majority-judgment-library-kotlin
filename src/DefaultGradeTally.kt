import com.ionspin.kotlin.bignum.integer.BigInteger

/**
 * Fill the missing judgments into the grade defined by [getDefaultGradeForCandidate].
 * This is an abstract class to dry code between static default grade and median default grade.
 */
abstract class DefaultGradeTally: PollTallyInterface {

    /**
     * Override this to choose the default grade for a given proposal.
     */
    protected abstract fun getDefaultGradeForCandidate(candidateTally: CandidateTallyInterface): Int

    protected fun guessAmountOfVoters(): BigInteger {
        return candidatesTallies.maxOfOrNull { it.gradesTallies.sumOf { it } } ?: BigInteger.ZERO
    }

    protected fun fillWithDefaultGrade(
        amountOfVoters: BigInteger? = null,
    ) {
        val expectedAmountOfVoters = amountOfVoters ?: guessAmountOfVoters()
        candidatesTallies.forEachIndexed { candidateIndex, tally ->
            val defaultGrade = getDefaultGradeForCandidate(candidateTally = tally)
            val amountOfJudgments = tally.gradesTallies.sumOf { it }
            val missingAmount = expectedAmountOfVoters - amountOfJudgments
            require(missingAmount >= BigInteger.ZERO) {
                "Candidate at index ${candidateIndex} has received ${amountOfJudgments} judgments" +
                        " but you specified that the poll had ${expectedAmountOfVoters} voters, which is less."
            }
            if (missingAmount > BigInteger.ZERO) {
                tally.gradesTallies[defaultGrade] += missingAmount
            }
        }
    }
}
