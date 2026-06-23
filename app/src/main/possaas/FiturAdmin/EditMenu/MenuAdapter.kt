package com.possaas.FiturAdmin.EditMenu

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

class MenuAdapter(

    private val listMenu: ArrayList<MenuModel>,

    private val onEditClick:
        (MenuModel) -> Unit,

    private val onDeleteClick:
        (MenuModel) -> Unit

) : RecyclerView.Adapter<MenuAdapter.ViewHolder>() {

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        val txtNamaMenu:
                TextView =
            itemView.findViewById(
                R.id.txtNamaMenu
            )

        val txtHarga:
                TextView =
            itemView.findViewById(
                R.id.txtHarga
            )

        val imgMenu:
                ImageView =
            itemView.findViewById(
                R.id.imgMenu
            )

        val layoutKategori:
                LinearLayout =
            itemView.findViewById(
                R.id.layoutKategori
            )

        val btnEdit:
                LinearLayout =
            itemView.findViewById(
                R.id.btnEdit
            )

        val btnDelete:
                LinearLayout =
            itemView.findViewById(
                R.id.btnDelete
            )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view =
            LayoutInflater.from(
                parent.context
            ).inflate(
                R.layout.item_menu,
                parent,
                false
            )

        return ViewHolder(view)
    }

    override fun getItemCount():
            Int {

        return listMenu.size
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val menu =
            listMenu[position]

        holder.txtNamaMenu.text =
            menu.nama

        val formatRupiah =
            NumberFormat.getInstance(
                Locale("id", "ID")
            )

        holder.txtHarga.text =
            "Rp ${formatRupiah.format(menu.harga.toLong())}"

        holder.layoutKategori.removeAllViews()

        for (kategori in menu.kategori) {

            val txtKategori =
                TextView(holder.itemView.context)

            txtKategori.text =
                kategori

            txtKategori.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.primary
                )
            )

            txtKategori.textSize = 12f

            txtKategori.setPadding(
                22,
                10,
                22,
                10
            )

            txtKategori.background =
                ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.bg_kategori_menu
                )

            val params =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

            params.marginEnd = 10

            txtKategori.layoutParams =
                params

            holder.layoutKategori.addView(
                txtKategori
            )
        }

        Glide.with(holder.itemView.context)
            .load(menu.foto)
            .placeholder(R.drawable.logo_bfc)
            .into(holder.imgMenu)

        holder.btnEdit.setOnClickListener {

            onEditClick(menu)
        }

        holder.btnDelete.setOnClickListener {

            onDeleteClick(menu)
        }
    }
}