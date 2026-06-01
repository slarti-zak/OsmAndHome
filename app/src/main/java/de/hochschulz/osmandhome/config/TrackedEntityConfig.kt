package de.hochschulz.osmandhome.config

data class TrackedEntityConfig(
    val entityId: String,
    val enabled: Boolean     = true,
    val displayName: String? = null,
    val customColor: Int?    = null,
    val showLabel: Boolean   = true,
    val showAvatar: Boolean  = true
)