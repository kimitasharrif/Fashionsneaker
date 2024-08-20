package com.example.sneakerhub.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sneakerhub.R
import com.example.sneakerhub.helpers.SQLiteCartHelper
import com.example.sneakerhub.models.shoes
import com.google.android.material.button.MaterialButton
import com.squareup.picasso.Picasso

class ShoesCartAdapter(private val context: Context) :
    RecyclerView.Adapter<ShoesCartAdapter.ViewHolder>() {

    private var itemList: List<shoes> = listOf()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.item_name)
        val quantity: TextView = itemView.findViewById(R.id.cart_quantity)
        val price: TextView = itemView.findViewById(R.id.item_price)
        val remove: MaterialButton = itemView.findViewById(R.id.cartremove)
        val imageView:ImageView = itemView.findViewById(R.id.item_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.single_cart, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.name.text = item.name
        holder.quantity.text = "Quantity: ${item.quantity}"
        holder.price.text = "Price: ${item.price}"

        // Load the image using Picasso or Glide
        Picasso.get().load(item.photo_url).into(holder.imageView)

        holder.remove.setOnClickListener {
            val shoe_id = item.shoe_id
            val helper = SQLiteCartHelper(context)
            helper.clearCartById(shoe_id)
            // Optional: Update the itemList and notify the adapter of the change
            val updatedList = itemList.filter { it.shoe_id != shoe_id }
            setListItems(updatedList)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun setListItems(data: List<shoes>) {
        itemList = data
        notifyDataSetChanged()
    }
}
