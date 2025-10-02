package com.fatkhun.core.model

data class LostFoundResponse(
    val data: LostFoundItem = LostFoundItem()
): BaseResponse()

data class LostFoundItem(
    val items: MutableList<LostFoundItemList> = mutableListOf()
)

data class LostFoundItemList(
    val _id: String = "",
    val category: CategoryItem = CategoryItem(),
    val type: String = "",
    val name: String = "",
    val description: String = "",
    val photoUrl: String = "",
    val contact: ContactItem = ContactItem(),
    val status: String = "",
    val owner: OwnerItem = OwnerItem(),
    val createdAt: String = "",
    val updatedAt: String = ""
)

data class OwnerItem(
    val _id: String = "",
    val name: String = "",
    val email: String = ""
)

data class CategoryItem(
    val _id: String = "",
    val name: String = ""
)

data class ContactItem(
    val type: String = "",
    val value: String = ""
)

data class DetailItemResponse(
    val data: LostFoundItemList = LostFoundItemList()
): BaseResponse()