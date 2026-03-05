package com.example.impresionetiquetas.Adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.impresionetiquetas.R
import com.example.impresionetiquetas.model.ProductoStockResponse

class StockAdapter(
    private val lista: List<ProductoStockResponse>
) : RecyclerView.Adapter<StockAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tvUbicacion: TextView =
            view.findViewById(R.id.tvUbicacion)

        val tvStock: TextView =
            view.findViewById(R.id.tvStock)

        val tvAlmacen: TextView =
            view.findViewById(R.id.tvAlmacen)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_stock, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#F5F5F5"));
        }

        val stock = lista[position]

        holder.tvUbicacion.text = stock.ubicacion
        holder.tvAlmacen.text = stock.almacen
        holder.tvStock.text = stock.stockActual.toString()

        when {

            stock.stockActual == 0.0 -> {
                holder.tvStock.setTextColor(Color.RED)
            }

            stock.stockActual <= 10 -> {
                holder.tvStock.setTextColor(Color.parseColor("#FFA500"))
            }

            else -> {
                holder.tvStock.setTextColor(Color.parseColor("#008000"))
            }
        }
    }
}