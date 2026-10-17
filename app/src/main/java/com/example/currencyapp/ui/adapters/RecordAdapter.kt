package com.example.currencyapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.currencyapp.databinding.ItemRecordBinding

data class RecordRow(val id: String, val title: String, val subtitle: String)
class RecordAdapter(private val click: ((String) -> Unit)? = null) : ListAdapter<RecordRow, RecordAdapter.Holder>(object : DiffUtil.ItemCallback<RecordRow>() {
    override fun areItemsTheSame(old: RecordRow, new: RecordRow) = old.id == new.id
    override fun areContentsTheSame(old: RecordRow, new: RecordRow) = old == new
}) {
    class Holder(val binding: ItemRecordBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(ItemRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val row = getItem(position)
        holder.binding.name.text = row.title
        holder.binding.description.text = row.subtitle
        holder.itemView.isClickable = click != null
        holder.itemView.isFocusable = click != null
        holder.itemView.setOnClickListener(if (click != null) android.view.View.OnClickListener { click.invoke(row.id) } else null)
    }
}

