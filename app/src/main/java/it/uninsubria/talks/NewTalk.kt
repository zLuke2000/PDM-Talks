package it.uninsubria.talks

import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.uninsubria.firebase.Database
import it.uninsubria.firebase.Storage
import java.io.ByteArrayOutputStream


class NewTalk : AppCompatActivity() {
    // Current activity TAG
    private val TAG = "Activity_NewTalk"

    // Firebase
    private val myDB: Database = Database()
    private lateinit var myAuth: FirebaseAuth
    private val myStorage: Storage = Storage()
    private val PICK_IMAGE_REQUEST = 1
    private var imgData: ByteArray? = null
    private var hasImage = false

    // raw view declaration
    private lateinit var etTalkText: TextView
    private lateinit var tilTalkText: TextInputLayout
    private lateinit var etLinkSource: TextView
    private lateinit var ivSelectedImage: ImageView
    private lateinit var bRemoveImage: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Carico il layout
        setContentView(R.layout.activity_new_talk)
        // raw view link
        etTalkText = findViewById(R.id.ET_testoTalk)
        tilTalkText = findViewById(R.id.TIL_testoTalk)
        etLinkSource = findViewById(R.id.ET_linkSource)
        ivSelectedImage = findViewById(R.id.IV_selectedImg)
        bRemoveImage = findViewById(R.id.B_imageRemover)
        myAuth = Firebase.auth
    }

    fun createNewTalk(@Suppress("UNUSED_PARAMETER") v: View) {
        // valori View
        val testoTalk: String = etTalkText.text.toString().trim()
        var linkSource: String = etLinkSource.text.toString().trim()

        if(linkSource.isEmpty()) {
            linkSource = ""
        } else if((!linkSource.startsWith("http://")) and (!linkSource.startsWith("https://"))) {
            linkSource = "http://$linkSource"
        }
        when (testoTalk.length) {
            in 0..3 -> tilTalkText.error = getString(R.string.talkTooShort).replace("$", "4")
            in 4..500 -> {
                tilTalkText.error = null
                myAuth.currentUser?.email?.let {
                    myDB.addTalkToDB(it, testoTalk, linkSource, hasImage) { success, uid ->
                        if (success) {
                            Toast.makeText(baseContext, R.string.talkSent, Toast.LENGTH_SHORT).show()
                            etTalkText.text = ""
                            etLinkSource.text = ""
                            if (hasImage) {
                                Log.e(TAG, "CARICO L'IMMAGINE ")
                                uploadPicture(uid)
                            }
                            removeImage(bRemoveImage)
                        }
                    }
                }
            }
            else -> tilTalkText.error = getString(R.string.talkTooLong).replace("$", "500")
        }
    }

    fun getPicture(@Suppress("UNUSED_PARAMETER") v: View) {
        startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI), PICK_IMAGE_REQUEST)
    }

    fun removeImage(@Suppress("UNUSED_PARAMETER") v: View) {
        hasImage = false
        imgData = null
        ivSelectedImage.setImageBitmap(null)
        bRemoveImage.isVisible = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //Detects request codes
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            val selectedImage: Uri? = data?.data
            val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(
                this.contentResolver,
                selectedImage
            )
            // Compressione
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            imgData = baos.toByteArray()

            val factor = Resources.getSystem().displayMetrics.widthPixels / 1.5F / bitmap.width.toFloat()
            val finalBitmap = Bitmap.createScaledBitmap(
                bitmap,
                (Resources.getSystem().displayMetrics.widthPixels / 1.5F).toInt(),
                (bitmap.height * factor).toInt(),
                true
            )
            ivSelectedImage.setImageBitmap(finalBitmap)
            hasImage = true
            bRemoveImage.isVisible = true
        }
    }

    private fun uploadPicture(talksID: String) {
        if(imgData != null) {
            myStorage.uploadBitmap("TalksImage/${talksID}.jpg", imgData!!) {}
        }
    }
}