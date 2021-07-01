package it.uninsubria.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.uninsubria.firebase.Storage
import it.uninsubria.talks.R
import it.uninsubria.models.Profile

/*
 *  RVUAdapter -> RecyclerView User Adapter
 *  TRHolder   -> Talk Row Holder
 */

class RVUAdapter(private val usersList: ArrayList<Profile>, private val listener: OnTalkClickListener?) : RecyclerView.Adapter<RVUAdapter.TRHolder>() {
    // Current class TAG
    private val TAG = "RVTAdapter"

    private val myStorage: Storage = Storage()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TRHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.row_talk, parent, false)
        return TRHolder(itemView)
    }

    override fun onBindViewHolder(holder: TRHolder, position: Int) {
        val currentProfile: Profile = usersList[position]

        // update nickname
        holder.nickname.text = currentProfile.nickname
        // update name and surname
        holder.content.text = (currentProfile.surname + currentProfile.name)
        // update profile picture
        if(currentProfile.hasPicture == true) {
            myStorage.downloadBitmap("AccountIcon/${currentProfile.nickname}.jpg") { resultBitmap ->
                if (resultBitmap != null) {
                    holder.accountIcon.setImageBitmap(resultBitmap)
                } else {
                    holder.accountIcon.setImageResource(R.drawable.default_account_image)
                }
            }
        } else {
            holder.accountIcon.setImageResource(R.drawable.default_account_image)
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