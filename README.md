# Majority Judgment Library for Kotlin Multiplatform

[![MIT](https://img.shields.io/github/license/MieuxVoter/majority-judgment-library-kotlin?style=for-the-badge)](./LICENSE)
[![Release](https://img.shields.io/github/v/release/MieuxVoter/majority-judgment-library-kotlin?sort=semver&style=for-the-badge)](https://github.com/MieuxVoter/majority-judgment-library-kotlin/releases)
[![Build Status](https://img.shields.io/github/actions/workflow/status/MieuxVoter/majority-judgment-library-kotlin/maven.yml?style=for-the-badge)](https://github.com/MieuxVoter/majority-judgment-library-kotlin/actions)
[![Code Quality](https://img.shields.io/codefactor/grade/github/MieuxVoter/majority-judgment-library-kotlin?style=for-the-badge)](https://www.codefactor.io/repository/github/mieuxvoter/majority-judgment-library-kotlin)
[![Join the Discord chat at https://discord.gg/k9YRuZPSZs](https://img.shields.io/discord/705322981102190593.svg?style=for-the-badge)](https://discord.gg/k9YRuZPSZs)

> [!WARNING]
> This is a work in progress ; no release has been made yet.

Test-driven Kotlin library to help deliberate (rank candidates) using [Majority Judgment](https://mieuxvoter.fr/index.php/decouvrir/?lang=en).

The goal is to be **scalable**, **reliable**, fast and extensible.

## Features

- [x] Supports billions of voters
- [x] Supports millions of candidates
- [ ] Handles default grades (static or normalized) (TODO)
- [x] No floating-point arithmetic used in ranking
- [x] Room for other deliberation resolvers (central, usual)
- [x] Computes a scalar majority merit for candidates that can be used for proportional representation


## Example Usage

Collect the **tallies** for each candidate by your own means,
provide them to `MajorityJudgment.deliberate()`, and get back the **rank** of each candidate,
with some additional analysis data.

Let's say you have the following tally, from a poll with 18 voters:

|            | To Reject | Poor | Passable | Somewhat Good | Good | Very Good | Excellent |
|------------|-----------|------|----------|---------------|------|-----------|-----------|
| Arancini   |     4     |   5  |     2    |       1       |   3  |     1     |     2     |
| Burger     |     3     |   6  |     2    |       2       |   2  |     1     |     2     |
| Chips      |     5     |   3  |     0    |       2       |   3  |     2     |     3     |
|     …      |           |      |          |               |      |           |           |
|            |           |      |          |               |      |           |           |

> The values in the table are the amount of judgments received per grade, by each candidate.
> They are entirely fabricated for the purpose of this example and do not reflect reality.

```kotlin
val mj = MajorityJudgment()
val tally = PollTally(
    candidatesTallies = listOf(
        CandidateTally(gradesTallies = arrayOf(4, 5, 2, 1, 3, 1, 2)),
        CandidateTally(gradesTallies = arrayOf(3, 6, 2, 2, 2, 1, 2)),
        CandidateTally(gradesTallies = arrayOf(5, 3, 0, 2, 3, 2, 3)),
    ),
)
val result = mj.deliberate(tally)

print(result.candidateResults.map { it.rank }) // [ 2, 3, 1 ]
print(result.candidateResultsRanked.map { it.index }) // [ 2, 0, 1 ]
```

> [!TIP]
> Got more than 2³² voters?  Use `Long`s. \
> Got even more than that ?  Use `BigInteger`s !


## Balancing

Sometimes, depending on how you've set up your poll, some candidates may receive more judgments than others.

Majority Judgment only works if the merit profiles are *balanced*, that is holding the same total amount of judgments.




<!--

### Using a static default grade

Want to set a static default grade ?  Use a `StaticDefaultTally` instead of a `Tally`.

```java
Integer amountOfJudges = 18;
Integer defaultGrade = 0;  // "worst" grade (usually "to reject")
TallyInterface tally = new StaticDefaultTally(new ProposalTallyInterface[] {
        // Amounts of judgments received of each grade, from "worst" grade to "best" grade
        new ProposalTally(new Integer[]{4, 5, 2, 1, 3, 1, 2}),  // Proposal A
        new ProposalTally(new Integer[]{3, 6, 2, 1, 3, 1, 2}),  // Proposal B
        // …
}, amountOfJudges, defaultGrade);
```


### Using normalized tallies

In some polls with a very high amount of proposals, where participants cannot be expected to judge every last one of them, it may make sense to normalize the tallies instead of using a default grade.

---

> TODO

---

> This normalization uses the Least Common Multiple, in order to skip floating-point arithmetic.

-->



## Run the test-suite

We have a bunch of unit tests.

    ./kotlin test


