package it.uninsubria.talks

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.uninsubria.firebase.firestore.Database
import kotlinx.android.synthetic.main.activity_crea_talk.*

class CreaTalk : AppCompatActivity() {
    private val TAG = "Activity_CreaTalk"
    private lateinit var myAuth: FirebaseAuth

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

        if (testoTalk.length < 4) {
            ET_testoTalk.error = getString(R.string.talkTooShort).replace("$", "4")
        } else if (testoTalk.length > 500) {
            ET_testoTalk.error = getString(R.string.talkTooLong).replace("$", "500")
        } else {
            Database().db.collection("utenti").whereEqualTo("email", myAuth.currentUser.email)
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            for (document in task.result!!) {
                                Log.d(TAG, document.id + " => " + document.data)
                                if (document.data["email"]?.equals(myAuth.currentUser.email)!!) {
                                    Database().addTalkToDB(document.data["nickname"] as String, testoTalk, linkSource) { result ->
                                        if(result) {
                                            Toast.makeText(baseContext, R.string.talkSent, Toast.LENGTH_SHORT).show()
                                            ET_testoTalk.setText("")
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.w(TAG, "[ERRORE] nella lettura degli utenti", task.exception)
                        }
                    }
        }
    }
}