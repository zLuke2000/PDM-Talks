package it.uninsubria.talks

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.uninsubria.firebase.Database
import it.uninsubria.firebase.Storage
import java.io.ByteArrayOutputStream

class CreaTalk : AppCompatActivity() {
    // Current activity TAG
    private val TAG = "Activity_CreaTalk"
    // Database
    private val myDB: Database = Database()
    // Firebase Authentication
    private lateinit var myAuth: FirebaseAuth
    // Firebase Storage
    private val myStorage: Storage = Storage()
    private val PICK_IMAGE_REQUEST = 1
    private var imgData: ByteArray? = null
    // raw view declaration
    private lateinit var etTalkText: TextView
    private lateinit var etLinkSource: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Carico il layout
        setContentView(R.layout.activity_crea_talk)
        // raw view link
        etTalkText = findViewById(R.id.ET_testoTalk)
        etLinkSource = findViewById(R.id.ET_linkSource)
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
            in 0..3 -> etTalkText.error = getString(R.string.talkTooShort).replace("$", "4")
            in 4..500 -> {
                myAuth.currentUser?.email?.let {
                    myDB.addTalkToDB(it, testoTalk, linkSource) { success, uid ->
                        if (success) {
                            Toast.makeText(baseContext, R.string.talkSent, Toast.LENGTH_SHORT).show()
                            etTalkText.text = ""
                            etTalkText.text = ""
                            uploadPicture(uid)
                        }
                    }
                }
            }
            else -> etTalkText.error = getString(R.string.talkTooLong).replace("$", "500")
        }
    }

    fun getPicture(@Suppress("UNUSED_PARAMETER") v: View) {
        startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //Detects request codes
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            val selectedImage: Uri? = data?.data
            val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
            // Compressione
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            imgData = baos.toByteArray()
        }
    }

    private fun uploadPicture(talksID: String) {
        if(imgData != null) {
            myStorage.uploadBitmap("TalksImage/${talksID}.jpg", imgData!!) {}
        }
    }
}