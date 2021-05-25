package it.uninsubria.talks

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.uninsubria.firebase.authentication.Authentication
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_registrazione.TF_EmailLogin

class Login : AppCompatActivity() {
    private val TAG = "Activity_Login"
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = Firebase.auth
    }

    fun loginUtente(view: View) {
        val password: String = TF_PasswordLogin.text.toString().trim()
        val email: String = TF_EmailLogin.text.toString().trim()
        Log.i(TAG, "email: $email")
        Log.i(TAG, "psw: $password")
        if(Authentication().autenticazioneUtenteNomePassword(this, auth, email, password)) {
            Log.i(TAG, "ATTENZIONE IF")
            finish()
        } else {
            Log.i(TAG, "ATTENZIONE ELSE")
        }

    }

    fun registraNuovoCliente(view: View) {
        Log.i(TAG, "[LOGIN] Passo alla schermata <Registrazione>")
        startActivity(Intent(this, Registrazione::class.java))
    }
}