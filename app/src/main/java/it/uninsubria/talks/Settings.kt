package it.uninsubria.talks

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Settings : AppCompatActivity() {
    private val TAG = "Activity_Settings"
    private lateinit var myAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        myAuth = Firebase.auth
    }

    fun logout(v: View) {
        myAuth.signOut()
        Toast.makeText(baseContext, R.string.logOut, Toast.LENGTH_SHORT).show()
    }
}