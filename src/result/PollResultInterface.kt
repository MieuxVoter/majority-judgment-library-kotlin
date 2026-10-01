package result

import MajorityJudgment

/**
 * The main output of this Majority Judgment library.
 * This is the output of [MajorityJudgment.deliberate].
 */
interface PollResultInterface {
    /**
     * List of [CandidateResult], in the order the [tally.CandidateTally]s were initially submitted
     * in the input [tally.PollTallyInterface.candidatesTallies].
     *
     * You can get the rank of each candidate by accessing [CandidateResult.rank].
     */
    val candidateResults: List<CandidateResult>

    /**
     * List of [CandidateResult] ordered by rank.
     * In case of rank equality, the initial order is preserved.
     *
     * You can get the original index of each candidate by accessing [CandidateResult.index].
     */
    val candidateResultsRanked: List<CandidateResult>
}
