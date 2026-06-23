package com.possaas.FiturAdmin.EditStok

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.possaas.R
import de.hdodenhof.circleimageview.CircleImageView

class StockAdapter(

    private val listStock:
    ArrayList<StockModel>,

    private val onStockChanged:
        () -> Unit

) : RecyclerView.Adapter<StockAdapter.ViewHolder>() {

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        val imgMenu:
                CircleImageView =
            itemView.findViewById(
                R.id.imgMenu
            )

        val txtNama:
                TextView =
            itemView.findViewById(
                R.id.txtNama
            )

        val txtJumlah:
                TextView =
            itemView.findViewById(
                R.id.txtJumlah
            )

        val btnMinus:
                LinearLayout =
            itemView.findViewById(
                R.id.btnMinus
            )

        val btnPlus:
                LinearLayout =
            itemView.findViewById(
                R.id.btnPlus
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
                R.layout.item_stock,
                parent,
                false
            )

        return ViewHolder(view)
    }

    override fun getItemCount():
            Int {

        return listStock.size
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val item =
            listStock[position]

        holder.txtNama.text =
            item.nama

        holder.txtJumlah.text =
            item.stok.toString()

        Glide.with(holder.itemView.context)
            .load(item.foto)
            .placeholder(R.drawable.logo_bfc)
            .into(holder.imgMenu)

        holder.btnPlus.setOnClickListener {

            if (item.stok < 99) {

                item.stok++

                holder.txtJumlah.text =
                    item.stok.toString()

                onStockChanged()
            }
        }

        holder.btnMinus.setOnClickListener {

            if (item.stok > 0) {

                item.stok--

                holder.txtJumlah.text =
                    item.stok.toString()

                onStockChanged()
            }
        }
    }
}