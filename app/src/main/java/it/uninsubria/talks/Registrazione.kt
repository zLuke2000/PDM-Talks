package it.uninsubria.talks

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import it.uninsubria.firebase.firestore.Database
import it.uninsubria.firebase.authentication.Authentication
import kotlinx.android.synthetic.main.activity_registrazione.*

class Registrazione : AppCompatActivity() {
    private val TAG = "Activity_Registrazione"
    private lateinit var myAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrazione)
        //Firebase - authentication
        myAuth = FirebaseAuth.getInstance()
    }

    fun checkRegistrazione(view: View){
        val name: String = TF_RealName.text.toString().trim()
        val surname: String = TF_RealSurname.text.toString().trim()
        val email: String = TF_EmailLogin.text.toString().trim()
        val password: String = TF_Password.text.toString().trim()
        val nickname: String = TF_Nickname.text.toString().trim()

        if(checkName(TF_RealName, 2, 16) and checkName(TF_RealSurname, 3, 16) and checkEmail(email) and checkPassword(TF_Password, 6) and checkName(TF_Nickname, 4, 30)) {
            Log.i(TAG, "Controllo OK")
            Database().addUserToDB(name, surname, email, nickname)
            if(Authentication().registraUtenteNomePassword(this, myAuth, email, password)) {
                Toast.makeText(baseContext, "Autenticazione Fallita", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(baseContext, "Utente registrato con successo", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, Login::class.java))
            }
        } else {
            Log.e(TAG, "[REG] ERRORE - Controllo non soddisfatto")
        }
    }

    // controllo lunghezza nome, cognome e nickname
    private fun checkName(tf: EditText, min: Int, max: Int): Boolean {
        if(tf.text.length < min) {
            tf.error = "(min: $min characters)"
            return false
        } else if(tf.text.length > max) {
            tf.error = "(max: $max characters)"
            return false
        }
        return true
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
    private fun checkPassword(pass: EditText, min: Int): Boolean {
       if(pass.text.length < min) {
           pass.error = "(min: $min characters)"
           return false
       } else {
           return true
       }
    }
}