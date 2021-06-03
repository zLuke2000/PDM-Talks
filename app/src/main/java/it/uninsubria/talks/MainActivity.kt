package it.uninsubria.talks

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import com.google.firebase.storage.*
import com.google.firebase.storage.ktx.storage
import it.uninsubria.adapter.RVTAdapter
import it.uninsubria.firebase.firestore.Database
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_profilo.*
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException


class MainActivity : AppCompatActivity(), RVTAdapter.OnTalkClickListener {
    private val TAG = "Main_Activity"
    // Firebase AUTH
    private lateinit var myAuth: FirebaseAuth
    // Firebase Storage
    var storage: FirebaseStorage = Firebase.storage
    private val PICK_IMAGE_REQUEST = 1
    // lista e adapter necessari per inizializzare la RecyclerView
    private lateinit var talksArrayList: ArrayList<Talks>
    private lateinit var rvtAdapter: RVTAdapter
    // Variabili condivise (necessarie per mantenere la modalità notturna in caso di riavvio dell'app)
    private var nightMode: Int = getDefaultNightMode();
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    // Menu popup per impostazioni
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
                R.id.changeAccountPicture -> uploadPicture()
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

    private fun uploadPicture() {
        startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Detects request codes
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            val selectedImage: Uri? = data?.data
            // Ricerca nickaname associato all'utente corrente
            Database().db.collection("utenti")
                    .whereEqualTo("email", myAuth.currentUser.email)
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            for (document in task.result!!) {
                                try {
                                    val accountRef = storage.reference.child("AccountIcon/${document.data["nickname"]}.jpg")
                                    var bitmap: Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
                                    // Compressione
                                    val baos = ByteArrayOutputStream()
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                                    val data = baos.toByteArray()
                                    // Upload
                                    var uploadTask = accountRef.putBytes(data)
                                    uploadTask.addOnFailureListener {
                                        Toast.makeText(baseContext, R.string.ImageNotUpdated, Toast.LENGTH_SHORT).show()
                                    }.addOnSuccessListener {
                                        Toast.makeText(baseContext, R.string.ImageUpdated, Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: FileNotFoundException) {
                                    Log.e(TAG, e.printStackTrace().toString())
                                } catch (e: IOException) {
                                    Log.e(TAG, e.printStackTrace().toString())
                                }
                            }
                        } else {
                            Log.w(TAG, "[ERRORE] nella lettura degli utenti", task.exception)
                        }
                    }
        }
    }
}