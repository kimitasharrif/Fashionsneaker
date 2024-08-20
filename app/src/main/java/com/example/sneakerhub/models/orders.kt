package com.example.sneakerhub.models

data class Order(
    val orderId: Int,  // Changed to non-nullable
    val userId: Int,
    val items: List<Item>,
    val totalAmount: Double  // Changed to non-nullable
)

data class Item(
    val shoeId: String,
    val quantity: Int
)
