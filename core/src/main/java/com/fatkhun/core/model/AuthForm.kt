package com.fatkhun.core.model

data class RegisterForm(
    val name: String = "",
    val email: String = "",
    val password: String = ""
)

data class LoginForm(
    val email: String = "",
    val password: String = ""
)