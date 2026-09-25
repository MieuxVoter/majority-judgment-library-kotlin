import com.ionspin.kotlin.bignum.integer.BigInteger

/**
 * Fill the missing judgments into the median grade of each candidate.
 * Useful when the candidates have received various amounts of votes and the median grade is considered a sane default.
 *
 * I can't fathom a practical case where I'd want to use this balancing strategy.
 * Usually, the static default grade or the normalization are better approaches, but here it is anyway.
 *
 * See [StaticDefaultBalancedPollTally].
 */
data class MedianDefaultBalancedPollTally(
    override val candidatesTallies: List<CandidateTallyInterface>,
    /**
     * If this value is not specified, we will try our best to guess the amount of voters in the poll
     * from the tally of the candidate that received the greatest amount of judgments.
     */
    val amountOfVoters: BigInteger? = null,
) : PollTallyInterface, DefaultGradeTally() {

    init {
        fillWithDefaultGrade(amountOfVoters)
    }

    override fun getDefaultGradeForCandidate(candidateTally: CandidateTallyInterface): Int {
        return CandidateTallyAnalysis(candidateTally).medianGrade
    }
}
