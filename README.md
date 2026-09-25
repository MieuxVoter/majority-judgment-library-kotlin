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

- [x] Super-fast
- [x] Supports billions of voters
- [x] Supports millions of candidates
- [x] Handles default grades (static or normalized)
- [x] No floating-point arithmetic used in ranking
- [x] Room for other deliberation resolvers (central, usual)


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

### Balancing using the lowest grade

This balancing strategy — using the lowest grade as the default grade — is recommended for most polls.

It incentivizes candidates to be clear, and to promote themselves.

```kotlin
val mj = MajorityJudgment()
val tally = StaticDefaultBalancedPollTally(
    candidatesTallies = listOf(
        CandidateTally(gradesTallies = arrayOf(1, 2, 3, 4)),
        CandidateTally(gradesTallies = arrayOf(0, 0, 3, 4)),
    ),
)

println(tally.candidatesTallies[1]) // [ 3, 0, 3, 4 ]

assertContentEquals(
    expected = arrayOf(3, 0, 3, 4).map { BigInteger.fromInt(it) }.toTypedArray(),
    actual = tally.candidatesTallies[1].gradesTallies,
    message = "Correct ranks",
)

val result = mj.deliberate(tally)

println(result.candidateResults.map { it.rank }) // [ 1, 2 ]
println(result.candidateResultsRanked.map { it.index }) // [ 0, 1 ]
```


### Balancing using normalization

> TODO: explain it here (it's already coded, see `NormalizationBalancedPollTally`)


## Run the test-suite

We have a bunch of unit tests.

    ./kotlin test


