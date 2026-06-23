package com.possaas.FiturKasir.PilihMenu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.possaas.R
import java.text.NumberFormat
import java.util.Locale

class CartSummaryAdapter(
    private val cartItems: List<PilihMenuModel>
) : RecyclerView.Adapter<CartSummaryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgMenu: ImageView = itemView.findViewById(R.id.imgMenu)
        val txtMenuName: TextView = itemView.findViewById(R.id.txtMenuName)
        val txtQty: TextView = itemView.findViewById(R.id.txtQty)
        val txtTotalSubHarga: TextView = itemView.findViewById(R.id.txtTotalSubHarga)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart_summary, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return cartItems.filter { it.jumlah > 0 }.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val filteredItems = cartItems.filter { it.jumlah > 0 }
        val item = filteredItems[position]

        holder.txtMenuName.text = item.nama
        holder.txtQty.text = "${item.jumlah}x"

        Glide.with(holder.itemView.context)
            .load(item.foto)
            .placeholder(R.drawable.logo_bfc)
            .into(holder.imgMenu)

        val totalSubHarga = item.jumlah * item.harga
        val rupiah = NumberFormat.getInstance(Locale("id", "ID"))
        holder.txtTotalSubHarga.text = "Rp ${rupiah.format(totalSubHarga)}"
    }
}