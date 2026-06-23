package com.possaas.FiturAdmin.EditKasir

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.possaas.R
import de.hdodenhof.circleimageview.CircleImageView

class KasirAdapter(

    private val listKasir:
    ArrayList<KasirModel>,

    private val onEditClick:
        (KasirModel) -> Unit,

    private val onDeleteClick:
        (KasirModel) -> Unit

) : RecyclerView.Adapter<KasirAdapter.ViewHolder>() {

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        val imgKasir:
                CircleImageView =
            itemView.findViewById(
                R.id.imgKasir
            )

        val txtNama:
                TextView =
            itemView.findViewById(
                R.id.txtNama
            )

        val txtRole:
                TextView =
            itemView.findViewById(
                R.id.txtRole
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
                R.layout.item_kasir,
                parent,
                false
            )

        return ViewHolder(view)
    }

    override fun getItemCount():
            Int {

        return listKasir.size
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val kasir =
            listKasir[position]

        holder.txtNama.text =
            kasir.name

        // GANTI ROLE JADI SHORT NAME
        holder.txtRole.text =
            kasir.short_name

        Glide.with(holder.itemView.context)
            .load(kasir.profile_image)
            .placeholder(R.drawable.logo_bfc)
            .into(holder.imgKasir)

        holder.btnEdit.setOnClickListener {

            onEditClick(kasir)
        }

        holder.btnDelete.setOnClickListener {

            onDeleteClick(kasir)
        }
    }
}