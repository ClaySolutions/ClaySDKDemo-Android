package com.salto.claysdkdemo.guest_digital_key

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.salto.claysdkdemo.access_code.AccessCodeActivity
import com.salto.claysdkdemo.base.SaltoActivity
import com.salto.claysdkdemo.databinding.ActivityGuestDigitalKeysBinding
import com.salto.claysdkdemo.guest_digital_key.list.GuestDigitalKeyAdapter
import com.salto.claysdkdemo.guest_digital_key.list.IGDKeyClickListener
import com.salto.claysdkdemo.guest_digital_key.presenters.IGuestDigitalKeysListPresenter
import com.salto.claysdkdemo.models.GuestDigitalKey
import com.salto.claysdkdemo.send_dkey.SendDKeyActivity

class GuestDigitalKeysListActivity : SaltoActivity<IGuestDigitalKeysListPresenter.View, IGuestDigitalKeysListPresenter.Action>(),
    IGuestDigitalKeysListPresenter.View, IGDKeyClickListener {

    lateinit var binding: ActivityGuestDigitalKeysBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuestDigitalKeysBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
        configureRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        presenter.fetchDigitalKeysForGuest()
    }

    private fun setListeners() {
        binding.addKeyButton.setOnClickListener {
            startActivity(Intent(this, AccessCodeActivity::class.java))
        }
    }

    override fun bindView() {
        presenter.bindView(this)
    }

    var adapter: GuestDigitalKeyAdapter? = null

    override fun onDigitalKeysForGuestRetrieved(keys: List<GuestDigitalKey>) {
        adapter?.guestDigitalKeyList = keys
        adapter?.notifyDataSetChanged()
    }

    private fun configureRecyclerView() {
        adapter = GuestDigitalKeyAdapter(arrayListOf(), this)
        binding.gdkList.layoutManager = LinearLayoutManager(this)
        binding.gdkList.setHasFixedSize(true)

        binding.gdkList.adapter = adapter
    }

    override fun clicked(key: GuestDigitalKey) {
        Intent(this, SendDKeyActivity::class.java).apply {
            putExtra("GDKEY", key)
            startActivity(this)
        }
    }
}