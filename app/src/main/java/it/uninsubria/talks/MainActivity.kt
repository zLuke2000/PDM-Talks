package it.uninsubria.talks

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import it.uninsubria.adapter.RVTAdapter
import it.uninsubria.firebase.firestore.Database
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), RVTAdapter.OnTalkClickListener {
    private val TAG = "Main_Activity"
    private lateinit var myAuth: FirebaseAuth
    private lateinit var talksArrayList: ArrayList<Talks>
    private lateinit var rvtAdapter: RVTAdapter
    private var nightMode: Int = getDefaultNightMode();
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var settingsPopup: PopupMenu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Firebase - authentication
        myAuth = Firebase.auth

        //Recycler View
        setContentView(R.layout.activity_main)
        UserTalksRecyclerView.layoutManager = LinearLayoutManager(this)
        UserTalksRecyclerView.setHasFixedSize(true)
        talksArrayList = arrayListOf()
        rvtAdapter = RVTAdapter(talksArrayList, this)
        UserTalksRecyclerView.adapter = rvtAdapter

        //Dark-Light Mode
        sharedPreferences = getSharedPreferences("SharedPrefs", MODE_PRIVATE);
        nightMode = sharedPreferences.getInt("NightModeIntStatus", MODE_NIGHT_NO)
        setDefaultNightMode(nightMode)

        settingsPopup = PopupMenu(this, settingsButton)
        settingsPopup.menuInflater.inflate(R.menu.popup_settings, settingsPopup.menu)
        if((nightMode == MODE_NIGHT_NO)) {
            settingsPopup.menu.findItem(R.id.nightModeButton).setTitle(R.string.switchToNight)
        } else {
            settingsPopup.menu.findItem(R.id.nightModeButton).setTitle(R.string.switchToLight)
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null)
        val currentUser = myAuth.currentUser
        if(currentUser == null) {
            Log.i(TAG, "[MAIN] Passo alla schermata <Login>")
            startActivity(Intent(this, Login::class.java))
        } else {
            talksArrayList.clear()
            eventChangeListener()
        }

        swipeRefreshLayout.setOnRefreshListener {
            talksArrayList.clear()
            eventChangeListener()
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun eventChangeListener() {
        //DOWNLOAD DATA FROM FIRESTORE DB
        Database().db.collection("talks")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(object : EventListener<QuerySnapshot> {
                    override fun onEvent(value: QuerySnapshot?,
                                 error: FirebaseFirestoreException?) {
                if(error != null) {
                    Log.e(TAG, "Firestore error: " + error.message.toString())
                    return
                }
                for(dc : DocumentChange in value?.documentChanges!!) {
                    if(dc.type == DocumentChange.Type.ADDED) {
                        talksArrayList.add(dc.document.toObject(Talks::class.java))
                    }
                }
                rvtAdapter.notifyDataSetChanged()
            }
        })
    }

    override fun onTalkclick(position: Int) {
        val intent = Intent(this, Profilo::class.java)
        intent.putExtra("NICKNAME", talksArrayList[position].nickname)
        startActivity(intent)
    }

    fun openSettings(v: View) {
        if(nightMode == MODE_NIGHT_NO) {
            settingsPopup.menu.findItem(R.id.nightModeButton).setTitle(R.string.switchToNight)
        } else {
            settingsPopup.menu.findItem(R.id.nightModeButton).setTitle(R.string.switchToLight)
        }
        settingsPopup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.nightModeButton -> switchUIMode()
                R.id.logoutButton -> doLogout()
            }
            true
        }
        settingsPopup.show()
    }

    private fun switchUIMode() {
        if(nightMode == MODE_NIGHT_NO) {
            setDefaultNightMode(MODE_NIGHT_YES)
            editor = sharedPreferences.edit();
            editor.putInt("NightModeIntStatus", getDefaultNightMode());
            editor.apply();
        } else {
            setDefaultNightMode(MODE_NIGHT_NO)
            editor = sharedPreferences.edit();
            editor.putInt("NightModeIntStatus", getDefaultNightMode());
            editor.apply();
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