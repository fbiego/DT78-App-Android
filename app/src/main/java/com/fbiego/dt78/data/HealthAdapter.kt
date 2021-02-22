package com.fbiego.dt78.data

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.fbiego.dt78.R

class HealthAdapter(healthData: ArrayList<HealthData>): RecyclerView.Adapter<HealthAdapter.DataHolder>() {
    private val data = mutableListOf<HealthData>()


    init {
        data.addAll(healthData)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): DataHolder {
        val inflater = LayoutInflater.from(p0.context)
        val view = inflater.inflate(R.layout.health_item, p0, false)
        return DataHolder(view, p0.context)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(p0: DataHolder, p1: Int) {
        p0.bind(data[p1])
    }

    fun update(healthData: ArrayList<HealthData>){
        this.data.clear()
        this.data.addAll(healthData)
        this.notifyDataSetChanged()
    }

    class DataHolder(itemView: View, private val cnt: Context) : RecyclerView.ViewHolder(itemView){
        private val mDate: TextView = itemView.findViewById(R.id.date)
        private val mTime: TextView = itemView.findViewById(R.id.time)
        private val mValue: TextView = itemView.findViewById(R.id.value)
        //private val card: CardView = itemView.findViewById(R.id.cardView)

        @SuppressLint("DefaultLocale")
        fun bind (health: HealthData){
            val date = java.lang.String.format("%02d-%02d-20%02d", health.day, health.month,health.year)
            mDate.text = date
            val time = java.lang.String.format("%02d:%02d", health.hour, health.minute)
            mTime.text = time
            mValue.text = health.value
            //card.setBackgroundColor(colors(health.day%10))
            //card.setCardBackgroundColor(ContextCompat.getColor(cnt, R.color.colorAccent))
        }

    }
}