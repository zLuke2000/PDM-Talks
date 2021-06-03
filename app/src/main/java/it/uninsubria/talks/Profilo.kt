package it.uninsubria.talks

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.*
import it.uninsubria.adapter.RVTAdapter
import it.uninsubria.firebase.firestore.Database
import kotlinx.android.synthetic.main.activity_crea_talk.*
import kotlinx.android.synthetic.main.activity_profilo.*
import java.io.File


class Profilo : AppCompatActivity() {
    private val TAG = "Activity_Profilo"
    private lateinit var talksArrayList: ArrayList<Talks>
    private lateinit var rvtAdapter: RVTAdapter
    private lateinit var nickname: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nickname = intent.getStringExtra("NICKNAME").toString()

        // Carica il layout
        setContentView(R.layout.activity_profilo)
        // Imposta il nickanme
        TV_NicknameProfilo.text = nickname
        // Compila gli altri campi (Nome, Cognome, Immagine profilo)
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
        // Imposta Nome e Cognome
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
        // Imposta immagine profilo
        val path = MainActivity().storage.reference.child("AccountIcon/$nickname.jpg")
        val localFile = File.createTempFile("tempImg", "jpg")
        path.getFile(localFile).addOnSuccessListener {
            val filePath = localFile.path
            val bitmap = BitmapFactory.decodeFile(filePath)
            immagine_Profilo.setImageBitmap(bitmap)
        }.addOnFailureListener {
            // Handle any errors
        }
    }

    private fun eventChangeListener() {
        //DOWNLOAD DATA FROM FIRESTORE DB
        Database().db.collection("talks")
                .whereEqualTo("nickname", nickname)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(object : EventListener<QuerySnapshot> {
                    override fun onEvent(
                        value: QuerySnapshot?,
                        error: FirebaseFirestoreException?
                    ) {
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