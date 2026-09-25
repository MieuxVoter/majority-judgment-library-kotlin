import com.ionspin.kotlin.bignum.integer.BigInteger

/**
 * Balanced poll tally using a static default grade.
 */
data class StaticDefaultBalancedPollTally(
    override val candidatesTallies: List<CandidateTallyInterface>,
    /**
     * The grade index to use asd default grade when balancing the candidate tallies.
     *
     * Usually, zero (0=REJECT) is a good choice as it incentivizes candidates to be clear.
     *
     * Grades are represented as numbers, as indices in a list.
     * Grades start from 0 ("worst" grade, most conservative) and go upwards.
     * Values out of the range of grades defined in the poll will yield errors.
     *
     * Example:
     *     0 == REJECT
     *     1 == PASSABLE
     *     2 == GOOD
     *     3 == EXCELLENT
     */
    val defaultGrade: Int = 0,
    /**
     * If this value is not specified, we will try our best to guess the amount of voters
     * from the candidate that received the greatest amount of judgments.
     */
    val amountOfVoters: BigInteger? = null,
) : PollTallyInterface, DefaultGradeTally() {

    // TBD: I want alternative constructors, but … ambiguity !  Perhaps with Union Types ?

//    constructor(
//        candidatesTallies: List<CandidateTallyInterface>,
//        defaultGrade: Int = 0,
//        amountOfVoters: Int? = null,
//    ) : this(
//        candidatesTallies = candidatesTallies,
//        defaultGrade = defaultGrade,
//        amountOfVoters = if (amountOfVoters != null) {
//            BigInteger.fromInt(amountOfVoters)
//        } else {
//            amountOfVoters
//        },
//    )
//    constructor(
//        candidatesTallies: List<CandidateTallyInterface>,
//        defaultGrade: Int = 0,
//        amountOfVoters: Long? = null,
//    ) : this(
//        candidatesTallies = candidatesTallies,
//        defaultGrade = defaultGrade,
//        amountOfVoters = if (amountOfVoters != null) {
//            BigInteger.fromLong(amountOfVoters)
//        } else {
//            amountOfVoters
//        },
//    )

    init {
        fillWithDefaultGrade(amountOfVoters)
    }

    override fun getDefaultGradeForCandidate(candidateTally: CandidateTallyInterface): Int {
        return defaultGrade
    }

}
