package it.uninsubria.talks

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.ktx.Firebase
import it.uninsubria.adapter.RVTAdapter
import it.uninsubria.firebase.Database
import it.uninsubria.firebase.Storage
import it.uninsubria.models.Profile
import it.uninsubria.models.Talks

/*
    SUTRV -> Single User Talks Recycler View
 */
class UserProfile : AppCompatActivity() {
    // Current activity TAG
    private val TAG = "Activity_UserProfile"

    // Firebase
    private val myDB: Database = Database()
    private val myAuth: FirebaseAuth = Firebase.auth
    private val myStorage: Storage = Storage()

    private lateinit var talksArrayList: ArrayList<Talks>
    private lateinit var rvtAdapter: RVTAdapter
    private lateinit var nickname: String
    private var hasPicture: Boolean = false

    // raw view declaration
    private lateinit var tvNicknameProfilo: TextView
    private lateinit var mySUTRV: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var tvNomeProfilo: TextView
    private lateinit var tvCognomeProfilo: TextView
    private lateinit var ivProfileIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nickname = intent.getStringExtra("NICKNAME").toString()

        // Carica il layout
        setContentView(R.layout.activity_profile)

        // raw view link
        tvNicknameProfilo = findViewById(R.id.TV_NicknameProfilo)
        mySUTRV = findViewById(R.id.SingleUserTalksRecyclerView)
        refreshLayout = findViewById(R.id.swipeRefreshLayoutProfilo)
        tvNomeProfilo = findViewById(R.id.TV_NomeProfilo)
        tvCognomeProfilo = findViewById(R.id.TV_CognomeProfilo)
        ivProfileIcon = findViewById(R.id.IV_ProfileIcon)

        // Imposta il nickanme
        tvNicknameProfilo.text = nickname
        // Compila gli altri campi (Nome, Cognome, Immagine profilo)
        getUserData()

        mySUTRV.layoutManager = LinearLayoutManager(this)
        mySUTRV.setHasFixedSize(true)
        talksArrayList = arrayListOf()
        rvtAdapter = RVTAdapter(baseContext, talksArrayList, null, Resources.getSystem().displayMetrics.widthPixels, myAuth.currentUser?.email)
        mySUTRV.adapter = rvtAdapter
    }

    public override fun onStart() {
        super.onStart()
        talksArrayList.clear()
        eventChangeListener()

        refreshLayout.setOnRefreshListener {
            talksArrayList.clear()
            eventChangeListener()
            refreshLayout.isRefreshing = false
        }
    }

    private fun getUserData() {
        // Imposta Nome e Cognome
        myDB.getUser(nickname) { task ->
            if (task.isSuccessful) {
                for (document in task.result!!) {
                    tvNomeProfilo.text = document.data["name"].toString()
                    tvCognomeProfilo.text = document.data["surname"].toString()
                    // Imposta immagine profilo
                    if(document.data["hasPicture"] as Boolean) {
                        myStorage.downloadBitmap("AccountIcon/$nickname.jpg") { resultBitmap ->
                            if (resultBitmap != null) {
                                ivProfileIcon.setImageBitmap(resultBitmap)
                            } else {
                                ivProfileIcon.setImageResource(R.drawable.default_account_image)
                            }
                        }
                    }else {
                        ivProfileIcon.setImageResource(R.drawable.default_account_image)
                    }
                }
            } else {
                Log.w(TAG, "[ERRORE] nella lettura degli utenti", task.exception)
            }
        }

    }

    private fun eventChangeListener() {
        //DOWNLOAD DATA FROM FIRESTORE DB
        myDB.getSingleUserTalks(nickname) { value ->
            for (dc: DocumentChange in value?.documentChanges!!) {
                if (dc.type == DocumentChange.Type.ADDED) {
                    val currentTalk = dc.document.toObject(Talks::class.java)
                    currentTalk.imagePath = dc.document.id
                    talksArrayList.add(currentTalk)
                }
            }
            rvtAdapter.notifyDataSetChanged()
        }
    }
}