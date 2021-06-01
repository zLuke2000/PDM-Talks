package it.uninsubria.talks

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.*
import it.uninsubria.adapter.RVTAdapter
import it.uninsubria.firebase.firestore.Database
import kotlinx.android.synthetic.main.activity_crea_talk.*
import kotlinx.android.synthetic.main.activity_profilo.*
import kotlinx.coroutines.android.awaitFrame

class Profilo : AppCompatActivity() {
    private val TAG = "Activity_Profilo"
    private lateinit var talksArrayList: ArrayList<Talks>
    private lateinit var rvtAdapter: RVTAdapter
    private lateinit var nickname: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nickname = intent.getStringExtra("NICKNAME").toString()

        setContentView(R.layout.activity_profilo)
        TV_NicknameProfilo.text = nickname
        getUserData()

        SingleUserTalksRecyclerView.layoutManager = LinearLayoutManager(this)
        SingleUserTalksRecyclerView.setHasFixedSize(true)
        talksArrayList = arrayListOf()
        rvtAdapter = RVTAdapter(talksArrayList, null)
        SingleUserTalksRecyclerView.adapter = rvtAdapter
    }

    public override fun onStart() {
        super.onStart()
        talksArrayList.clear()
        eventChangeListener()

        swipeRefreshLayout_Profilo.setOnRefreshListener {
            talksArrayList.clear()
            eventChangeListener()
            swipeRefreshLayout_Profilo.isRefreshing = false
        }
    }

    private fun getUserData() {
        Database().db.collection("utenti")
                .whereEqualTo("nickname", nickname)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            TV_NomeProfilo.text = document.data["nome"].toString()
                            TV_CognomeProfilo.text = document.data["cognome"].toString()
                        }
                    } else {
                        Log.w(TAG, "[ERRORE] nella lettura degli utenti", task.exception)
                    }
                }
    }

    private fun eventChangeListener() {
        //DOWNLOAD DATA FROM FIRESTORE DB
        Database().db.collection("talks")
                .whereEqualTo("nickname", nickname)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(object : EventListener<QuerySnapshot> {
                    override fun onEvent(value: QuerySnapshot?,
                                         error: FirebaseFirestoreException?) {
                        if (error != null) {
                            Log.e(TAG, "Firestore error: " + error.message.toString())
                            return
                        }
                        for (dc: DocumentChange in value?.documentChanges!!) {
                            if (dc.type == DocumentChange.Type.ADDED) {
                                talksArrayList.add(dc.document.toObject(Talks::class.java))
                            }
                        }
                        rvtAdapter.notifyDataSetChanged()
                    }
                })
    }
}