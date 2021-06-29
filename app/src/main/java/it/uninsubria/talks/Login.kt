package it.uninsubria.talks

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout
import it.uninsubria.firebase.Authentication

class Login : AppCompatActivity() {
    // Current activity TAG
    private val TAG = "Activity_Login"
    private var myAuth: Authentication = Authentication()

    // raw view declaration
    private lateinit var tfPassword: TextView
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tfEmail: TextView
    private lateinit var tilEmail: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // raw view link
        tfPassword = findViewById(R.id.TF_PasswordLogin)
        tilPassword = findViewById(R.id.TIL_PasswordLogin)
        tfEmail = findViewById(R.id.TF_EmailLogin)
        tilEmail = findViewById(R.id.TIL_EmailLogin)
    }

    fun loginUtente(@Suppress("UNUSED_PARAMETER") v: View) {
        val password: String = tfPassword.text.toString().trim()
        val email: String = tfEmail.text.toString().trim()

        if (checkEmail(email) and checkPassword(password, 6)) {
            myAuth.signInWithEmailAndPassword(this, email, password) { result ->
                if(result) {
                    Toast.makeText(baseContext, R.string.loginOK, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    Toast.makeText(baseContext, R.string.loginKO, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // controllo email
    private fun checkEmail(email: String): Boolean {
        return if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = null
            true
        } else {
            tilEmail.error = getString(R.string.invalidEmail)
            false
        }
    }

    // controllo password
    private fun checkPassword(pass: String, min: Int): Boolean {
        Log.i(TAG, pass);
        return if (pass.length < min) {
            tilPassword.error = getString(R.string.minChar).replace("$", "" + min)
            false
        } else {
            tilPassword.error = null
            true
        }
    }

    fun registraNuovoCliente(@Suppress("UNUSED_PARAMETER") v: View) {
        Log.i(TAG, "[LOGIN] Passo alla schermata <Registrazione>")
        startActivity(Intent(this, Registrazione::class.java))
    }
}