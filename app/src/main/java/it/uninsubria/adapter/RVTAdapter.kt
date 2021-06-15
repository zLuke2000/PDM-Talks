package it.uninsubria.adapter

import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FileDownloadTask
import it.uninsubria.firebase.Storage
import it.uninsubria.talks.MainActivity
import it.uninsubria.talks.R
import it.uninsubria.talks.Talks
import java.io.File

/*
    RVTAdapert -> RecyclerView Talks Adapter
    TRHolder   -> Talk Row Holder
 */

class RVTAdapter(private val talksList: ArrayList<Talks>, private val listener: OnTalkClickListener?) : RecyclerView.Adapter<RVTAdapter.TRHolder>() {
    private val TAG = "RVTAdapter"
    private val myStorage: Storage = Storage()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TRHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.row_talk, parent, false)
        return TRHolder(itemView)
    }

    override fun onBindViewHolder(holder: TRHolder, position: Int) {
        val currentTalk: Talks = talksList[position]

        // aggiorno nickname
        holder.nickname.text = currentTalk.nickname
        // aggiorno contenuto Talk
        holder.content.text = currentTalk.content
        // aggiorno e rendo visibile link fonte (se esistente)
        holder.linkSource.text = currentTalk.linkSource
        if (holder.linkSource.text.isNullOrEmpty()) {
            holder.linkSource.textSize = 0F
        } else {
            holder.linkSource.textSize = 14F
        }
        // aggiorno icona profilo
        myStorage.downloadBitmap("AccountIcon/${currentTalk.nickname}.jpg") { resultBitmap ->
            if(resultBitmap != null) {
                holder.accountIcon.setImageBitmap(resultBitmap)
            } else {
                holder.accountIcon.setImageResource(R.drawable.default_account_image)
            }
        }

        // aggiorno immagine Talk (se esistente)
        myStorage.downloadBitmap("TalksImage/${currentTalk.imagePath}.jpg") { resultBitmap ->
            if(resultBitmap != null) {
                holder.talkImage.setImageBitmap(resultBitmap)
            }
        }
    }

    override fun getItemCount(): Int = talksList.size

    inner class TRHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val nickname : TextView = itemView.findViewById(R.id.TV_nickname)
        val content : TextView = itemView.findViewById(R.id.TV_content)
        val linkSource : TextView = itemView.findViewById(R.id.TV_linkSource)
        val accountIcon : ImageView = itemView.findViewById(R.id.IV_profile)
        val talkImage : ImageView = itemView.findViewById(R.id.IV_content)

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