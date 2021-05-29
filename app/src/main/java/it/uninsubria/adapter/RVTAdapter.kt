package it.uninsubria.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.recyclerview.widget.RecyclerView
import it.uninsubria.talks.R
import it.uninsubria.talks.Talks

class RVTAdapter(private val talksList : ArrayList<Talks>) : RecyclerView.Adapter<RVTAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RVTAdapter.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.row_talk, parent, false)

        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return talksList.size
    }

    override fun onBindViewHolder(holder: RVTAdapter.MyViewHolder, position: Int) {
        val talk : Talks = talksList[position]
        holder.nickname.text = talk.nickname
        holder.content.text = talk.content
    }

    public class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nickname : TextView = itemView.findViewById(R.id.TV_nickname)
        val content : TextView = itemView.findViewById(R.id.TV_content)
    }
}