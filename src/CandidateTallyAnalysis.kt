import com.ionspin.kotlin.bignum.integer.BigInteger

/**
 * Collect useful data on a candidate's tally.
 * Does NOT compute the rank nor the scalar merit, but provides everything we need to do so.
 */
class CandidateTallyAnalysis {

    var tally: CandidateTallyInterface
        private set

    var totalSize: BigInteger = BigInteger.ZERO // amount of judges
        private set

    var medianGrade: Int = 0
        private set

    var medianGroupSize: BigInteger = BigInteger.ZERO // amount of judges in the median group
        private set

    var contestationGrade: Int = 0 // "best" grade of the contestation group
        private set

    var contestationGroupSize: BigInteger = BigInteger.ZERO // of lower grades than median
        private set

    var adhesionGrade: Int = 0 // "worst" grade of the adhesion group
        private set

    var adhesionGroupSize: BigInteger = BigInteger.ZERO // of higher grades than median
        private set

    var secondMedianGrade: Int = 0 // grade of the biggest group out of the median
        private set

    var secondMedianGroupSize: BigInteger = BigInteger.ZERO // either contestation or adhesion
        private set

    var secondMedianGroupSign: Int = 0 // -1 for contestation, +1 for adhesion, 0 for empty group size
        private set

    constructor(
        tally: CandidateTallyInterface,
        favorContestation: Boolean = true,
    ) {
        this.tally = tally
        reanalyze(tally, favorContestation)
    }

    fun reanalyze(
        tally: CandidateTallyInterface,
        favorContestation: Boolean = true,
    ) {
        this.tally = tally
        this.totalSize = BigInteger.ZERO
        this.medianGrade = 0
        this.medianGroupSize = BigInteger.ZERO
        this.contestationGrade = 0
        this.contestationGroupSize = BigInteger.ZERO
        this.adhesionGrade = 0
        this.adhesionGroupSize = BigInteger.ZERO

        val gradesTallies = tally.gradesTallies
        val amountOfGrades = gradesTallies.size

        for (gradeTally in gradesTallies) {
            require(gradeTally >= BigInteger.ZERO) { "Negative tallies are not allowed." }
            this.totalSize += gradeTally
        }

        val medianOffset = if (favorContestation) {
            1
        } else {
            2
        }
        val medianCursor = this.totalSize
            .add(BigInteger.fromInt(medianOffset))
            .divide(BigInteger.TWO)

        var tallyBeforeCursor: BigInteger
        var tallyCursor = BigInteger.ZERO
        var foundMedian = false
        for (grade in 0..<amountOfGrades) {
            val gradeTally = gradesTallies[grade]
            tallyBeforeCursor = tallyCursor
            tallyCursor = tallyCursor.add(gradeTally)

            if (!foundMedian) {
                if (tallyCursor >= medianCursor) {
                    foundMedian = true
                    this.medianGrade = grade
                    this.contestationGroupSize = tallyBeforeCursor
                    this.medianGroupSize = gradeTally
                    this.adhesionGroupSize = this.totalSize
                        .subtract(this.contestationGroupSize)
                        .subtract(this.medianGroupSize)
                } else {
                    if (0 < gradeTally.compareTo(BigInteger.ZERO)) { // 0 < gradeTally
                        this.contestationGrade = grade
                    }
                }
            } else {
                if (0 < gradeTally.compareTo(BigInteger.ZERO) && 0 == this.adhesionGrade) {
                    this.adhesionGrade = grade
                }
            }
        }

        this.secondMedianGroupSize = BigInteger.max(this.contestationGroupSize, this.adhesionGroupSize)

        if (this.adhesionGroupSize > this.contestationGroupSize) { // adhesion group wins
            this.secondMedianGrade = this.adhesionGrade
            this.secondMedianGroupSign = 1
        } else if (this.contestationGroupSize > this.adhesionGroupSize) { // contestation group wins
            this.secondMedianGrade = this.contestationGrade
            this.secondMedianGroupSign = -1
        } else { // equality
            if (favorContestation) {
                this.secondMedianGrade = this.contestationGrade
                this.secondMedianGroupSign = -1
            } else {
                this.secondMedianGrade = this.adhesionGrade
                this.secondMedianGroupSign = 1
            }
        }

        if (0 == this.secondMedianGroupSize.compareTo(BigInteger.ZERO)) {
            this.secondMedianGroupSign = 0
        }
    }

    fun computeResolution(
        tally: CandidateTallyInterface,
        favorContestation: Boolean = true,
    ): Array<ParticipantGroup> {
        val participantGroups = ArrayList<ParticipantGroup>()
        val currentTally = WorkingCandidateTally(tally.gradesTallies.copyOf())
        val analysis = CandidateTallyAnalysis(currentTally, favorContestation)

        participantGroups.add(
            ParticipantGroup(
                analysis.medianGroupSize, analysis.medianGrade, ParticipantGroup.Type.Median
            )
        )

        val amountOfGrades = tally.gradesTallies.size
        repeat(amountOfGrades - 1) {
            analysis.reanalyze(currentTally, favorContestation)

            var type = ParticipantGroup.Type.Median
            if (analysis.secondMedianGroupSign > 0) {
                type = ParticipantGroup.Type.Adhesion
            } else if (analysis.secondMedianGroupSign < 0) {
                type = ParticipantGroup.Type.Contestation
            }

            if (type != ParticipantGroup.Type.Median) { // ie. secondMedianGroupSize != 0
                participantGroups.add(
                    ParticipantGroup(
                        analysis.secondMedianGroupSize, analysis.secondMedianGrade, type
                    )
                )
            }

            currentTally.moveJudgments(analysis.medianGrade, analysis.secondMedianGrade)
        }

        return participantGroups.toTypedArray<ParticipantGroup>()
    }
}
