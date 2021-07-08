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
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.ktx.Firebase
import it.uninsubria.adapter.MyLinearLayoutManager
import it.uninsubria.adapter.RVTAdapter
import it.uninsubria.firebase.Database
import it.uninsubria.firebase.Storage
import it.uninsubria.models.Talks
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException

/*
    UTRV -> User Talks Recycler View
 */

class MainActivity : AppCompatActivity(), RVTAdapter.OnTalkClickListener {
    // Current activity TAG
    private val TAG = "Main_Activity"

    // Firebase
    private val myDB: Database = Database()
    private lateinit var myAuth: FirebaseAuth
    private val myStorage: Storage = Storage()
    private val PICK_IMAGE_REQUEST = 1

    // Lista e adapter necessari per inizializzare la RecyclerView
    private lateinit var talksArrayList: ArrayList<Talks>
    private var oldTALSize: Int = 0
    private lateinit var rvtAdapter: RVTAdapter

    // Variabili condivise (necessarie per mantenere la modalità notturna in caso di riavvio dell'app)
    private var nightMode: Int = getDefaultNightMode()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    // Menu popup per impostazioni
    private lateinit var settingsPopup: PopupMenu

    // raw view declaration
    private lateinit var myUTRV: RecyclerView
    private lateinit var settingBtn: ImageButton
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var tilCercaProfilo: TextInputLayout
    private lateinit var tfCercaProfilo: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Firebase - authentication
        myAuth = Firebase.auth
        // Carico il layout
        setContentView(R.layout.activity_main)
        // raw view link
        myUTRV = findViewById(R.id.UserTalksRecyclerView)
        settingBtn = findViewById(R.id.IB_settingsButton)
        refreshLayout = findViewById(R.id.swipeRefreshLayout)
        tilCercaProfilo = findViewById(R.id.TIL_cercaProfilo)
        tfCercaProfilo = findViewById(R.id.TF_cercaProfilo)

        myUTRV.layoutManager = MyLinearLayoutManager(this)
        myUTRV.setHasFixedSize(true)

        volatile@ talksArrayList = arrayListOf()
        rvtAdapter = RVTAdapter(baseContext, talksArrayList, this, Resources.getSystem().displayMetrics.widthPixels, "")
        myUTRV.adapter = rvtAdapter

        // Dark-Light Mode
        sharedPreferences = getSharedPreferences("SharedPrefs", MODE_PRIVATE)
        nightMode = sharedPreferences.getInt("NightModeIntStatus", MODE_NIGHT_NO)
        setDefaultNightMode(nightMode)

        // Pop-up menu
        settingsPopup = PopupMenu(this, settingBtn)
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
            Log.i(TAG, "Switch to activity <Login>")
            startActivity(Intent(this, Login::class.java))
        } else {
            eventChangeListener()
        }

        // Imposto listener per swipeRefreshLayout
        refreshLayout.setOnRefreshListener {
            eventChangeListener()
        }
    }

    private fun eventChangeListener() {
        // Scarico Talks da firestore
        talksArrayList.clear()
        try {
            myDB.getTalksList { value ->
                for (dc: DocumentChange in value?.documentChanges!!) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val currentTalk = dc.document.toObject(Talks::class.java)
                        currentTalk.imagePath = dc.document.id
                        talksArrayList.add(currentTalk)
                    }
                }
                refreshLayout.isRefreshing = false
                rvtAdapter.notifyDataSetChanged()
            }
        } catch (e: IndexOutOfBoundsException) {
            Log.e(TAG, e.printStackTrace().toString())
        }
    }

    override fun talkClick(position: Int) {
        Log.i(TAG, "Switch to activity <UserProfile>")
        val intent = Intent(this, UserProfile::class.java)
        intent.putExtra("NICKNAME", talksArrayList[position].nickname)
        startActivity(intent)
    }

    fun openSettings(@Suppress("UNUSED_PARAMETER") v: View) {
        if(nightMode == MODE_NIGHT_NO) {
            settingsPopup.menu.findItem(R.id.nightModeButton).setTitle(R.string.switchToNight)
        } else {
            settingsPopup.menu.findItem(R.id.nightModeButton).setTitle(R.string.switchToLight)
        }
        settingsPopup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.nightModeButton -> switchUIMode()
                R.id.changeAccountPicture -> uploadPicture()
                R.id.removeAccountPicture -> removeAccountPicture()
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
        Log.i(TAG, "Switch to activity <Login>")
        startActivity(Intent(this, Login::class.java))
    }

    fun findProfile(@Suppress("UNUSED_PARAMETER") v: View) {
        Log.i(TAG, "Switch to activity <ProfileList>")
        val intent = Intent(this, ProfileList::class.java)

        myDB.getSimilarProfile(tfCercaProfilo.text.toString().trim()) { profileArrayList ->
            if(profileArrayList != null && profileArrayList.size > 0) {
                tilCercaProfilo.error = null
                intent.putExtra("PROFILE_LIST", profileArrayList)
                startActivity(intent)
            } else {
                tilCercaProfilo.error = (tfCercaProfilo.text.toString().trim() + " " + getString(R.string.notFound))
            }
        }
    }

    fun newPost(@Suppress("UNUSED_PARAMETER") v: View) {
        Log.i(TAG, "Switch to activity <NewTalk>")
        startActivity(Intent(this, NewTalk::class.java))
    }

    // Richiesta immagine dalla galleria
    private fun uploadPicture() {
        Log.i(TAG, "Get picture from gallery")
        startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI), PICK_IMAGE_REQUEST)
    }

    // Ricezione e gestione dell'immagine dalla galleria
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
                                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
                                // Compressione
                                val baos = ByteArrayOutputStream()
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                                val imgData = baos.toByteArray()
                                // Upload
                                myStorage.uploadBitmap("AccountIcon/${document.data["nickname"]}.jpg", imgData) { storageResult ->
                                    if (storageResult) {
                                        myDB.setUserPictureStatus(myAuth.currentUser!!.email, true) { result ->
                                            if(result) {
                                                Toast.makeText(baseContext, R.string.ImageUpdated, Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(baseContext, R.string.ImageNotUpdated, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(baseContext, R.string.ImageNotUpdated, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: FileNotFoundException) {
                                Log.e(TAG, e.printStackTrace().toString())
                            } catch (e: IOException) {
                                Log.e(TAG, e.printStackTrace().toString())
                            }
                        }
                    } else {
                        Log.e(TAG, task.exception.toString())
                    }
                }
            }
        }
    }

    private fun removeAccountPicture() {
        myDB.getNicknameByEmail(myAuth.currentUser!!.email) { result ->
            if(result != "") {
                myStorage.deleteBitmap("AccountIcon/$result.jpg")
            }
        }
        myDB.setUserPictureStatus(myAuth.currentUser!!.email, false) { result ->
            if(result) {
                Toast.makeText(baseContext, R.string.ImageRemoved, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(baseContext, R.string.ImageNotRemoved, Toast.LENGTH_SHORT).show()
            }
        }
    }
}