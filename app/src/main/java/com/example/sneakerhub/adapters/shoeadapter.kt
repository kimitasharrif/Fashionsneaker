package com.example.sneakerhub.adapters


import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sneakerhub.R
import com.example.sneakerhub.ShoeDetailActivity
import com.example.sneakerhub.models.shoes
import com.google.android.material.textview.MaterialTextView

class shoeadapter(
    private val context: Context,
    private val onItemClick: (shoes) -> Unit
) : RecyclerView.Adapter<shoeadapter.ViewHolder>() {

    private var itemList2: List<shoes> = listOf()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.single_shoe,
            parent, false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val brand = holder.itemView.findViewById<MaterialTextView>(R.id.brandname)
        val shoe = holder.itemView.findViewById<MaterialTextView>(R.id.shoename)
        val price = holder.itemView.findViewById<MaterialTextView>(R.id.price)
        val image = holder.itemView.findViewById<ImageView>(R.id.shoe_image)

        val item = itemList2[position]

        // Apply default values if item properties are null
        brand.text = item.brand_name ?: "Unknown Brand"
        shoe.text = item.name ?: "Unknown Shoe"
        price.text = "KES ${item.price ?: "0"}"

        // Use Glide for image loading
        Glide.with(context)
            .load(item.photo_url ?: R.drawable.sh1) // Use default image or placeholder
            .into(image)

        holder.itemView.setOnClickListener {
            onItemClick(item)
            val i = Intent(context, ShoeDetailActivity::class.java)
            i.putExtra("category_id", item.category_id)
            i.putExtra("shoe_id", item.shoe_id)
            i.putExtra("price", item.price)
            i.putExtra("name", item.name)
            i.putExtra("description", item.description)
            i.putExtra("brand_name", item.brand_name)
            i.putExtra("quantity", item.quantity)
            i.putExtra("photo_url", item.photo_url) // Pass photo_url directly
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(i)
        }
        holder.itemView.visibility = View.VISIBLE
    }

    override fun getItemCount(): Int {
        return itemList2.size
    }

    // For filtering data
    fun filterList(filterList: List<shoes>) {
        itemList2 = filterList
        notifyDataSetChanged()
    }

    fun setListItems(data: List<shoes>) {
        itemList2 = data
        notifyDataSetChanged()
    }
}
