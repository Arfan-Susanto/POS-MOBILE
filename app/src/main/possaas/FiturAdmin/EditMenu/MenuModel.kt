package com.possaas.FiturAdmin.EditMenu

data class MenuModel(
    var id: String = "",
    var nama: String = "",
    var harga: Long = 0,
    var kategori: MutableList<String> = mutableListOf(),
    var foto: String = ""
)