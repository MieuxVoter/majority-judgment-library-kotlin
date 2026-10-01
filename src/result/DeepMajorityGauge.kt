package result

/**
 * This holds all the information we need to rank the candidates, without approximation.
 * We can simply compare candidates' deep majority gauges to rank them.
 */
data class DeepMajorityGauge(
    val gauges: List<MajorityGauge>,
): Comparable<DeepMajorityGauge> {

    override fun compareTo(other: DeepMajorityGauge): Int {
        require(this.gauges.size == other.gauges.size) {
            "Deep Majority Gauges must be of the same shape to be comparable."
        }

        this.gauges.forEachIndexed { index, thisGauge ->
            val otherGauge = other.gauges[index]
            val comparison = thisGauge.compareTo(otherGauge)
            if (comparison != 0) {
                return comparison
            }
        }

        return 0
    }

}