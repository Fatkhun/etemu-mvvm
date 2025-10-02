package com.fatkhun.core.model

data class CategoriesResponse(
    val data: MutableList<CategoriesItem> = mutableListOf()
): BaseResponse()

data class CategoriesItem(
    val _id: String = "",
    val name: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
)