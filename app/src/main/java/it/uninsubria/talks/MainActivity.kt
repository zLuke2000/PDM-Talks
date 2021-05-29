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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import it.uninsubria.adapter.RVTAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val TAG = "Main_Activity"
    private lateinit var myAuth: FirebaseAuth
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var talksArrayList: ArrayList<Talks>
    private lateinit var rvtAdapter: RVTAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Firebase - authentication
        myAuth = Firebase.auth
        setContentView(R.layout.activity_main)
        recyclerView = talksRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        talksArrayList = arrayListOf()

        rvtAdapter = RVTAdapter(talksArrayList)

        recyclerView.adapter = rvtAdapter
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null)
        val currentUser = myAuth.currentUser
        if(currentUser == null) {
            Log.i(TAG, "[MAIN] Passo alla schermata <Login>")
            startActivity(Intent(this, Login::class.java))
        } else {
            eventChangeListener()
        }

        swipeRefreshLayout.setOnRefreshListener {
            eventChangeListener()
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun eventChangeListener() {
        //DOWNLOAD DATA FROM FIRESTORE DB
        db.collection("talks")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(object : EventListener<QuerySnapshot> {
            override fun onEvent(value: QuerySnapshot?,
                                 error: FirebaseFirestoreException?) {
                if(error != null) {
                    Log.e(TAG, "Firestore error: " + error.message.toString())
                    return
                }
                talksArrayList.clear()
                for(dc : DocumentChange in value?.documentChanges!!) {
                    if(dc.type == DocumentChange.Type.ADDED) {
                        talksArrayList.add(dc.document.toObject(Talks::class.java))
                    }
                }
                rvtAdapter.notifyDataSetChanged()
            }
        })
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