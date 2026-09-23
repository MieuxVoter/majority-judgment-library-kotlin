
/**
 * A Basic implementation of a [PollTallyInterface] that reads from an array of [CandidateTallyInterface].
 */
data class PollTally(
    override val candidatesTallies: List<CandidateTallyInterface>,
) : PollTallyInterface
