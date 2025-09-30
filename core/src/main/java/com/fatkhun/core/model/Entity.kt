package com.fatkhun.core.model

data class NetworkConnection(
    val type: Int = 0,
    val isConnected: Boolean = false
)

data class ProviderConnection(
    val provider: Int = 0,
    val isTurnOn: Boolean = false
)