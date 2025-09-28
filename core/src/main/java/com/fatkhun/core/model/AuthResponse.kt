package com.fatkhun.core.model

data class RegisterResponse(
    val data : AuthItem = AuthItem()
): BaseResponse()

data class LoginResponse(
    val data: AuthItem = AuthItem()
): BaseResponse()

data class AuthItem(
    val token: String = "",
    val user: UserItem = UserItem()
)

data class UserItem(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = ""
)