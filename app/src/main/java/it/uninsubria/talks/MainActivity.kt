package it.uninsubria.talks

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
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
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.ktx.Firebase
import it.uninsubria.adapter.RVTAdapter
import it.uninsubria.firebase.Database
import it.uninsubria.firebase.Storage
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException


class MainActivity : AppCompatActivity(), RVTAdapter.OnTalkClickListener {
    private val TAG = "Main_Activity"

    // Firebase
    private val myDB: Database = Database()
    private lateinit var myAuth: FirebaseAuth
    private val myStorage: Storage = Storage()
    private val PICK_IMAGE_REQUEST = 1

    // Lista e adapter necessari per inizializzare la RecyclerView
    private lateinit var talksArrayList: ArrayList<Talks>
    private lateinit var rvtAdapter: RVTAdapter

    // Variabili condivise (necessarie per mantenere la modalità notturna in caso di riavvio dell'app)
    private var nightMode: Int = getDefaultNightMode()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    // Menu popup per impostazioni
    private lateinit var settingsPopup: PopupMenu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase - authentication
        myAuth = Firebase.auth

        // Recycler View
        setContentView(R.layout.activity_main)
        UserTalksRecyclerView.layoutManager = LinearLayoutManager(this)
        UserTalksRecyclerView.setHasFixedSize(true)
        talksArrayList = arrayListOf()
        rvtAdapter = RVTAdapter(baseContext, talksArrayList, this, Resources.getSystem().displayMetrics.widthPixels, "")
        UserTalksRecyclerView.adapter = rvtAdapter

        // Dark-Light Mode
        sharedPreferences = getSharedPreferences("SharedPrefs", MODE_PRIVATE)
        nightMode = sharedPreferences.getInt("NightModeIntStatus", MODE_NIGHT_NO)
        setDefaultNightMode(nightMode)

        // PopUp menu
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

        // Controllo se l'utente e' gia' registrato
        val currentUser = myAuth.currentUser
        if(currentUser == null) {
            Log.i(TAG, "[MAIN] Passo alla schermata <Login>")
            startActivity(Intent(this, Login::class.java))
        } else {
            talksArrayList.clear()
            eventChangeListener()
        }

        // Imposto listener per swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            talksArrayList.clear()
            eventChangeListener()
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun eventChangeListener() {
        // Scarico Talks da firestore
        try {
            myDB.getTalks { value ->
                for (dc: DocumentChange in value?.documentChanges!!) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val currentTalk = dc.document.toObject(Talks::class.java)
                        currentTalk.imagePath = dc.document.id
                        talksArrayList.add(currentTalk)
                    }
                }
                rvtAdapter.notifyDataSetChanged()
            }
        } catch (e: IndexOutOfBoundsException) {
            Log.e(TAG, e.printStackTrace().toString())
        }

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
            editor = sharedPreferences.edit()
            editor.putInt("NightModeIntStatus", getDefaultNightMode())
            editor.apply()
        } else {
            setDefaultNightMode(MODE_NIGHT_NO)
            editor = sharedPreferences.edit()
            editor.putInt("NightModeIntStatus", getDefaultNightMode())
            editor.apply()
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

            myAuth.currentUser?.email?.let {
                myDB.getTaskForImage(it) { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            try {
                                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(
                                    this.contentResolver,
                                    selectedImage
                                )
                                // Compressione
                                val baos = ByteArrayOutputStream()
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                                val imgData = baos.toByteArray()
                                // Upload
                                myStorage.uploadBitmap(
                                    "AccountIcon/${document.data["nickname"]}.jpg",
                                    imgData
                                ) { result ->
                                    if (result) {
                                        Toast.makeText( baseContext, R.string.ImageNotUpdated, Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(baseContext, R.string.ImageUpdated, Toast.LENGTH_SHORT).show()
                                    }
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
}