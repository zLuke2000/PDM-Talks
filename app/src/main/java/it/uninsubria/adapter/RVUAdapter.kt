package it.uninsubria.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.uninsubria.firebase.Storage
import it.uninsubria.talks.R
import it.uninsubria.models.User

/*
 *  RVUAdapter -> RecyclerView User Adapter
 *  TRHolder   -> Talk Row Holder
 */

class RVUAdapter(private val usersList: ArrayList<User>, private val listener: OnTalkClickListener?) : RecyclerView.Adapter<RVUAdapter.TRHolder>() {
    private val TAG = "RVTAdapter"

    private val myStorage: Storage = Storage()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TRHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.row_talk, parent, false)
        return TRHolder(itemView)
    }

    override fun onBindViewHolder(holder: TRHolder, position: Int) {
        val currentUser: User = usersList[position]

        // aggiorno nickname
        holder.nickname.text = currentUser.nickname
        // aggiorno nome e cognome
        holder.content.text = (currentUser.surname + currentUser.name)
        // aggiorno icona profilo
        myStorage.downloadBitmap("AccountIcon/${currentUser.nickname}.jpg") { resultBitmap ->
            if(resultBitmap != null) {
                holder.accountIcon.setImageBitmap(resultBitmap)
            } else {
                holder.accountIcon.setImageResource(R.drawable.default_account_image)
            }
        }
    }

    override fun getItemCount(): Int = usersList.size

    inner class TRHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val nickname : TextView = itemView.findViewById(R.id.TV_nickname)
        val content : TextView = itemView.findViewById(R.id.TV_content)
        val accountIcon : ImageView = itemView.findViewById(R.id.IV_profile)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position: Int = adapterPosition
            if(position != RecyclerView.NO_POSITION) {
                listener?.talkClick(position)
            }
        }
    }

    interface OnTalkClickListener {
        fun talkClick(position: Int) {
        }
    }
}