import com.ionspin.kotlin.bignum.integer.BigInteger

/**
 * Holds the amounts of judgments received per grade by a single candidate.
 * This is also known as the Merit Profile of a candidate.
 */
interface CandidateTallyInterface {
    /**
     * Amounts of judgments received per grade, ordered from "worst" grade to "best" grade.
     *
     * Those are BigIntegers because of our optional but guaranteed perfect LCM-based normalization
     * that can be used as a balancing strategy when candidates did not receive the same amount of judgments.
     */
    val gradesTallies: Array<BigInteger>
}
