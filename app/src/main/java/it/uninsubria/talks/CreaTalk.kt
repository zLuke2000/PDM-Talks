package it.uninsubria.talks

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
    private val myDB: Database = Database()

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
            in 4..500 -> {myDB.addTalkToDB(myAuth.currentUser.email, testoTalk, linkSource) { success ->
                            if (success) {
                                Toast.makeText(baseContext, R.string.talkSent, Toast.LENGTH_SHORT).show()
                                ET_testoTalk.setText("")
                                ET_linkSource.setText("")
                            }
                        }}
            else -> ET_testoTalk.error = getString(R.string.talkTooLong).replace("$", "500")
        }
    }
}