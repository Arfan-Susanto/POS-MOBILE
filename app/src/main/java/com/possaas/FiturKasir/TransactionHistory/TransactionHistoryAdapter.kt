package com.possaas.FiturKasir.TransactionHistory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.possaas.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TransactionStatus {
    BERHASIL, PROSES
}

data class TransactionItem(
    val invoiceId: String,
    val customerName: String,
    val cashierName: String = "",
    val timestamp: Long,
    val status: TransactionStatus
)

class TransactionHistoryAdapter(
    private val transactions: List<TransactionItem>,
    private val isAdminView: Boolean = false,
    private val onItemClick: (invoiceId: String) -> Unit = {}
) : RecyclerView.Adapter<TransactionHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtCustomer: TextView = itemView.findViewById(R.id.txtCustomer)
        val txtInvoice: TextView? = itemView.findViewById(R.id.txtInvoice)
        val txtDateTime: TextView = itemView.findViewById(R.id.txtDateTime)
        val txtStatus: TextView = itemView.findViewById(R.id.txtStatus)
        val statusBadge: LinearLayout = itemView.findViewById(R.id.statusBadge)

        fun bind(transaction: TransactionItem) {
            itemView.setOnClickListener {
                onItemClick(transaction.invoiceId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutRes = if (isAdminView) R.layout.item_transaction_history_admin else R.layout.item_transaction_history
        val view = LayoutInflater.from(parent.context)
            .inflate(layoutRes, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = transactions.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]

        if (isAdminView) {
            holder.txtCustomer.text = transaction.customerName
            val accentColor = holder.itemView.context.getColor(R.color.accent)
            holder.txtCustomer.setTextColor(accentColor)
            holder.txtDateTime.setTextColor(accentColor)
        } else {
            holder.txtCustomer.text = transaction.customerName
            holder.txtInvoice?.text = transaction.invoiceId
        }

        val dateFormat = if (isAdminView) {
            SimpleDateFormat("d MMMM yyyy", Locale("id", "ID"))
        } else {
            SimpleDateFormat("d MMMM yyyy - HH:mm", Locale("id", "ID"))
        }
        val dateString = dateFormat.format(Date(transaction.timestamp))
        holder.txtDateTime.text = dateString

        when (transaction.status) {
            TransactionStatus.BERHASIL -> {
                holder.txtStatus.text = "BERHASIL"
                holder.txtStatus.setTextColor(holder.itemView.context.getColor(R.color.success_text))
                holder.statusBadge.setBackgroundResource(R.drawable.bg_status_success_badge)
            }
            TransactionStatus.PROSES -> {
                holder.txtStatus.text = "PROSES"
                holder.txtStatus.setTextColor(holder.itemView.context.getColor(R.color.accent))
                holder.statusBadge.setBackgroundResource(R.drawable.bg_status_process_badge)
            }
        }

        holder.bind(transaction)
    }
}