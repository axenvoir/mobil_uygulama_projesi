package com.axenvoir.expira.billing

enum class UserTier {
    FREE,
    SUPPORTER,
    PREMIUM,
    LIFETIME,
}

val UserTier.isAdFree: Boolean
    get() = this != UserTier.FREE
