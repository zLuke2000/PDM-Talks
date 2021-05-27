package it.uninsubria.talks

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_registrazione.TF_EmailLogin

class Login : AppCompatActivity() {
    private val TAG = "Activity_Login"
    private lateinit var myAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        myAuth = Firebase.auth
    }

    fun loginUtente(view: View) {
        val password: String = TF_PasswordLogin.text.toString().trim()
        val email: String = TF_EmailLogin.text.toString().trim()

        if(checkEmail(email) and checkPassword(password, 6)) {
            myAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success")
                            Toast.makeText(baseContext, R.string.loginOK, Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                            Toast.makeText(baseContext, R.string.loginKO, Toast.LENGTH_SHORT).show()
                        }
                    }
        }
    }

    // controllo email
    private fun checkEmail(email: String): Boolean {
        if(android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return true
        } else {
            TF_EmailLogin.error = getString(R.string.invalidEmail)
            return false
        }
    }

    // controllo password
    private fun checkPassword(pass: String, min: Int): Boolean {
        if(pass.length < min) {
            TF_PasswordLogin.error = getString(R.string.minChar).replace("$", "" + min)
            return false
        } else {
            return true
        }
    }

    fun registraNuovoCliente(view: View) {
        Log.i(TAG, "[LOGIN] Passo alla schermata <Registrazione>")
        startActivity(Intent(this, Registrazione::class.java))
    }
}