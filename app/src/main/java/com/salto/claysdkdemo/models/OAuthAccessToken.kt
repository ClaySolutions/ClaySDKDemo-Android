package com.salto.claysdkdemo.models

import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.joda.time.DateTimeZone
import org.joda.time.Instant
import org.joda.time.LocalDateTime

class OAuthAccessToken(
    private val mAccessToken: String,
    val refreshToken: String,
    private val mTokenType: String,
    expiresIn: Long
) {
    private val mExpirationDate: Long = Instant.now().millis + (expiresIn * 0.9 * 1000).toInt()

    val accessToken: String
        get() = String.format("%s %s", mTokenType, mAccessToken)

    val isValid: Boolean
        get() = mExpirationDate > Instant.now().millis

    val expiration: String
        get() = LocalDateTime(mExpirationDate, DateTimeZone.UTC).toString()

    override fun equals(other: Any?): Boolean {
        if (other !is OAuthAccessToken) {
            return false
        }
        if (other === this) {
            return true
        }
        val equalsBuilder = EqualsBuilder()
            .append(mAccessToken, other.mAccessToken)
            .append(refreshToken, other.refreshToken)
            .append(mTokenType, other.mTokenType)
        return equalsBuilder.isEquals
    }

    override fun hashCode(): Int {
        val builder = HashCodeBuilder(17, 31)
            .append(mAccessToken)
            .append(refreshToken)
            .append(mTokenType)
        return builder.toHashCode()
    }

}