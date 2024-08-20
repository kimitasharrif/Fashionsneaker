package com.example.sneakerhub.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.sneakerhub.R
import com.example.sneakerhub.models.category

class categoryadapter(
    private val context: Context,
    private val onItemClick: (category) -> Unit
) : RecyclerView.Adapter<categoryadapter.ViewHolder>() {

    private var itemList: List<category> = listOf()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_category,
            parent, false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = itemList[position]
        val button = holder.itemView.findViewById<Button>(R.id.category_button) // Example ID

        button.text = category.category_name // Set category name
        button.setOnClickListener {
            onItemClick(category)
        }
        button.visibility = View.VISIBLE
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun setListItems(data: List<category>) {
        itemList = data
        notifyDataSetChanged()
    }
}
