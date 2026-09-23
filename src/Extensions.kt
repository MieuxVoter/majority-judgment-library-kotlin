import com.ionspin.kotlin.bignum.integer.BigInteger

fun <T> Array<out T>.sumOf(selector: (T) -> BigInteger): BigInteger {
    var sum: BigInteger = BigInteger.ZERO
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

fun <T> Iterable<T>.sumOf(selector: (T) -> BigInteger): BigInteger {
    var sum: BigInteger = BigInteger.ZERO
    for (element in this) {
        sum += selector(element)
    }
    return sum
}
