package com.fatkhun.core.model

import com.fatkhun.core.utils.Status

data class PostingItemForm(
    val id: Int = 0,
    val type: String = "",
    val name: String = "",
    val status: String = "",
    val description: String = "",
    val contact_type: String = "",
    val contact_value: String = "",
    val photo_url: String = "",
    val user_id: Int = 0
)

data class LostFoundForm(
    val keyword: String = "",
    val user_id: Int = 0,
    val category_id: Int = 0,
    val status: String = "",
    val type: String = "",
    val limit: Int = 0,
    val offset: Int = 0
)

data class PostingUpdateForm(
    val category_id: Int = 0,
    val type: String = "",
    val name: String = "",
    val status: String = "",
    val description: String = "",
    val contact_type: String = "",
    val contact_value: String = "",
    val photo: String = "",
    val owner_id: Int = 0
)

data class PostingUpdateStatusForm(
    val status: String = ""
)