
/**
 * Raised when the provided tally does not hold the same amount of judgments for each proposal.
 */
class UnbalancedTallyException(
    val pollTally: PollTallyInterface? = null,
) : InvalidTallyException() {
    override val message: String
        get() = buildString {
            append("The provided poll tally is unbalanced, as some candidates received more judgments than others.")
            append("\n")
            if (pollTally != null) {
                append("Here's a breakdown of the candidate tallies and the amount of judgments they each received:")
                append("\n")
                pollTally.candidatesTallies.forEach { candidateTally ->
                    append("- ${candidateTally.gradesTallies.map { it.toString() }}")
                    append(" : ")
                    append("${candidateTally.gradesTallies.sumOf { it }} judgments")
                    append("\n")
                }
                append("\n")
            }
            append("We provide some balancing strategies you may use:")
            append("\n")
            // TODO: document other balancing strategies here
            append("- StaticDefaultBalancedPollTally\n")
            append("\n")
            if (!super.message.isNullOrEmpty()) {
                append("\n")
                append(super.message)
            }
        }

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun hashCode(): Int {
        return System.identityHashCode(this)
    }
}
