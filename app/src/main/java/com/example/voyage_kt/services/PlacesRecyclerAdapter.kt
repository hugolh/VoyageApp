package com.example.voyage_kt.services

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.voyage_kt.R
import com.example.voyage_kt.data.Itinerary

class PlacesRecyclerAdapter(
    private val context: Context,
    private val itemList: MutableList<Itinerary>,
    private val showDeleteBool: Boolean
) : RecyclerView.Adapter<PlacesRecyclerAdapter.MyViewHolder>() {

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // Create a new view
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_items, parent, false)
        return MyViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // Get element from your dataset at this position
        val item = itemList[position]

        // Replace the contents of the view with that element
        holder.titleTextView.text = item.name
        holder.descriptionTextView.text = item.description
        holder.textViewPlace.text = holder.textViewPlace.text.toString() + (position + 1).toString()

        // Show or hide the delete button based on the showDeleteBool flag
        holder.deleteIt.visibility = if (showDeleteBool) View.VISIBLE else View.GONE

        // Set click listener for the delete button
        holder.deleteIt.setOnClickListener {
            // Remove the item from the list
            val deletedItem = itemList.removeAt(holder.adapterPosition)

            // Inform the adapter that the item has been removed
            notifyItemRemoved(holder.adapterPosition)

            // You can also handle the deleted item as needed, for example, display a message or perform additional operations.
            // For example, if you want to display a Toast:
            Toast.makeText(context, "Item deleted: ${deletedItem.name}", Toast.LENGTH_SHORT).show()
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return itemList.size
    }

    // Provide a reference to the views for each data item
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewPlace: TextView = itemView.findViewById(R.id.tvPlace)
        val titleTextView: TextView = itemView.findViewById(R.id.tvTitreRv)
        val descriptionTextView: TextView = itemView.findViewById(R.id.tvDescRv)
        val deleteIt: ImageButton = itemView.findViewById(R.id.bDeleteIt)
    }
}
