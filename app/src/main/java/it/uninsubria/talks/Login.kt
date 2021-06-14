package it.uninsubria.talks

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import it.uninsubria.firebase.firestore.Authentication
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_registrazione.TF_EmailLogin

class Login : AppCompatActivity() {
    private val TAG = "Activity_Login"
    private var myAuth: Authentication = Authentication()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun loginUtente(view: View) {
        val password: String = TF_PasswordLogin.text.toString().trim()
        val email: String = TF_EmailLogin.text.toString().trim()

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
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return true
        } else {
            TF_EmailLogin.error = getString(R.string.invalidEmail)
            return false
        }
    }

    // controllo password
    private fun checkPassword(pass: String, min: Int): Boolean {
        return if (pass.length < min) {
            TF_PasswordLogin.error = getString(R.string.minChar).replace("$", "" + min)
            false
        } else {
            true
        }
    }

    fun registraNuovoCliente(view: View) {
        Log.i(TAG, "[LOGIN] Passo alla schermata <Registrazione>")
        startActivity(Intent(this, Registrazione::class.java))
    }
}