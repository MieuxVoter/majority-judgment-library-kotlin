import com.ionspin.kotlin.bignum.integer.BigInteger

/**
 * See [CandidateTallyInterface].
 */
class CandidateTally(
    override val gradesTallies: Array<BigInteger>,
) : CandidateTallyInterface {

    constructor(gradesTallies: Array<Int>) : this(
        gradesTallies = gradesTallies.map { BigInteger.fromInt(it) }.toTypedArray(),
    )

    constructor(gradesTallies: Iterable<Int>) : this(
        gradesTallies = gradesTallies.map { BigInteger.fromInt(it) }.toTypedArray(),
    )

    // When enabling this we get JVM collisions errors ; TBD…
//    constructor(gradesTalliesLong: Array<Long>) : this(
//        gradesTallies=gradesTalliesLong.map { BigInteger.fromLong(it) }.toTypedArray(),
//    )
//    constructor(gradesTalliesLong: Iterable<Long>) : this(
//        gradesTallies=gradesTalliesLong.map { BigInteger.fromLong(it) }.toTypedArray(),
//    )

}
