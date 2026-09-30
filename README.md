# Majority Judgment Library for Kotlin Multiplatform

[![MIT](https://img.shields.io/github/license/MieuxVoter/majority-judgment-library-kotlin?style=for-the-badge)](./LICENSE)
[![Release](https://img.shields.io/github/v/release/MieuxVoter/majority-judgment-library-kotlin?sort=semver&style=for-the-badge)](https://github.com/MieuxVoter/majority-judgment-library-kotlin/releases)
[![Build Status](https://img.shields.io/github/actions/workflow/status/MieuxVoter/majority-judgment-library-kotlin/build.yml?style=for-the-badge)](https://github.com/MieuxVoter/majority-judgment-library-kotlin/actions)
[![Code Quality](https://img.shields.io/codefactor/grade/github/MieuxVoter/majority-judgment-library-kotlin?style=for-the-badge)](https://www.codefactor.io/repository/github/mieuxvoter/majority-judgment-library-kotlin)
[![Join the Discord chat at https://discord.gg/k9YRuZPSZs](https://img.shields.io/discord/705322981102190593.svg?style=for-the-badge)](https://discord.gg/k9YRuZPSZs)

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
| Arancini   |     4     |   4  |     3    |       1       |   3  |     1     |     2     |
| Burger     |     3     |   6  |     2    |       2       |   2  |     1     |     2     |
| Chips      |     5     |   3  |     0    |       2       |   3  |     2     |     3     |
|     …      |           |      |          |               |      |           |           |
|            |           |      |          |               |      |           |           |

> The values in the table are the amount of judgments received per grade, by each candidate.
> They are entirely fabricated for the purpose of this example and do not reflect reality.

![Example merit profile with three candidates](docs/merit-example.svg)

```kotlin
val mj = MajorityJudgment()
val tally = PollTally(
    candidatesTallies = listOf(
        CandidateTally(gradesTallies = arrayOf(4, 4, 3, 1, 3, 1, 2)),
        CandidateTally(gradesTallies = arrayOf(3, 6, 2, 2, 2, 1, 2)),
        CandidateTally(gradesTallies = arrayOf(5, 3, 0, 2, 3, 2, 3)),
    ),
)
val result = mj.deliberate(tally)

print(result.candidateResults.map { it.rank }) // [ 2, 3, 1 ]
print(result.candidateResultsRanked.map { it.index }) // [ 2, 0, 1 ]
```

![Example merit profiles of three ranked candidates](docs/merit-example-ranked.svg)

> [!TIP]
> The images of the merit profiles were generated using our [online merit profile generator](https://educ.mieuxvoter.fr).


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

println(tally.candidatesTallies[1].gradesTallies) // [ 3, 0, 3, 4 ]

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

When there are too many candidates to vote on all of them, a good balancing strategy can be normalization.

We provide a _Least Common Multiple_ based normalization.

To that effect you can use the `NormalizationBalancedPollTally`:

```kotlin
val mj = MajorityJudgment()
val tally = NormalizationBalancedPollTally(
    candidatesTallies = listOf(
        CandidateTally(gradesTallies = arrayOf(1, 2, 3, 4, 5)), // 15 judgments total
        CandidateTally(gradesTallies = arrayOf(1, 1, 1, 1, 1)), //  5 judgments only
        CandidateTally(gradesTallies = arrayOf(1, 1, 0, 0, 1)), //  3 judgments only
    ),
)

// Now the candidates' tallies are balanced
println(tally.candidatesTallies[0].gradesTallies.contentToString()) // [1, 2, 3, 4, 5]
println(tally.candidatesTallies[1].gradesTallies.contentToString()) // [3, 3, 3, 3, 3]
println(tally.candidatesTallies[2].gradesTallies.contentToString()) // [5, 5, 0, 0, 5]


val result = mj.deliberate(tally)

println(result.candidateResults.map { it.rank }) // [1, 2, 3]
println(result.candidateResultsRanked.map { it.index }) // [0, 1, 2]
```

## Run the test-suite

We have a bunch of unit tests.

    ./kotlin test


## Architecture Decisions

### Why bother with BigInteger ?

There are two reasons why we want to be able to handle huge integers, beyond what 64 bits can provide us.

> I did not want to depend on an external lib, but it's worth it, I promise.

#### Balance tallies using normalization via Least Common Multiple

When you want to do normalization using percentages or floating numbers, you risk having inaccurate results.
Even if the probability of inaccurate results is low (very low!), it's uncool.

> Voting systems must be 100% reliable, I believe.
> Also, I'm a computer engineer, and _IEEE 754_ fuels my nightmares.

Hence, we do normalization using LCM, and those multiples can grow fast if you're unlucky and fall on primes.

There's a way to normalize accurately without pre-computing the LCM, but it caps the amount of supported voters to about 3.4e9.
That is a reasonable upper limit in 2026, but I'd rather support arbitrarily large amounts of voters, especially with the upcoming demographic boost wrought by the birth of AIs.

#### Scalar Majority Merit

Although we can sort candidates using their deep majority gauges, the scalar majority merit is a very nice thing to have around, as it provides multiple advantages:

- a fast sort criteria
- a way to do proportional representation (albeit not anti-strategic)
- a way to approximate the _absolute rank_ of a merit profile
- a feel of how close (or further apart) two merit profiles are

The scalar majority merit grows very big very quickly, and requires support for large numbers, as 64 bits are not enough, not by a long shot.
Its size is on the scale of the amount of voters to the power of the amount of grades.

