package com.salto.claysdkdemo.guest_digital_key.list

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.salto.claysdkdemo.databinding.GuestDkeyItemBinding
import com.salto.claysdkdemo.models.GuestDigitalKey
import java.util.*

class GuestDigitalKeyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val binding = GuestDkeyItemBinding.bind(view)

    fun bind(key: GuestDigitalKey, listener: IGDKeyClickListener) {
        binding.apply {
            dkeyNameValue.text = "${key.firstName} ${key.lastName}"
            dkeyDeviceValue.text = "${key.device?.deviceName}"
            dkeyDateValue.text = key.getDateString()

            root.setOnClickListener {
                listener.clicked(key)
            }
        }
    }
}