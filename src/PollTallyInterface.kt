
/**
 * The main input of the Majority Judgment library.
 * This holds the tallies of the judgments cast by the voters, per candidate and per grade.
 *
 * See [PollTally] for a usable implementation.
 */
interface PollTallyInterface {
    val candidatesTallies: List<CandidateTallyInterface>
}
