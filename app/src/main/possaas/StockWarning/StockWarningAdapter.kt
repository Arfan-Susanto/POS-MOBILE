package com.possaas.StockWarning

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.possaas.R

class StockWarningAdapter(

    private val list:
    ArrayList<StockWarningModel>

) : RecyclerView.Adapter<
        StockWarningAdapter.ViewHolder>() {

    class ViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {

        val imgFood:
                ImageView =
            view.findViewById(
                R.id.imgFood
            )

        val txtName:
                TextView =
            view.findViewById(
                R.id.txtName
            )

        val txtStock:
                TextView =
            view.findViewById(
                R.id.txtStock
            )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view =
            LayoutInflater
                .from(parent.context)
                .inflate(
                    R.layout.item_stock_warning,
                    parent,
                    false
                )

        return ViewHolder(view)
    }

    override fun getItemCount():
            Int = list.size

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val item =
            list[position]

        holder.txtName.text =
            item.name

        holder.txtStock.text =
            "Sisa Stok : ${item.stock}"

        Glide.with(holder.itemView.context)
            .load(item.image)
            .placeholder(R.drawable.logo_bfc)
            .into(holder.imgFood)
    }
}