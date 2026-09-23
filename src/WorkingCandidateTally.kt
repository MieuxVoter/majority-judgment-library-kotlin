import com.ionspin.kotlin.bignum.integer.BigInteger

internal class WorkingCandidateTally(
    override val gradesTallies: Array<BigInteger>,
) : CandidateTallyInterface {
    /**
     * Move all judgments that were fromGrade into intoGrade.
     * Used by the algorithm that computes the scalar merit for ranking.
     */
    fun moveJudgments(fromGrade: Int, intoGrade: Int) {
        this.gradesTallies[intoGrade] = this.gradesTallies[intoGrade] + this.gradesTallies[fromGrade]
        this.gradesTallies[fromGrade] = BigInteger.ZERO
    }
}
