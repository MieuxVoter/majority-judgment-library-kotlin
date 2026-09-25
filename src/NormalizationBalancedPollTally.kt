import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.ionspin.kotlin.bignum.integer.BigInteger

/**
 * Balanced poll tally using a scaled normalization.
 * We normalize using the Least Common Multiple (LCM).
 *
 * Make sure to only process candidates tallies with at least one judgment.
 */
data class NormalizationBalancedPollTally(
    override val candidatesTallies: List<CandidateTallyInterface>,

    /**
     * PARAMETER DISABLED FOR NOW
     * Would love this parameter, but it's too much of a hassle for now.
     * Look into Sainte Lague, etc.
     * Therefore, only using the LCM for now ; it's simpler.
     * --------------------------
     *
     * You may use a bigger value here, but you probably should not use a lower value,
     * as it will drastically augment the loss of precision, and make the results worthless.
     *
     * Anyway, we've hardcoded a lower bound at 1 for this value.
     * Best not use a value below 100, unless you know what you are doing.
     *
     * 100 is for percentages.
     * 1000 is for permillages, etc.
     */
    //val normalizationScale: BigInteger = BigInteger.fromInt(100),

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
        val initialScale = candidateTally.gradesTallies.sumOf { it }

        // TBD: maybe we should throw here instead of filling the lowest grade like pigs
        if (initialScale == BigInteger.ZERO && candidateTally.gradesTallies.isNotEmpty()) {
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
         * http://en.wikipedia.org/wiki/Least_common_multiple
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
