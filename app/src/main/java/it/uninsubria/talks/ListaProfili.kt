package it.uninsubria.talks

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import it.uninsubria.adapter.RVUAdapter
import it.uninsubria.firebase.Database
import it.uninsubria.models.User
import kotlinx.android.synthetic.main.activity_lista_profili.*

class ListaProfili : AppCompatActivity(), RVUAdapter.OnTalkClickListener {
    private val TAG = "Activity_ListaProfili"

    private val myDB: Database = Database()

    private lateinit var userToFind: String
    // Lista e adapter necessari per inizializzare la RecyclerView
    private lateinit var usersArrayList: ArrayList<User>
    private lateinit var rvuAdapter: RVUAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userToFind = intent.getStringExtra("NICKNAME").toString()
        setContentView(R.layout.activity_lista_profili)

        myDB.getSimilarProfile(userToFind) { result ->
            if(result != null) {
                ListUserRecyclerView.layoutManager = LinearLayoutManager(this)
                ListUserRecyclerView.setHasFixedSize(true)
                usersArrayList = result
                for(user: User in usersArrayList) {
                    Log.i(TAG, "----------------> ${user.nickname}")
                }
                rvuAdapter = RVUAdapter(usersArrayList, this)
                ListUserRecyclerView.adapter = rvuAdapter
            }
        }
    }

    override fun onTalkclick(position: Int) {
        val intent = Intent(this, Profilo::class.java)
        intent.putExtra("NICKNAME", usersArrayList[position].nickname)
        startActivity(intent)
    }
}