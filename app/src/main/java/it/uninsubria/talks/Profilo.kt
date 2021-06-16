package it.uninsubria.talks

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.*
import it.uninsubria.adapter.RVTAdapter
import it.uninsubria.firebase.Storage
import it.uninsubria.firebase.Database
import kotlinx.android.synthetic.main.activity_profilo.*

class Profilo : AppCompatActivity() {
    private val TAG = "Activity_Profilo"
    private val myDB: Database = Database()
    private val myStorage: Storage = Storage()
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
        rvtAdapter = RVTAdapter(talksArrayList, null, Resources.getSystem().displayMetrics.widthPixels)
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

        myDB.getUser(nickname) { task ->
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
        myStorage.downloadBitmap("AccountIcon/$nickname.jpg") { success, resultBitmap ->
            if (success) {
                immagine_Profilo.setImageBitmap(resultBitmap)
            } else {
                immagine_Profilo.setImageResource(R.drawable.default_account_image)
            }
        }
    }

    private fun eventChangeListener() {
        //DOWNLOAD DATA FROM FIRESTORE DB
        myDB.getSingleUserTalks(nickname) { value ->
            for (dc: DocumentChange in value?.documentChanges!!) {
                if (dc.type == DocumentChange.Type.ADDED) {
                    var currentTalk = dc.document.toObject(Talks::class.java)
                    currentTalk.imagePath = dc.document.id
                    talksArrayList.add(currentTalk)
                }
            }
            rvtAdapter.notifyDataSetChanged()
        }
    }
}