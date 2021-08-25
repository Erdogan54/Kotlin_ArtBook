package com.ozgurerdogan.kotlin_artbook

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ozgurerdogan.kotlin_artbook.databinding.RecyclerRowBinding

class RcAdapter(val arrayModel:ArrayList<ArtModel>): RecyclerView.Adapter<RcAdapter.RcHolder>() {


    class RcHolder(val binding:RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RcHolder {
        val binding=RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return RcHolder(binding)
    }


    override fun onBindViewHolder(holder: RcHolder, position: Int) {
        holder.binding.recyclerTextView.text=arrayModel.get(position).id.toString()+". "+arrayModel.get(position).artName

        holder.itemView.setOnClickListener {
            val intent= Intent(holder.itemView.context,AddArtActivity::class.java)
            intent.putExtra("model",arrayModel.get(position))
            intent.putExtra("info","old")
            holder.itemView.context.startActivity(intent)
        }
    }


    override fun getItemCount(): Int {
        return arrayModel.size
    }
}