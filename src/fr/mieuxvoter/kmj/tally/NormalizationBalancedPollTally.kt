package fr.mieuxvoter.kmj.tally

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.ionspin.kotlin.bignum.integer.BigInteger
import fr.mieuxvoter.kmj.extension.sumOf

/**
 * Balanced poll tally using a scaled normalization.
 * We normalize using the Least Common Multiple (LCM), to bypass IEE 754 shenanigans.
 *
 * This is handy when you have hundreds of candidates, and voters can't be expected to judge them all.
 *
 * Things to keep in mind:
 * - You should exclude candidates with low participation (skewed results)
 *   For example, a candidate having received only ONE "excellent" judgment will win,
 *   because this normalization will give them many "excellent" judgments.
 * - You MUST exclude candidates with no participation (division by zero)
 * - You should ensure, as best you can, that participation is somewhat equivalent across candidates
 */
data class NormalizationBalancedPollTally(
    override val candidatesTallies: List<CandidateTallyInterface>,
) : PollTallyInterface {

    init {
        val normalizationScale = guessLcmScale(candidatesTallies)
        require(normalizationScale >= 1) { "Normalization scale is too low." }

        candidatesTallies.forEach {
            normalizeCandidateTallyToScale(it, normalizationScale)
        }
    }

    fun normalizeCandidateTallyToScale(
        candidateTally: CandidateTallyInterface,
        scale: BigInteger,
    ) {
        if (candidateTally.gradesTallies.isEmpty()) {
            return
        }

        val initialScale = candidateTally.gradesTallies.sumOf { it }

        // TBD: maybe we should throw here instead of filling the lowest grade like pigs?
        if (initialScale == BigInteger.ZERO) {
            candidateTally.gradesTallies[0] = scale
            return
        }

        candidateTally.gradesTallies.forEachIndexed { gradeIndex, amount ->
            val normalizedAmount = BigDecimal.fromBigInteger(amount)
                .multiply(
                    other = BigDecimal.fromBigInteger(scale),
                )
                .divide(
                    other = BigDecimal.fromBigInteger(initialScale),
                    decimalMode = DecimalMode(
                        decimalPrecision = 15,
                        roundingMode = RoundingMode.ROUND_HALF_AWAY_FROM_ZERO,
                    ),
                )
                .roundToDigitPositionAfterDecimalPoint(
                    digitPosition = 0,
                    roundingMode = RoundingMode.ROUND_HALF_AWAY_FROM_ZERO,
                )
                .toBigInteger()

            candidateTally.gradesTallies[gradeIndex] = normalizedAmount
        }
    }

    fun guessLcmScale(tallies: List<CandidateTallyInterface>): BigInteger {
        var lcmScale = BigInteger.ONE

        tallies.forEach {
            val sum = it.gradesTallies.sumOf { it }
            if (sum > BigInteger.ZERO) {
                lcmScale = lcm(lcmScale, sum)
            }
        }

        return lcmScale
    }

    companion object {
        /**
         * Least Common Multiple.
         * https://en.wikipedia.org/wiki/Least_common_multiple
         *
         * Not added as extension since the BigInteger lib might add it itself at some point.
         * Perhaps we should, though, if only to be warned when they do, so we can remove this.
         */
        fun lcm(a: BigInteger, b: BigInteger): BigInteger {
            if (a.signum() == 0 || b.signum() == 0) {
                return BigInteger.ZERO
            }
            return a
                .multiply(other = b)
                .divide(other = a.gcd(other = b))
                .abs()
        }
    }
}
