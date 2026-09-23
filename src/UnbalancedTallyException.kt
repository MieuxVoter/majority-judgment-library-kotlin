
/**
 * Raised when the provided tally does not hold the same amount of judgments for each proposal.
 */
class UnbalancedTallyException : InvalidTallyException() {
    override val message: String
        get() = buildString {
            append("The provided tally is unbalanced, as some candidates received more judgments than others.")
            // TODO: document various balancing strategies here
            if (!super.message.isNullOrEmpty()) {
                append("\n")
                append(super.message)
            }
        }
}
