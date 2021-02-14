package com.fbiego.dt78.data

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.fbiego.dt78.R

class ErrorAdapter(private val errorData: ArrayList<ErrorData>,private val context: Context): RecyclerView.Adapter<ErrorAdapter.DataHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): DataHolder {
        val inflater = LayoutInflater.from(p0.context)
        val view = inflater.inflate(R.layout.error_item, p0, false)
        return DataHolder(view)
    }

    override fun getItemCount(): Int = errorData.size

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(p0: DataHolder, p1: Int) {
        p0.bind(errorData[p1], context)
    }



    class DataHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val mName: TextView = itemView.findViewById(R.id.errorName)
        private val mDate: TextView = itemView.findViewById(R.id.errorDate)
        private val mIcon: ImageView = itemView.findViewById(R.id.errorIcon)

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        fun bind (errorData: ErrorData, context: Context){

            mName.text = getErrorName(errorData.error)
            mDate.text = String.format("%02d:%02d:%02d", errorData.hour, errorData.minute, errorData.second)
            mIcon.setImageResource(getErrorIcon(errorData.error))
            mIcon.imageTintList = ColorStateList.valueOf(context.getColorFromAttr(R.attr.colorIcons))

        }

    }

}