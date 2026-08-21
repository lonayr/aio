package com.example.data.model

import com.example.data.local.entity.ProductEntity

data class CartItem(
    val product: ProductEntity,
    val quantity: Int = 1,
    val itemNote: String = ""
) {
    val subtotal: Double
        get() = product.price * quantity
}
