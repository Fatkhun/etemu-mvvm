package com.fatkhun.core.model

data class CategoriesResponse(
    val data: MutableList<CategoriesItem> = mutableListOf()
): BaseResponse()

data class CategoriesItem(
    val id: Int = 0,
    val name: String = "",
    val created_at: String = "",
    val updated_at: String = ""
)