package it.uninsubria.adapter

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import it.uninsubria.firebase.Database
import it.uninsubria.firebase.Storage
import it.uninsubria.talks.R
import it.uninsubria.models.Talks
import java.io.IOException
import java.lang.Exception

/*
    RVTAdapter -> RecyclerView Talks Adapter
    TRHolder   -> Talk Row Holder
 */

class RVTAdapter(private val parentContext: Context, private val talksList: ArrayList<Talks>, private val listener: OnTalkClickListener?, private val parentWidth: Int, private val userEmail: String?) : RecyclerView.Adapter<RVTAdapter.TRHolder>() {
    // Current class TAG
    private val TAG = "RVTAdapter"

    private val myStorage: Storage = Storage()
    private val myDB: Database = Database()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TRHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.row_talk, parent, false)
        return TRHolder(itemView)
    }

    override fun onBindViewHolder(holder: TRHolder, position: Int) {
        val currentTalk: Talks = talksList[position]

        // update nickname
        holder.nickname.text = currentTalk.nickname
        // update Talk content
        holder.content.text = currentTalk.content
        // update and show source link (if exist)
        holder.linkSource.text = currentTalk.linkSource
        if (holder.linkSource.text.isNullOrEmpty()) {
            holder.linkSource.textSize = 0F
        } else {
            holder.linkSource.textSize = 14F
        }

        // update profile picture
        myDB.getUserPictureStatus(currentTalk.nickname) { result ->
            if (result) {
                myStorage.downloadBitmap("AccountIcon/${currentTalk.nickname}.jpg") { resultBitmap ->
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

        // update Talk image (if exist)
        if(currentTalk.hasImage == true) {
            Log.e(TAG, "Imposto l'immagine per: ${currentTalk.nickname}" + " - " + "${currentTalk.content}")
            myStorage.downloadBitmap("TalksImage/${currentTalk.imagePath}.jpg") { resultBitmap ->
                if (resultBitmap != null) {
                    val factor = parentWidth / resultBitmap.width.toFloat()
                    val finalBitmap = Bitmap.createScaledBitmap(resultBitmap, parentWidth, (resultBitmap.height * factor).toInt(), true)
                    holder.talkImage.setImageBitmap(finalBitmap)
                } else {
                    holder.talkImage.setImageDrawable(null)
                }
            }
        } else {
            holder.talkImage.setImageDrawable(null)
        }

        // enable talk remove icon (if the current user is the owner)
        myDB.getNicknameByEmail(userEmail) { result ->
            if (currentTalk.nickname.equals(result)) {
                holder.deleteTalkImage.isVisible = true
                holder.deleteTalkImage.isClickable = true
                holder.deleteTalkImage.setOnClickListener{ currentTalk.imagePath?.let { it1 -> deleteCurrentTalk(it1) } }
            } else {
                holder.deleteTalkImage.isVisible = false
                holder.deleteTalkImage.isClickable = false
                holder.deleteTalkImage.setOnClickListener {}
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
        val deleteTalkImage: ImageView = itemView.findViewById(R.id.IV_delete)

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
        fun talkClick(position: Int) {}
    }

    private fun deleteCurrentTalk(uid: String) {
        Log.i(TAG, "DELETE: $uid")
        myStorage.deleteBitmap("TalksImage/$uid.jpg")
        myDB.deleteTalks(uid) { result ->
            if(result) {
                Toast.makeText(parentContext, R.string.talkDeletedOK, Toast.LENGTH_SHORT).show()
                this.notifyDataSetChanged()
            } else {
                Toast.makeText(parentContext, R.string.talkDeletedKO, Toast.LENGTH_SHORT).show()
            }
        }
    }
}