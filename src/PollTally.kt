import tally.CandidateTallyInterface

/**
 * A Basic implementation of a [PollTallyInterface] that reads from an array of [tally.CandidateTallyInterface].
 */
data class PollTally(
    override val candidatesTallies: List<CandidateTallyInterface>,
) : PollTallyInterface
