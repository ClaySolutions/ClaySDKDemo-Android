package com.salto.claysdkdemo.guest_digital_key.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.salto.claysdkdemo.R
import com.salto.claysdkdemo.models.GuestDigitalKey

class GuestDigitalKeyAdapter(var guestDigitalKeyList: List<GuestDigitalKey>, val listener: IGDKeyClickListener): RecyclerView.Adapter<GuestDigitalKeyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuestDigitalKeyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.guest_dkey_item, parent,false)
        return GuestDigitalKeyViewHolder(view)
    }

    override fun onBindViewHolder(holder: GuestDigitalKeyViewHolder, position: Int) {
        holder.bind(guestDigitalKeyList[position], listener)
    }

    override fun getItemCount(): Int {
        return guestDigitalKeyList.size
    }
}