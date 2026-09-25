import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.ionspin.kotlin.bignum.integer.BigInteger

/**
 * Deliberate (i.e. rank candidates) using Majority Judgment.
 *
 * Majority Judgment ranks candidates by their median grade.  A higher grade ranks better.
 * When two candidates share the same median grade, as it often happens,
 * it gives reason to the largest group of people that did not give the median grade.
 *
 * We do not use any of the easy-to-grasp algorithms for Majority Judgment here, because they scale poorly.
 * Instead, this algorithm is scalar-merit-based, for performance (and possible future parallelization).
 * It essentially runs in:
 * - constant time over the amount of voters
 * - (n⋅log(n)) time over the amount of candidates
 * - (n) time over the amount of available grades (usually no more than seven)
 *
 * Each candidate gets a scalar majority merit from its merit profile.  A higher merit ranks better.
 *
 * https://en.wikipedia.org/wiki/Majority_judgment
 */
class MajorityJudgment(
    /**
     * In edge cases of equality between adhesion and contestation groups, we have to choose
     * to favor one over the other.  This is inherent to median-based systems.
     * The default behavior is to favor contestation in those cases, since one could argue that people feel stronger
     * about contestation than they feel about adhesion.  It's reasonable, but debatable ; so, you get to decide.
     */
    private val favorContestation: Boolean = true,
) : DeliberatorInterface {

    override fun deliberate(tally: PollTallyInterface): PollResultInterface {
        checkTally(tally)

        val amountOfCandidates = tally.candidatesTallies.size
        val amountOfGrades = if (tally.candidatesTallies.isNotEmpty()) {
            countGrades(tally.candidatesTallies[0])
        } else {
            7 // dummy value, it's never going to be used anyway if there are no candidates
        }
        val amountOfJudges = if (tally.candidatesTallies.isNotEmpty()) {
            countJudgments(tally.candidatesTallies[0])
        } else {
            BigInteger.ZERO
        }

        // I Compute the scalar majority merit of each candidate
        val merits = tally.candidatesTallies.map { computeMerit(tally = it, favorContestation) }
        val sumOfMerits = merits.sumOf { it }

        // II.a Compute the (maximum!) merit a 100% EXCELLENT candidate would get
        //      This is not used in ranking ; it's used to compute the "merit from absolute rank" approximation
        val maxMerit = if (tally.candidatesTallies.isNotEmpty()) {
            val amountOfGrades = countGrades(tally.candidatesTallies[0])
            val amountOfJudges = countJudgments(tally.candidatesTallies[0])
            val bestMeritProfile = Array(size = amountOfGrades) { BigInteger.ZERO }
            bestMeritProfile[bestMeritProfile.size - 1] = amountOfJudges
            computeMerit(
                tally = CandidateTally(gradesTallies = bestMeritProfile),
                favorContestation = favorContestation,
            )
        } else {
            BigInteger.ONE // a dummy is OK, it's never going to be used
        }

        // II.b Approximate the scalar "merit from absolute rank" of each candidate (Affine Merit)
        //      This (optional) value may be used to compute a naive proportional representation
        val affineMerits = List(amountOfCandidates) { candidateIndex ->
            adjustMeritToAffine(
                merits[candidateIndex],
                maxMerit,
                amountOfJudges,
                amountOfGrades,
            )
        }

        // III. Prepare the results for each candidate (except the rank)
        val candidateResults = Array(size = amountOfCandidates) { candidateIndex ->
            val candidateTally = tally.candidatesTallies[candidateIndex]
            val analysis = CandidateTallyAnalysis(candidateTally, this.favorContestation)

            CandidateResult(
                index = candidateIndex,
                rank = 0, // computed later, after the sorting step
                merit = merits[candidateIndex],
                relativeMerit = if (sumOfMerits != BigInteger.ZERO) {
                    BigDecimal.fromBigInteger(merits[candidateIndex])
                        .divide(
                            other = BigDecimal.fromBigInteger(sumOfMerits),
                            decimalMode = DecimalMode(
                                decimalPrecision = 15,
                                roundingMode = RoundingMode.ROUND_HALF_AWAY_FROM_ZERO,
                            ),
                        )
                        .doubleValue(exactRequired = false)
                } else {
                    0.0
                },
                affineMerit = affineMerits[candidateIndex],
                relativeAffineMerit = if (maxMerit != BigInteger.ZERO) {
                    BigDecimal.fromDouble(affineMerits[candidateIndex])
                        .divide(
                            other = BigDecimal.fromBigInteger(maxMerit),
                            decimalMode = DecimalMode(
                                decimalPrecision = 15,
                                roundingMode = RoundingMode.ROUND_HALF_AWAY_FROM_ZERO,
                            ),
                        )
                        .doubleValue(exactRequired = false)
                } else {
                    0.0
                },
                analysis = analysis,
            )
        }

        // IV. Sort the candidates by their majority merit — higher merit ranks better
        val candidateResultsSorted = candidateResults.clone()
        candidateResultsSorted.sortWith(
            Comparator { pA: CandidateResult, pB: CandidateResult ->
                pB.merit.compare(other = pA.merit)
            }
        )

        // V. Attribute a rank to each candidate
        var increasingRank = 1
        for (candidateIndexSorted in 0..<amountOfCandidates) {
            val candidateResult = candidateResultsSorted[candidateIndexSorted]

            // Rule: In case of perfect equality, multiple candidates may share the same rank
            var actualRank = increasingRank
            if (candidateIndexSorted > 0) {
                val candidateResultBefore = candidateResultsSorted[candidateIndexSorted - 1]
                if (candidateResult.merit == candidateResultBefore.merit) {
                    actualRank = candidateResultBefore.rank
                }
            }

            // A copy incurs a small cost, but immutability is nice to have (@Stable)
            val rankedCandidateResult = candidateResult.copy(rank = actualRank)
            candidateResultsSorted[candidateIndexSorted] = rankedCandidateResult
            candidateResults[candidateResult.index] = rankedCandidateResult

            increasingRank += 1
        }

        // VI. All done !
        return PollResult(
            candidateResults = candidateResults.asList(),
            candidateResultsRanked = candidateResultsSorted.asList(),
        )
    }

    private fun checkTally(tally: PollTallyInterface) {
        if (!isTallyCoherent(tally)) {
            throw IncoherentTallyException()
        }
        if (!isTallyBalanced(tally)) {
            throw UnbalancedTallyException(tally)
        }
    }

    private fun isTallyCoherent(tally: PollTallyInterface): Boolean {
        // Rule: reject negative values, they are absurd as tallies and will break the ranking algorithm
        for (candidateTally in tally.candidatesTallies) {
            if (candidateTally.gradesTallies.any { it < BigInteger.ZERO }) {
                return false
            }
        }
        // Rule: all the merit profiles must have the same shape, i.e. the same amount of grades
        if (tally.candidatesTallies.isNotEmpty()) {
            val amountOfGrades = countGrades(tally.candidatesTallies[0])
            if (tally.candidatesTallies.any { countGrades(tally = it) != amountOfGrades }) {
                return false
            }
        }

        return true
    }

    private fun isTallyBalanced(tally: PollTallyInterface): Boolean {
        // Rule: all merit profiles must hold the same total amount of judgments — use a balancing strategy beforehand!
        if (tally.candidatesTallies.isNotEmpty()) {
            val amountOfJudges = countJudgments(tally.candidatesTallies[0])
            if (tally.candidatesTallies.any { countJudgments(tally = it) != amountOfJudges }) {
                return false
            }
        }

        return true
    }

    private fun countGrades(tally: CandidateTallyInterface): Int {
        return tally.gradesTallies.size
    }

    private fun countJudgments(tally: CandidateTallyInterface): BigInteger {
        return tally.gradesTallies.sumOf { it }
    }

    /**
     * Computes a scalar majority merit for a given merit profile.
     * This merit is isomorphic with MJ ranking and is used for ranking. (bigger is better)
     * Such a scalar merit is also handy for deriving a proportional representation for example.
     * For lack of a better name, I call this a "signed base" technique.  The base is the amount of judges.
     * Of course, we represent the merit in base 10, but intrinsically it's base amountOfJudges.
     */
    private fun computeMerit(
        tally: CandidateTallyInterface,
        favorContestation: Boolean = true,
    ): BigInteger {
        val analysis = CandidateTallyAnalysis(tally, favorContestation)

        val amountOfGrades = countGrades(tally)
        val amountOfJudges = countJudgments(tally)

        val currentTally = WorkingCandidateTally(gradesTallies = tally.gradesTallies.copyOf())

        var merit = BigInteger.fromInt(analysis.medianGrade)
        var cursorGrade = analysis.medianGrade
        var minProcessedGrade = cursorGrade
        var maxProcessedGrade = cursorGrade

        repeat(times = amountOfGrades - 1) {
            merit *= amountOfJudges

            if (analysis.secondMedianGroupSize == BigInteger.ZERO) {
                return@repeat // a.k.a. continue
            }

            if (analysis.secondMedianGroupSign > 0) {
                cursorGrade = maxProcessedGrade + 1
                maxProcessedGrade = cursorGrade
            } else {
                cursorGrade = minProcessedGrade - 1
                minProcessedGrade = cursorGrade
            }

            merit += analysis.secondMedianGroupSize * analysis.secondMedianGroupSign

            currentTally.moveJudgments(fromGrade = analysis.medianGrade, intoGrade = cursorGrade)
            analysis.reanalyze(currentTally, favorContestation)
        }

        return merit
    }

    /**
     * This method is NOT used in ranking, but helps compute yet another scalar merit for a given merit profile.
     * Such a scalar merit may be handy for deriving a proportional representation for example.
     * This method adjusts the scalar merit to make its distribution quasi-affine over all possible merit profiles.
     * See study/output_30_0.png
     * You can safely pretend that this does not exist, since it is NOT used in ranking.
     */
    private fun adjustMeritToAffine(
        merit: BigInteger,
        maxMerit: BigInteger,
        amountOfJudges: BigInteger,
        amountOfGrades: Int,
    ): Double {
        if (maxMerit == BigInteger.ZERO) {
            return 0.0
        }

        val meritNormalized = BigDecimal.fromBigInteger(merit)
            .divide(
                other = BigDecimal.fromBigInteger(maxMerit),
                decimalMode = DecimalMode(
                    decimalPrecision = 15,
                    roundingMode = RoundingMode.ROUND_HALF_AWAY_FROM_ZERO,
                ),
            )
            .doubleValue(exactRequired = false)

        val rankNormalized = MeritToAbsoluteRankModel().apply(
            meritNormalized,
            amountOfGrades,
            // Our model has only been fitted against data up to 30 judges anyway, so loss of precision here is ok
            amountOfJudges.longValue(exactRequired = false),
        )

        return 1.0 - rankNormalized
    }
}
