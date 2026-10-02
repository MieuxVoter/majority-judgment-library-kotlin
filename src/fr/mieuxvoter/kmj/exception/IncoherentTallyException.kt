package fr.mieuxvoter.kmj.exception
/**
 * Raised when the provided tally holds negative values, or disparate amounts of grades.
 */
internal class IncoherentTallyException : InvalidTallyException() {
    override val message: String
        get() = buildString {
            append("The provided tally holds negative values, or disparate amounts of grades.")
            if (!super.message.isNullOrBlank()) {
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
