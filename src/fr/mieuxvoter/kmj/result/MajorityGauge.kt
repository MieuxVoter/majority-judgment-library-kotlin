package fr.mieuxvoter.kmj.result

import com.ionspin.kotlin.bignum.integer.BigInteger

/**
 * Somewhat akin to the Majority Gauge described by Rida Laraki and Michel Balinski,
 * but with only the size of the biggest group out of the median grade.
 *
 * The sign tells which it is:
 * - positive for the adhesion group (higher than the median grade)
 * - negative for the contestation group (lower than the median grade)
 */
data class MajorityGauge(
    /**
     * Index of the median grade.
     */
    val medianGrade: Int,
    /**
     * Signed size of the biggest group out of the median group.
     */
    val biggestOutsideGroupSignedSize: BigInteger,
) : Comparable<MajorityGauge> {

    override fun compareTo(other: MajorityGauge): Int {
        if (medianGrade < other.medianGrade) {
            return -1
        }
        if (medianGrade > other.medianGrade) {
            return 1
        }
        if (biggestOutsideGroupSignedSize < other.biggestOutsideGroupSignedSize) {
            return -1
        }
        if (biggestOutsideGroupSignedSize > other.biggestOutsideGroupSignedSize) {
            return 1
        }
        return 0
    }

}
