package it.uninsubria.talks

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.uninsubria.adapter.RVUAdapter
import it.uninsubria.firebase.Database
import it.uninsubria.models.User

/*
    lurv -> List User Recycler View
 */

class ListaProfili : AppCompatActivity(), RVUAdapter.OnTalkClickListener {
    private val TAG = "Activity_ListaProfili"

    private val myDB: Database = Database()

    private lateinit var userToFind: String
    // Lista e adapter necessari per inizializzare la RecyclerView
    private lateinit var usersArrayList: ArrayList<User>
    private lateinit var rvuAdapter: RVUAdapter

    // raw view declaration
    private lateinit var myLURV: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userToFind = intent.getStringExtra("NICKNAME").toString()
        // Carico il layout
        setContentView(R.layout.activity_lista_profili)
        // raw view link
        myLURV = findViewById(R.id.ListUserRecyclerView)

        myDB.getSimilarProfile(userToFind) { result ->
            if(result != null) {
                myLURV.layoutManager = LinearLayoutManager(this)
                myLURV.setHasFixedSize(true)
                usersArrayList = result
                rvuAdapter = RVUAdapter(usersArrayList, this)
                myLURV.adapter = rvuAdapter
            }
        }
    }

    override fun talkClick(position: Int) {
        val intent = Intent(this, Profilo::class.java)
        intent.putExtra("NICKNAME", usersArrayList[position].nickname)
        startActivity(intent)
    }
}