package com.possaas.FiturKasir.PaymentSuccess

import android.os.Parcel
import android.os.Parcelable
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

data class ReceiptMenuModel(
    val id: String,
    val nama: String,
    val foto: String,
    val jumlah: Int,
    val hargaSatuan: Long
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(nama)
        parcel.writeString(foto)
        parcel.writeInt(jumlah)
        parcel.writeLong(hargaSatuan)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ReceiptMenuModel> {
        override fun createFromParcel(parcel: Parcel): ReceiptMenuModel {
            return ReceiptMenuModel(parcel)
        }

        override fun newArray(size: Int): Array<ReceiptMenuModel?> {
            return arrayOfNulls(size)
        }
    }
}

class ReceiptItemAdapter(
    private val items: List<ReceiptMenuModel>
) : RecyclerView.Adapter<ReceiptItemAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgMenuItem: ImageView = itemView.findViewById(R.id.imgMenuItem)
        val txtMenuName: TextView = itemView.findViewById(R.id.txtMenuName)
        val txtQuantity: TextView = itemView.findViewById(R.id.txtQuantity)
        val txtMenuPrice: TextView = itemView.findViewById(R.id.txtMenuPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_receipt_menu_success, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.txtMenuName.text = item.nama
        holder.txtQuantity.text = item.jumlah.toString()

        val rupiah = NumberFormat.getInstance(Locale("id", "ID"))
        val totalPrice = item.hargaSatuan * item.jumlah
        holder.txtMenuPrice.text = "Rp ${rupiah.format(totalPrice)}"

        Glide.with(holder.itemView.context)
            .load(item.foto)
            .placeholder(R.drawable.logo_bfc)
            .into(holder.imgMenuItem)
    }
}