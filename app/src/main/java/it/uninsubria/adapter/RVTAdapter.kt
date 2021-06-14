package it.uninsubria.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.uninsubria.talks.R
import it.uninsubria.talks.Talks

/*
    RVTAdapert -> RecyclerView Talks Adapter
    TRHolder   -> Talk Row Holder
 */

class RVTAdapter(private val talksList: ArrayList<Talks>, private val listener: OnTalkClickListener?) : RecyclerView.Adapter<RVTAdapter.TRHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TRHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.row_talk, parent, false)
        return TRHolder(itemView)
    }

    override fun onBindViewHolder(holder: TRHolder, position: Int) {
        val currentTalk : Talks = talksList[position]

        holder.nickname.text = currentTalk.nickname
        holder.content.text = currentTalk.content
        holder.linkSource.text = currentTalk.linkSource
        if(holder.linkSource.text.length > 4) {
            holder.linkSource.textSize = 14F
        }
    }
    override fun getItemCount(): Int = talksList.size

    inner class TRHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val nickname : TextView = itemView.findViewById(R.id.TV_nickname)
        val content : TextView = itemView.findViewById(R.id.TV_content)
        val linkSource : TextView = itemView.findViewById(R.id.TV_linkSource)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position: Int = adapterPosition
            if(position != RecyclerView.NO_POSITION) {
                listener?.onTalkclick(position)
            }
        }
    }

    interface OnTalkClickListener {
        fun onTalkclick(position: Int) {
        }
    }
}