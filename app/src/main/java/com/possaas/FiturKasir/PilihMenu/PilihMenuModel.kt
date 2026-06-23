package com.possaas.FiturKasir.PilihMenu

data class PilihMenuModel(
    var id: String = "",
    var nama: String = "",
    var harga: Long = 0,
    var foto: String = "",
    var kategori: MutableList<String> = mutableListOf(),
    var stok: Int = 0,
    var jumlah: Int = 0
)