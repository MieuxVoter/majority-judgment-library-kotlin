
/**
 * A deliberator takes in a poll's tally, which holds the amount of judgments of each grade received
 * by each candidate, and outputs that poll's result, which holds the computed rank of each candidate.
 *
 * Ranks start at 1 ("best"), and increment towards "worst".
 * Multiple candidates may share the same rank, in case of perfect equality.
 *
 * This is the main API of this library.
 *
 * See [MajorityJudgment] for an implementation.
 * One could implement other deliberators, such as:
 * - CentralJudgment
 * - UsualJudgment
 */
interface DeliberatorInterface {
    fun deliberate(tally: PollTallyInterface): PollResultInterface
}
