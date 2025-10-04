package com.fatkhun.core.model

data class LostFoundResponse(
    val data: LostFoundItem = LostFoundItem()
): BaseResponse()

data class LostFoundItem(
    val items: MutableList<LostFoundItemList> = mutableListOf()
)

data class LostFoundItemList(
    val id: Int = 0,
    val category_id: CategoryItem = CategoryItem(),
    val type: String = "",
    val name: String = "",
    val description: String = "",
    val photo_url: String = "",
    val contact_type: String = "",
    val contact_value: String = "",
    val status: String = "",
    val owner_id: OwnerItem = OwnerItem(),
    val created_at: String = "",
    val updated_at: String = ""
)

data class OwnerItem(
    val id: Int = 0,
    val name: String = "",
    val email: String = ""
)

data class CategoryItem(
    val id: Int = 0,
    val name: String = ""
)

data class DetailItemResponse(
    val data: LostFoundItemList = LostFoundItemList()
): BaseResponse()