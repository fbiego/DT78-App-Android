package com.fbiego.dt78.data

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.fbiego.dt78.R

class MeasureAdapter(measureData: ArrayList<MeasureData>): RecyclerView.Adapter<MeasureAdapter.DataHolder>() {
    private val data = mutableListOf<MeasureData>()


    init {
        data.addAll(measureData)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): DataHolder {
        val inflater = LayoutInflater.from(p0.context)
        val view = inflater.inflate(R.layout.measure_item, p0, false)
        return DataHolder(view, p0.context)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(p0: DataHolder, p1: Int) {
        p0.bind(data[p1])
    }

    fun update(measureData: ArrayList<MeasureData>){
        this.data.clear()
        this.data.addAll(measureData)
        this.notifyDataSetChanged()
    }

    class DataHolder(itemView: View, private val cnt: Context) : RecyclerView.ViewHolder(itemView){
        private val mTime: TextView = itemView.findViewById(R.id.time)
        private val mValue: TextView = itemView.findViewById(R.id.value)

        @SuppressLint("DefaultLocale")
        fun bind (measure: MeasureData){
            mTime.text = measure.time
            mValue.text = measure.text
        }

    }
}