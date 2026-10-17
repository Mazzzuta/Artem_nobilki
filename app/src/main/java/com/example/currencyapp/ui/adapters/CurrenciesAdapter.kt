package com.example.currencyapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.currencyapp.R
import com.example.currencyapp.data.local.entity.CurrencyEntity

class CurrenciesAdapter : ListAdapter<CurrencyEntity, CurrenciesAdapter.CurrencyViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_currency, parent, false)
        return CurrencyViewHolder(view)
    }

    override fun onBindViewHolder(holder: CurrencyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CurrencyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCode: TextView = itemView.findViewById(R.id.tvCurrencyCode)
        private val tvName: TextView = itemView.findViewById(R.id.tvCurrencyName)
        private val tvRate: TextView = itemView.findViewById(R.id.tvCurrencyRate)

        fun bind(item: CurrencyEntity) {
            tvCode.text = item.code
            tvName.text = item.name
            tvRate.text = "%.4f".format(item.currentRate)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CurrencyEntity>() {
        override fun areItemsTheSame(oldItem: CurrencyEntity, newItem: CurrencyEntity): Boolean =
            oldItem.code == newItem.code

        override fun areContentsTheSame(oldItem: CurrencyEntity, newItem: CurrencyEntity): Boolean =
            oldItem == newItem
    }
}