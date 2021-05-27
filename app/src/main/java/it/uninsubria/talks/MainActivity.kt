package it.uninsubria.talks

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val TAG = "Main_Activity"
    private lateinit var myAuth: FirebaseAuth
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Firebase - authentication
        myAuth = Firebase.auth
    }

    public override fun onStart() {
        super.onStart()
        myAuth.signOut()
        // Check if user is signed in (non-null)
        val currentUser = myAuth.currentUser
        if(currentUser == null) {
            Log.i(TAG, "[MAIN] Passo alla schermata <Login>")
            startActivity(Intent(this, Login::class.java))
        } else {
            setContentView(R.layout.activity_main)
            aggiornamentoInterfaccia()
        }
    }

    private fun aggiornamentoInterfaccia() {
        val layoutInflater = LayoutInflater.from(baseContext)
        val talkRow = layoutInflater.inflate(R.layout.row_talk, null)

        val talkNickname = talkRow.findViewById<TextView>(R.id.TV_nickname)
        val talkContent = talkRow.findViewById<TextView>(R.id.TV_content)

        //DOWNLOAD DATA FROM FIRESTORE DB
        db.collection("talks")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            Log.d(TAG, document.id + " => " + document.data)
                            talkNickname.text = document.data["nickname"] as String
                            talkContent.text = document.data["text"] as String
                            //main_listview.addFooterView(talkRow)
                        }
                    } else {
                        Log.w(TAG, "[ERRORE] nella lettura degli utenti", task.exception)
                    }
                }
    }

    fun openSettings(v: View) {
        startActivity(Intent(this, Settings::class.java))
    }

    fun nuovoPost(v: View) {
        startActivity(Intent(this, CreaTalk::class.java))
    }
}
