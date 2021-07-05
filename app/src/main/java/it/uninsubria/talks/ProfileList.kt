package it.uninsubria.talks

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.uninsubria.adapter.RVUAdapter
import it.uninsubria.firebase.Database
import it.uninsubria.models.Profile

/*
    lurv -> List User Recycler View
 */

class ProfileList : AppCompatActivity(), RVUAdapter.OnTalkClickListener {
    // Current activity TAG
    private val TAG = "Activity_ListaProfili"

    // Firebase
    private val myDB: Database = Database()

    // Lista e adapter necessari per inizializzare la RecyclerView
    private lateinit var usersArrayList: ArrayList<Profile>
    private lateinit var rvuAdapter: RVUAdapter

    // raw view declaration
    private lateinit var myLURV: RecyclerView
    private lateinit var activityTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        usersArrayList = intent.getParcelableArrayListExtra("PROFILE_LIST")!!
        // Carico il layout
        setContentView(R.layout.activity_profile_list)
        // raw view link
        myLURV = findViewById(R.id.ListUserRecyclerView)
        activityTitle = findViewById(R.id.TV_titleProfileList)

        myLURV.layoutManager = LinearLayoutManager(this)
        myLURV.setHasFixedSize(true)

        rvuAdapter = RVUAdapter(usersArrayList, this)
        myLURV.adapter = rvuAdapter
    }

    override fun talkClick(position: Int) {
        val intent = Intent(this, UserProfile::class.java)
        intent.putExtra("NICKNAME", usersArrayList[position].nickname)
        startActivity(intent)
    }
}