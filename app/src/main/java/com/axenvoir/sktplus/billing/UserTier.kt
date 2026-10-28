package com.axenvoir.sktplus.billing

enum class UserTier {
    FREE,
    SUPPORTER,
    PREMIUM,
    LIFETIME,
}

data class UserEntitlement(
    val tier: UserTier = UserTier.FREE,
    val maxProducts: Int = 50,
    val maxCategories: Int = 3,
    val cloudSync: Boolean = false,
    val widgetEnabled: Boolean = false,
    val themeCustomization: Boolean = false,
    val subscriptionExpiry: Long? = null,
) {
    val isPremium: Boolean get() = tier == UserTier.PREMIUM || tier == UserTier.LIFETIME
    val isSupporter: Boolean get() = tier == UserTier.SUPPORTER || isPremium
    val adsEnabled: Boolean get() = tier == UserTier.FREE
}

object TierLimits {
    val FREE = UserEntitlement(
        tier = UserTier.FREE,
        maxProducts = 50,
        maxCategories = 3,
    )
    val SUPPORTER = UserEntitlement(
        tier = UserTier.SUPPORTER,
        maxProducts = 100,
        maxCategories = 10,
        themeCustomization = true,
    )
    val PREMIUM = UserEntitlement(
        tier = UserTier.PREMIUM,
        maxProducts = Int.MAX_VALUE,
        maxCategories = Int.MAX_VALUE,
        cloudSync = true,
        widgetEnabled = true,
        themeCustomization = true,
    )
    val LIFETIME = PREMIUM.copy(tier = UserTier.LIFETIME)
}
