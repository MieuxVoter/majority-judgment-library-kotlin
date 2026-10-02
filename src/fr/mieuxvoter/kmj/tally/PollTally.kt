package fr.mieuxvoter.kmj.tally

/**
 * Basic implementation of a [PollTallyInterface] that holds a list of [CandidateTallyInterface].
 *
 * @see [PollTallyInterface]
 */
data class PollTally(
    override val candidatesTallies: List<CandidateTallyInterface>,
) : PollTallyInterface
