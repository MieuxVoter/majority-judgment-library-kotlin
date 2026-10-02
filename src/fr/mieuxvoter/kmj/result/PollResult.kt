package fr.mieuxvoter.kmj.result

/**
 * See [PollResultInterface].
 */
data class PollResult(
    override val candidateResults: List<CandidateResult>,
    override val candidateResultsRanked: List<CandidateResult>,
) : PollResultInterface
