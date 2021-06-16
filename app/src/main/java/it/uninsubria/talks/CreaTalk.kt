package it.uninsubria.talks

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import it.uninsubria.firebase.Database
import it.uninsubria.firebase.Storage
import kotlinx.android.synthetic.main.activity_crea_talk.*
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
    private lateinit var imgData: ByteArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crea_talk)
        myAuth = Firebase.auth
    }

    fun createNewTalk(v: View) {
        val testoTalk: String = ET_testoTalk.text.toString().trim()
        var linkSource: String = ET_linkSource.text.toString().trim()

        if(linkSource.isEmpty()) {
            linkSource = ""
        } else if((!linkSource.startsWith("http://")) and (!linkSource.startsWith("https://"))) {
            linkSource = "http://$linkSource";
        }
        when (testoTalk.length) {
            in 0..3 -> ET_testoTalk.error = getString(R.string.talkTooShort).replace("$", "4")
            in 4..500 -> {
                myAuth.currentUser?.email?.let {
                    myDB.addTalkToDB(it, testoTalk, linkSource) { success, uid ->
                        if (success) {
                            Toast.makeText(baseContext, R.string.talkSent, Toast.LENGTH_SHORT).show()
                            ET_testoTalk.setText("")
                            ET_linkSource.setText("")
                            uploadPicture(uid)
                        }
                    }
                }
            }
            else -> ET_testoTalk.error = getString(R.string.talkTooLong).replace("$", "500")
        }
    }

    fun getPicture(view: View) {
        startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //Detects request codes
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            val selectedImage: Uri? = data?.data
            var bitmap: Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
            // Compressione
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            imgData = baos.toByteArray()
        }
    }

    private fun uploadPicture(talksID: String) {
        myStorage.uploadBitmap("TalksImage/${talksID}.jpg", imgData) {}
    }
}