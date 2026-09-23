import com.ionspin.kotlin.bignum.integer.BigInteger

/**
 * Result of the Majority Judgment deliberation (ranking) for a single candidate.
 * The most important property in this class is the [rank].
 * You will also find useful values in the [analysis] if you want to display the merit profiles for example.
 */
data class CandidateResult(

    /**
     * Index of the candidate, as submitted in the [PollTallyInterface.candidatesTallies] list.
     * Each candidate therefore has a unique index, and indices start at 0.
     * This property is useful when iterating over [PollResult.candidateResultsRanked].
     */
    val index: Int,

    /**
     * The rank of the candidate in the Majority Judgment ranking
     * Rank starts at 1 ("best" candidate), and goes upwards. Multiple candidates may receive the same
     * rank, in the extreme case where they have the exact same merit profile.
     */
    val rank: Int,

    /**
     * Scalar majority merit of the candidate.
     * This merit is isomorphic with Majority Judgment ranking.  A higher merit means a better rank.
     *
     * It grows quite big quite fast (multiple dozens of decimals), so it needs to be a BigInteger.
     *
     * A merit of zero means that the candidate received the "worst" grade from all the voters.
     * The maximum possible merit depends on the amount of grades and voters.
     *
     * Its distribution is not affine over all possible and ranked merit profiles of the same shape and total.
     * The distribution looks like a finite-fractal series of sigmoids.
     * This is why we also approximate the affine scalar merit, see [affineMerit].
     */
    val merit: BigInteger,

    /**
     * This is the scalar [merit] of this candidate, divided by the sum of the merits of the candidates in the poll.
     * This is therefore always a value between 0 and 1, inclusive.
     *
     * The sum of the relative merits of all the candidates of a poll is always 1.
     */
    val relativeMerit: Double,

    /**
     * Approximation of the merit from absolute rank, normalized.
     *
     * The "absolute rank" of a merit profile is the rank of that merit profile in the holistic ranking of all possible
     * merit profiles of the same shape and total.  That ranking is usually way too big to be computable.
     *
     * This value is very experimental and might be subject to changes as our fitting evolves and gets more precise.
     * Best not rely on this 'til it's stable.
     *
     * This merit's distribution is quasi-affine over all possible merit profiles of the same shape and total.
     * It is an approximation because its exact value is quickly un-computable as the amount of judges grows.
     * Contrary to the BigInteger [merit], this value is always between 0 and 1 (inclusive).
     */
    val affineMerit: Double,

    /**
     * This is a value between 0 and 1 (inclusive).
     * The sum of the relative affine merits of all the candidates of a poll is always 1.
     */
    val relativeAffineMerit: Double,

    /**
     * Provides more data about the candidate tally, such as the median grade.
     */
    val analysis: CandidateTallyAnalysis,
)
