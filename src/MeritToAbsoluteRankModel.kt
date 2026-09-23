import kotlin.math.E
import kotlin.math.pow
import kotlin.math.sin

/**
 * This is an experiment.  This is NOT used in computing the ranking of MJ.  Don't worry.  You can ignore this.
 * It is used to approximate the "absolute rank" of a merit profile from its scalar majority merit.
 * What we call "absolute rank" is the rank of a merit profile in the MJ poll with ALL possible merit profiles.
 */
class MeritToAbsoluteRankModel {
    /**
     * @param merit is expected to be normalized (between 0 and 1)
     * @return the approximation of the absolute rank, normalized
     */
    fun apply(
        merit: Double,
        amountOfGrades: Int,
        amountOfJudges: Long,
    ): Double {
        /**
         * This is *not* a model of a sigmoid.
         * This is a model of how our sigmoids' amplitudes evolve with the amount of judges.
         */
        class SigmoidAmplitudeModel(
            val coefficient: Double,
            val offset: Double,
            val origin: Double,
            val sinAmplitude: Double,
            val sinOrigin: Double,
            val sinPhase: Double,
        ) {
            fun computeAmplitude(amountOfJudges: Long): Double {
                return (this.offset + (this.coefficient / (amountOfJudges - this.origin))
                        + (
                        this.sinAmplitude * sin(amountOfJudges * Math.PI + this.sinPhase)
                                /
                                (amountOfJudges - this.sinOrigin)))
            }
        }

        // This bullshit fitting has been made using dirty, dirty python ; but it works well enough for now
        val sam: Array<SigmoidAmplitudeModel>?
        when (amountOfGrades) {
            2 -> {
                // With 2 grades the scalar merit is already affine
                return 1.0 - merit
            }
            3 -> {
                // Values derived from rough model fitting ; they can be improved
                sam = arrayOf<SigmoidAmplitudeModel>(
                    SigmoidAmplitudeModel(
                        0.6409350779507367,
                        0.4965854515219494,
                        -5.914696245375644,
                        23.38514377704792,
                        0.9996311919466460,
                        0.0009832013303302,
                    ),
                    SigmoidAmplitudeModel(
                        -0.6410295650865494,
                        0.5034170870490888,
                        -5.915780584894787,
                        -0.5494767763972728,
                        1.0001343001977745,
                        0.0418436294071475,
                    ),
                )
            }
            4 -> {
                // Values derived from rough model fitting ; they can be improved
                sam = arrayOf<SigmoidAmplitudeModel>(
                    SigmoidAmplitudeModel(
                        0.9170475003989843,
                        0.2456153714826784,
                        -3.5091977159324292,
                        0.1867944159248675,
                        0.9990570652741461,
                        -6.105115854811561,
                    ),
                    SigmoidAmplitudeModel(
                        -0.8277524466501042,
                        0.5019721627432320,
                        -3.0645135231547678,
                        -0.0080383779640542,
                        1.2071429213468290,
                        0.5552095403898315,
                    ),
                    SigmoidAmplitudeModel(
                        -0.0537159095557622,
                        0.2509962916400555,
                        -10.322521372701758,
                        -0.0450613036977610,
                        0.7945092912788447,
                        0.7452859647656658,
                    ),
                )
            }
            5 -> {
                // Values derived from rough model fitting ; they can be improved
                sam = arrayOf<SigmoidAmplitudeModel>(
                    SigmoidAmplitudeModel(
                        0.9000482334396634,
                        0.1206547483774695,
                        -2.4963552848848400,
                        -0.0356967817861015,
                        1.0359005237315060,
                        -1.5470500509326637,
                    ),
                    SigmoidAmplitudeModel(
                        -0.3290841630085418,
                        0.3771535023430787,
                        -1.2587082942998835,
                        -5.2922128265961055,
                        0.1750391985549460,
                        -0.0032739374414037,
                    ),
                    SigmoidAmplitudeModel(
                        -0.8157881989763880,
                        0.3768242875184030,
                        -3.6329714453909800,
                        -0.0239089808347504,
                        0.6837626088956580,
                        1.5690544889497136,
                    ),
                    SigmoidAmplitudeModel(
                        0.1980505155370003,
                        0.1265655384666737,
                        -2.595110846626628,
                        -0.1151449718489945,
                        0.9638237758976738,
                        0.2475457864562964,
                    ),
                )
            }
            6 -> {
                // Values derived from rough model fitting ; they can be improved
                sam = arrayOf<SigmoidAmplitudeModel>(
                    SigmoidAmplitudeModel(
                        0.7708075223467123,
                        0.0580869399899168,
                        -1.7708756450606116,
                        -0.0514019515740431,
                        1.0922318721535316,
                        5.543570790101829,
                    ),
                    SigmoidAmplitudeModel(
                        0.0113468236267469,
                        0.2593847095025533,
                        3.4080676150197013,
                        0.3127399704834197,
                        4.516275255204553,
                        0.0246261384044150,
                    ),
                    SigmoidAmplitudeModel(
                        -0.9580137264950088,
                        0.3756958174463476,
                        -3.691237611566132,
                        -0.0808529635154282,
                        1.1932023599818111,
                        6.3582517865739,
                    ),
                    SigmoidAmplitudeModel(
                        -0.3759791146003723,
                        0.2517848780681173,
                        -2.7201748200294440,
                        -0.0411965223777122,
                        0.4881179927844195,
                        -5.301106532574872,
                    ),
                    SigmoidAmplitudeModel(
                        0.2852468211981568,
                        0.0634098062656290,
                        -2.624725281419371,
                        0.0250299350511801,
                        1.0159131152232956,
                        -1.583788629415341,
                    ),
                )
            }
            7 -> {
                // Values derived from rough model fitting ; they can be improved
                sam = arrayOf<SigmoidAmplitudeModel>(
                    SigmoidAmplitudeModel(
                        0.5151336373041772,
                        0.0304017096437998,
                        -0.1560819745436698,
                        -0.0642768687910415,
                        3.701961856511572,
                        -0.2267673450950530,
                    ),
                    SigmoidAmplitudeModel(
                        0.8321495032592745,
                        0.1538010001096599,
                        -10.1403742732170450,
                        0.1452337649130754,
                        2.9093303593527824,
                        0.1670760936959231,
                    ),
                    SigmoidAmplitudeModel(
                        -0.5832534017217945,
                        0.3128738036537556,
                        -2.4481699553712186,
                        1.769859148904302,
                        0.0064898411429031,
                        -3.1491904326892173,
                    ),
                    SigmoidAmplitudeModel(
                        -0.9135479603269890,
                        0.3121169039235479,
                        -4.041938401368361,
                        -0.0398619334678863,
                        2.260898341853797,
                        -3.5661704309341040,
                    ),
                    SigmoidAmplitudeModel(
                        -0.0358891062680384,
                        0.1592742142625385,
                        0.8473094470570051,
                        -0.1720450496934443,
                        0.8776512589952787,
                        0.1900715592340584,
                    ),
                    SigmoidAmplitudeModel(
                        0.2965479931458628,
                        0.0309932939590777,
                        -2.706478536997022,
                        -0.0616634512919992,
                        3.306936959026428,
                        -3.3295936102008192,
                    ),
                )
            }
            else -> {
                // Let's add support for more grades later
                return 1.0
            }
        }

        var sumOfAmplitudes = 0.0
        val amplitudes = Array(amountOfGrades) { 0.0 }
        for (i in 0..<amountOfGrades - 1) {
            amplitudes[i] = sam[i].computeAmplitude(amountOfJudges)
            sumOfAmplitudes += amplitudes[i]
        }
        for (i in 0..<amountOfGrades - 1) {
            amplitudes[i] = amplitudes[i] / sumOfAmplitudes
        }

        val tightness = 96.0 // derived from fitting
        var rank = 0.0 // from 0.0 (exclusive) to 1.0 (inclusive) ; is 'double' enough precision?
        for (i in 0..<amountOfGrades - 1) {
            rank += amplitudes[i] * sigmoid(
                merit,
                tightness,
                (2.0 * i + 1.0) / (2.0 * (amountOfGrades - 1))
            )
        }

        return rank
    }

    @Suppress("SameParameterValue")
    private fun sigmoid(x: Double, tightness: Double, origin: Double): Double {
        return 1.0 / (1.0 + E.pow(tightness * (x - origin)))
    }
}
