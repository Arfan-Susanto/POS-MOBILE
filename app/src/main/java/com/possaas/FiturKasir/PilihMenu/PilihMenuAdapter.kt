package com.possaas.FiturKasir.PilihMenu

import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.possaas.R
import java.text.NumberFormat
import java.util.Locale

class PilihMenuAdapter(
    private val listMenu: ArrayList<PilihMenuModel>,
    private val listener: OnCartChangeListener
) : RecyclerView.Adapter<PilihMenuAdapter.ViewHolder>() {

    interface OnCartChangeListener {
        fun onCartChanged(list: List<PilihMenuModel>)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgMenu: ImageView = itemView.findViewById(R.id.imgMenu)
        val txtNamaMenu: TextView = itemView.findViewById(R.id.txtNamaMenu)
        val txtHarga: TextView = itemView.findViewById(R.id.txtHarga)
        val layoutKategori: LinearLayout = itemView.findViewById(R.id.layoutKategori)
        val txtJumlah: TextView = itemView.findViewById(R.id.txtJumlah)
        val btnPlus: LinearLayout = itemView.findViewById(R.id.btnPlus)
        val btnMinus: LinearLayout = itemView.findViewById(R.id.btnMinus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pilih_menu, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = listMenu.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val menu = listMenu[position]

        holder.txtNamaMenu.text = menu.nama

        val rupiah = NumberFormat.getInstance(Locale("id", "ID"))
        holder.txtHarga.text = "Rp ${rupiah.format(menu.harga)}"

        holder.txtJumlah.text = menu.jumlah.toString()

        Glide.with(holder.itemView.context)
            .load(menu.foto)
            .placeholder(R.drawable.logo_bfc)
            .into(holder.imgMenu)

        holder.layoutKategori.removeAllViews()
        for (kategori in menu.kategori) {
            val txt = TextView(holder.itemView.context)
            txt.text = kategori
            txt.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.accent))
            txt.textSize = 11f
            txt.setPadding(22, 10, 22, 10)
            val plusJakarta = ResourcesCompat.getFont(holder.itemView.context, R.font.plus_jakarta)
            if (plusJakarta != null) {
                txt.typeface = Typeface.create(plusJakarta, Typeface.NORMAL)
            } else {
                txt.setTypeface(null, Typeface.NORMAL)
            }
            txt.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.bg_kategori_primary)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 10)
            txt.layoutParams = params

            holder.layoutKategori.addView(txt)
        }

        holder.btnPlus.setOnClickListener {
            if (menu.jumlah < menu.stok) {
                menu.jumlah++
                holder.txtJumlah.text = menu.jumlah.toString()
                listener.onCartChanged(listMenu)
            }
        }

        holder.btnMinus.setOnClickListener {
            if (menu.jumlah > 0) {
                menu.jumlah--
                holder.txtJumlah.text = menu.jumlah.toString()
                listener.onCartChanged(listMenu)
            }
        }
    }
}