package com.fatkhun.core.model

data class PostingItemForm(
    val id: Int = 0,
    val type: String = "",
    val name: String = "",
    val description: String = "",
    val contact_type: String = "",
    val contact_value: String = "",
    val file_evidence_path: String = "",
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