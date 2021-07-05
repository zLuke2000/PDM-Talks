package it.uninsubria.talks

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import it.uninsubria.firebase.Database
import it.uninsubria.models.Profile
import java.util.*

class Registration : AppCompatActivity() {
    // Current activity TAG
    private val TAG = "Activity_Registrazione"

    // Firebase
    private lateinit var myAuth: FirebaseAuth
    private val myDB: Database = Database()

    // raw view declaration
    private lateinit var tfRealName: TextInputEditText
    private lateinit var tfRealSurname: TextInputEditText
    private lateinit var tfEmail: TextInputEditText
    private lateinit var tfpassword: TextInputEditText
    private lateinit var tfNickname: TextInputEditText
    private lateinit var tilRealName: TextInputLayout
    private lateinit var tilRealSurname: TextInputLayout
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilNickname: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Carico il layout
        setContentView(R.layout.activity_registration)
        // raw view link
        tfRealName = findViewById(R.id.TF_RealNameReg)
        tfRealSurname = findViewById(R.id.TF_RealSurnameReg)
        tfEmail = findViewById(R.id.TF_EmailReg)
        tfpassword = findViewById(R.id.TF_PasswordReg)
        tfNickname = findViewById(R.id.TF_NicknameReg)
        tilRealName = findViewById(R.id.TIL_RealNameReg)
        tilRealSurname = findViewById(R.id.TIL_RealSurnameReg)
        tilEmail = findViewById(R.id.TIL_EmailReg)
        tilPassword = findViewById(R.id.TIL_PasswordReg)
        tilNickname = findViewById(R.id.TIL_NicknameReg)

        //Firebase - authentication
        myAuth = FirebaseAuth.getInstance()
    }

    fun checkValueReg(@Suppress("UNUSED_PARAMETER") v: View) {
        val name: String = tfRealName.text.toString().trim()
        val surname: String = tfRealSurname.text.toString().trim()
        val email: String = tfEmail.text.toString().trim().toLowerCase(Locale.getDefault())
        val password: String = tfpassword.text.toString().trim()
        val nickname: String = tfNickname.text.toString().trim()

        if (checkSimple(tilRealName, name, 2, 16) && checkSimple(tilRealSurname, surname, 3, 16) && checkEmail(tilEmail, email) && checkPassword(tilPassword, password, 6) && checkSimple(tilNickname, nickname, 4, 30)) {
            Log.i(TAG, "Controllo OK")

            //Controllo nickname unico
            myDB.checkUniqueUser(nickname) { result ->
                if (result) {
                    myAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { resultTask ->
                            if (resultTask.isSuccessful) {
                                // Sign in success
                                Log.d(TAG, "createUserWithEmail:success")
                                myDB.addUserToDB(Profile(nickname, name, surname, hasPicture = false, email))
                                Toast.makeText(baseContext, R.string.SignInOK, Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                            } else {
                                // If sign in fails
                                Log.w(TAG,"createUserWithEmail:failure", resultTask.exception)
                                Toast.makeText(baseContext, R.string.SignInKO, Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(baseContext,"" + getString(R.string.duplicateNickname).replace("$", nickname), Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Log.e(TAG, "ERRORE - Controllo non soddisfatto")
        }
    }

    // controllo lunghezza nome, cognome e nickname
    private fun checkSimple(til: TextInputLayout, tfText: String, min: Int, max: Int): Boolean {
        return when {
            tfText.length < min -> {
                til.error = getString(R.string.minChar).replace("$", "" + min)
                false
            }
            tfText.length > max -> {
                til.error = getString(R.string.maxChar).replace("$", "" + min)
                false
            }
            else -> {
                til.error = null
                true
            }
        }
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
            til.isHelperTextEnabled = false
            til.error = getString(R.string.minChar).replace("$", "" + min)
            false
        } else {
            til.error = null
            til.isHelperTextEnabled = true
            true
        }
    }
}