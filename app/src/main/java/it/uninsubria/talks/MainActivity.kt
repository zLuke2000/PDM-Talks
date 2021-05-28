package it.uninsubria.talks

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import it.uninsubria.adapter.ListAdapter
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
        var nicknameList = mutableListOf<String>()
        var contentList = mutableListOf<String>()

        //DOWNLOAD DATA FROM FIRESTORE DB
        db.collection("talks")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            Log.d(TAG, document.id + " => " + document.data)
                            nicknameList.add(document.data["nickname"] as String)
                            contentList.add(document.data["text"] as String)
                        }
                    } else {
                        Log.w(TAG, "[ERRORE] nella lettura degli utenti", task.exception)
                    }
                }

        val myListAdapter = ListAdapter(this, nicknameList, contentList)
        main_listview.adapter = myListAdapter

        main_listview.setOnItemClickListener(){adapterView, view, position, id ->
            val itemAtPos = adapterView.getItemAtPosition(position)
            val itemIdAtPos = adapterView.getItemIdAtPosition(position)
            Toast.makeText(this, "Click on item at $itemAtPos its item id $itemIdAtPos", Toast.LENGTH_LONG).show()
        }
    }

    fun openSettings(v: View) {
        val currentUIMode: String
        val settingsPopup = PopupMenu(this, settingsButton)
        settingsPopup.menuInflater.inflate(R.menu.popup_settings, settingsPopup.menu)

        if((getDefaultNightMode() == MODE_NIGHT_UNSPECIFIED) or (getDefaultNightMode() == MODE_NIGHT_NO)) {
            currentUIMode = "Light"
            settingsPopup.menu.findItem(R.id.nightModeButton).setTitle(R.string.switchToNight)
        } else {
            currentUIMode = "Night"
            settingsPopup.menu.findItem(R.id.nightModeButton).setTitle(R.string.switchToLight)
        }

        settingsPopup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.nightModeButton -> switchUIMode(currentUIMode)
                R.id.logoutButton -> doLogout()
            }
            true
        }
        settingsPopup.show()
    }

    private fun switchUIMode(mode: String) {
        if(mode == "Light") {
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
        }
    }

    private fun doLogout() {
        myAuth.signOut()
        Toast.makeText(baseContext, R.string.logOut, Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, Login::class.java))
    }

    fun newPost(v: View) {
        startActivity(Intent(this, CreaTalk::class.java))
    }
}