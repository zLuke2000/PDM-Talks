package it.uninsubria.talks

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import it.uninsubria.firebase.Database
import kotlinx.android.synthetic.main.activity_registrazione.*

class Registrazione : AppCompatActivity() {
    private val TAG = "Activity_Registrazione"

    private val myDB: Database = Database()
    private lateinit var myAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrazione)
        //Firebase - authentication
        myAuth = FirebaseAuth.getInstance()
    }

    fun checkValueReg(v: View) {
        val name: String = TF_RealNameReg.text.toString().trim()
        val surname: String = TF_RealSurnameReg.text.toString().trim()
        val email: String = TF_EmailReg.text.toString().trim()
        val password: String = TF_PasswordReg.text.toString().trim()
        val nickname: String = TF_NicknameReg.text.toString().trim()
        var duplicato = false

        if (checkName(TIL_RealNameReg, name, 2, 16) and checkName(TIL_RealSurnameReg, surname, 3, 16) and checkEmail(TIL_EmailReg, email) and checkPassword(TIL_PasswordReg, password, 6) and checkName(TIL_NicknameReg, nickname, 4, 30)) {
            Log.i(TAG, "Controllo OK")
            //Controllo nickname unico
            myDB.checkUniqueUser(nickname) { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        if(document.data["nickname"]?.equals(nickname)!!) {
                            duplicato = true
                        }
                    }
                    if(!duplicato) {
                        myAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(this) { resultTask ->
                                    if (task.isSuccessful) {
                                        // Sign in success
                                        Log.d(TAG, "[REG] createUserWithEmail:success")
                                        myDB.addUserToDB(name, surname, email, nickname)
                                        Toast.makeText(baseContext, R.string.SignInOK, Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this, MainActivity::class.java))
                                    } else {
                                        // If sign in fails
                                        Log.w(TAG, "[REG] createUserWithEmail:failure", resultTask.exception)
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
    private fun checkName(til: TextInputLayout, tfText: String, min: Int, max: Int): Boolean {
        if (tfText.length < min) {
            til.error = getString(R.string.minChar).replace("$", "" + min)
            return false
        } else if (tfText.length > max) {
            til.error = getString(R.string.maxChar).replace("$", "" + min)
            return false
        }
        til.error = null
        return true
    }

    // controllo email
    private fun checkEmail(til: TextInputLayout, email: String): Boolean {
        return if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            til.error = null
            true
        } else {
            til.error = getString(R.string.invalidEmail)
            false
        }
    }

    // controllo password
    private fun checkPassword(til: TextInputLayout, passText: String, min: Int): Boolean {
        return if (passText.length < min) {
            til.error = getString(R.string.minChar).replace("$", "" + min)
            false
        } else {
            til.error = null
            true
        }
    }
}