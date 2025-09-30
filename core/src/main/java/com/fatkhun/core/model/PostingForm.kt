package com.fatkhun.core.model

data class PostingItemForm(
    val categoryId: String = "",
    val type: String = "",
    val name: String = "",
    val description: String = "",
    val contactType: String = "",
    val contactValue: String = "",
    val file_evidence_path: String = ""
)

data class LostFoundForm(
    val keyword: String = "",
    val category_id: String = "",
    val status: String = "",
    val type: String = "",
    val limit: Int = 0,
    val offset: Int = 0
)