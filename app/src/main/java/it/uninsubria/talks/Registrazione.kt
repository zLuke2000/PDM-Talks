package it.uninsubria.talks

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.uninsubria.database.Database
import kotlinx.android.synthetic.main.activity_registrazione.*
import java.util.regex.Pattern

class Registrazione : AppCompatActivity() {
    private val TAG = "Activity_Registrazione"
    private lateinit var myAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrazione)
        //Firebase - authentication
        myAuth = Firebase.auth
    }

    fun checkRegistrazione(view: View){
        val name: String = TF_RealName.text.toString().trim()
        val surname: String = TF_RealSurname.text.toString().trim()
        val email: String = editTextEmail.text.toString().trim()
        val password: String = TF_Password.text.toString().trim()
        val nickname: String = TF_Nickname.text.toString().trim()
        var ok: Boolean = true

        ok = checkName(TF_RealName, 2, 16)
        ok = checkName(TF_RealSurname, 3, 16)
        ok = checkEmail(email)
        ok = checkPassword(TF_Password, 4)
        ok = checkName(TF_Nickname, 4, 30)

        if(ok) {
            Log.i(TAG, "Controllo OK")
            Database().addUserToDB(name, surname, email, password, nickname)
            //AUTENTICAZIONE
            myAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "[REG] createUserWithEmail:success")
                        val user = myAuth.currentUser
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "[REG] createUserWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Log.e(TAG, "[REG] ERRORE")
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
        val EMAIL_PATTERN = ("^[_A-Za-z0-9-\\+]@[A-Za-z0-9-]+(\\.[A-Za-z]{2,})$")
        val pattern = Pattern.compile(EMAIL_PATTERN)
        val matcher = pattern.matcher(email)
        if(matcher.matches()) {
            return true
        } else {
            editTextEmail.error = getString(R.string.invalidEmail)
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