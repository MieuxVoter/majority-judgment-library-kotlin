import com.ionspin.kotlin.bignum.integer.BigInteger

data class ParticipantGroup(
    var size: BigInteger,
    var grade: Int,
    var type: Type,
) {
    enum class Type {
        Median,
        Contestation,
        Adhesion,
    }
}
