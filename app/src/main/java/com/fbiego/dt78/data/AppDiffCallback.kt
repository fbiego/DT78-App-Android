package com.fbiego.dt78.data

import androidx.recyclerview.widget.DiffUtil

class AppDiffCallback(
    private val oldList: List<AppsData>,
    private val newList: List<AppsData>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(p0: Int, p1: Int): Boolean {
        return oldList[p0].name == newList[p1].name
    }

    override fun areContentsTheSame(p0: Int, p1: Int): Boolean {
        return oldList[p0].enabled == newList[p1].enabled
                && oldList[p0].channel == newList[p1].channel
                && oldList[p0].hasFilter() == newList[p1].hasFilter()
    }
}