package com.example.sneakerhub.helpers

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import com.example.sneakerhub.MyCart
import com.example.sneakerhub.models.shoes

class SQLiteCartHelper(context: Context) : SQLiteOpenHelper(context, "cart.db", null, 2) {

    private val context = context

    override fun onCreate(sql: SQLiteDatabase?) {
        sql?.execSQL("""
            CREATE TABLE IF NOT EXISTS cart (
                shoe_id INTEGER PRIMARY KEY, 
                name TEXT, 
                price TEXT, 
                category_id INTEGER, 
                description TEXT,
                quantity INTEGER,
                photo_url TEXT
            )
        """)
    }

    override fun onUpgrade(sql: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            sql?.execSQL("ALTER TABLE cart ADD COLUMN photo_url TEXT")
        }
    }

    // Insert or update item in the cart
    fun insert(
        shoe_id: String, name: String, price: String, category_id: String,
        description: String, quantity: Int, photo_url: String
    ) {
        val db = this.writableDatabase

        // Check if the item already exists in the cart
        val cursor = db.query(
            "cart", null, "shoe_id=?", arrayOf(shoe_id),
            null, null, null
        )

        val values = ContentValues().apply {
            put("shoe_id", shoe_id)
            put("name", name)
            put("price", price)
            put("category_id", category_id)
            put("description", description)
            put("quantity", quantity)
            put("photo_url", photo_url) // Add the photo URL to the values
        }

        if (cursor.moveToFirst()) {
            // Item exists, update the quantity and other details
            db.update("cart", values, "shoe_id=?", arrayOf(shoe_id))
            Toast.makeText(context, "Item quantity updated in Cart", Toast.LENGTH_SHORT).show()
        } else {
            // Item does not exist, insert new record
            val result: Long = db.insert("cart", null, values)

            if (result == -1L) {
                Toast.makeText(context, "An error occurred while adding to cart", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Item Added to Cart", Toast.LENGTH_SHORT).show()
            }
        }
        cursor.close()
    }

    fun getNumItems(): Int {
        val db = this.readableDatabase
        val result: Cursor = db.rawQuery("SELECT * FROM cart", null)
        return result.count
    }

    fun clearCart() {
        val db = this.writableDatabase
        db.delete("cart", null, null)
        Toast.makeText(context, "Cart Cleared", Toast.LENGTH_SHORT).show()
        val i = Intent(context, MyCart::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(i)
    }

    fun clearCartById(shoe_id: String) {
        val db = this.writableDatabase
        db.delete("cart", "shoe_id=?", arrayOf(shoe_id))
        Toast.makeText(context, "Item Id $shoe_id Removed", Toast.LENGTH_SHORT).show()
        val i = Intent(context, MyCart::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(i)
    }

    fun totalCost(): Double {
        val db = this.readableDatabase
        val result: Cursor = db.rawQuery("SELECT price, quantity FROM cart", null)
        var total: Double = 0.0
        while (result.moveToNext()) {
            val priceString = result.getString(result.getColumnIndex("price"))
            val quantity = result.getInt(result.getColumnIndex("quantity"))
            val price = priceString.toDoubleOrNull() ?: 0.0
            total += price * quantity
        }
        result.close()
        return total
    }

    fun getAllItems(): ArrayList<shoes> {
        val db = this.readableDatabase
        val items = ArrayList<shoes>()
        val result: Cursor = db.rawQuery("SELECT * FROM cart", null)
        while (result.moveToNext()) {
            val model = shoes().apply {
                shoe_id = result.getString(result.getColumnIndex("shoe_id"))
                name = result.getString(result.getColumnIndex("name"))
                price = result.getString(result.getColumnIndex("price"))
                category_id = result.getString(result.getColumnIndex("category_id"))
                description = result.getString(result.getColumnIndex("description"))
                quantity = result.getInt(result.getColumnIndex("quantity")).toString()
                photo_url = result.getString(result.getColumnIndex("photo_url")) // Get the photo URL
            }
            items.add(model)
        }
        result.close()
        return items
    }
}
