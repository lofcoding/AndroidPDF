package com.loc.androidpdf

data class Invoice(
    val number: Long,
    val price: Float,
    val link: String,
    val date: String,
    val from: PersonInfo,
    val to: PersonInfo,
    val products: List<Product>,
    val signatureUrl: String? = null
)

data class PersonInfo(
    val name: String,
    val address: String
)

data class Product(
    val description: String,
    val rate: Float,
    val quantity: Int
)