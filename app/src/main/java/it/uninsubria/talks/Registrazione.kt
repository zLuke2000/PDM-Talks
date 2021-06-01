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
import kotlinx.android.synthetic.main.activity_profilo.*
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

    fun checkRegistrazione(view: View) {
        val name: String = TF_RealName.text.toString().trim()
        val surname: String = TF_RealSurname.text.toString().trim()
        val email: String = TF_EmailLogin.text.toString().trim()
        val password: String = TF_Password.text.toString().trim()
        val nickname: String = TF_Nickname.text.toString().trim()
        var duplicato = false

        if (checkName(TF_RealName, 2, 16) and checkName(TF_RealSurname, 3, 16) and checkEmail(email) and checkPassword(TF_Password, 6) and checkName(TF_Nickname, 4, 30)) {
            Log.i(TAG, "Controllo OK")
            //Controllo nickname unico
            Database().db.collection("utenti")
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            for (document in task.result!!) {
                                if(document.data["nickname"]?.equals(nickname)!!) {
                                    duplicato = true
                                }
                            }
                            if(!duplicato) {
                                Database().addUserToDB(name, surname, email, nickname)
                                myAuth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(this) { task ->
                                            if (task.isSuccessful) {
                                                // Sign in success, update UI with the signed-in user's information
                                                Log.d(TAG, "[REG] createUserWithEmail:success")
                                                Toast.makeText(baseContext, R.string.SignInOK, Toast.LENGTH_SHORT).show()
                                                startActivity(Intent(this, MainActivity::class.java))
                                            } else {
                                                // If sign in fails, display a message to the user.
                                                Log.w(TAG, "[REG] createUserWithEmail:failure", task.exception)
                                                Toast.makeText(baseContext, R.string.SignInKO, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                            } else {
                                Toast.makeText(baseContext, "" + getString(R.string.duplicateNickname).replace("$", nickname), Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Log.w(TAG, "[ERRORE] nella lettura degli utenti", task.exception)
                        }
                    }

        } else {
            Log.e(TAG, "ERRORE - Controllo non soddisfatto")
        }
    }

    // controllo lunghezza nome, cognome e nickname
    private fun checkName(tf: EditText, min: Int, max: Int): Boolean {
        if (tf.text.length < min) {
            tf.error = getString(R.string.minChar).replace("$", "" + min)
            return false
        } else if (tf.text.length > max) {
            tf.error = getString(R.string.maxChar).replace("$", "" + min)
            return false
        }
        return true
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
    private fun checkPassword(pass: EditText, min: Int): Boolean {
        if (pass.text.length < min) {
            pass.error = getString(R.string.minChar).replace("$", "" + min)
            return false
        } else {
            return true
        }
    }
}