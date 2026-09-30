import com.ionspin.kotlin.bignum.integer.BigInteger

/**
 * Mutable candidate tally that we use internally in the recursive (deep) analysis.
 */
internal class WorkingCandidateTally(
    override val gradesTallies: Array<BigInteger>,
) : CandidateTallyInterface {
    /**
     * Move all judgments that were fromGrade into intoGrade.
     * Used by the algorithm that computes the deep majority gauge for ranking candidates.
     */
    fun moveJudgments(fromGrade: Int, intoGrade: Int) {
        if (fromGrade == intoGrade) return
        this.gradesTallies[intoGrade] = this.gradesTallies[intoGrade] + this.gradesTallies[fromGrade]
        this.gradesTallies[fromGrade] = BigInteger.ZERO
    }
}
