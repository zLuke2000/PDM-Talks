package it.uninsubria.talks

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import it.uninsubria.database.Database
import kotlinx.android.synthetic.main.activity_registrazione.*
import java.util.regex.Pattern

class Registrazione : AppCompatActivity() {
    private val TAG = "Activity_Registrazione"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrazione)
    }

    fun checkRegistration(v: View?){
        val name: String = TF_RealName.text.toString()
        val surname: String = TF_RealSurname.text.toString()
        val email: String = TF_EmailAddress.text.toString()
        val password: String = TF_Password.text.toString()
        val nickname: String = TF_Nickname.text.toString()
        var ok: Boolean = true

        ok = checkName(TF_RealName, 2, 16)
        ok = checkName(TF_RealSurname, 3, 16)
        ok = checkEmail(email)
        ok = checkPassword(TF_Password, 4)
        ok = checkName(TF_Nickname, 4, 30)

        if(ok) {
            Log.i(TAG, "Controllo OK")
            Database(name, surname, email, password, nickname).check()
        } else {
            Log.e(TAG, "ERRORE")
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
        val EMAIL_PATTERN = ("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)@[A-Za-z0-9-]+(\\.[A-Za-z]{2,})$")
        val pattern = Pattern.compile(EMAIL_PATTERN)
        val matcher = pattern.matcher(email)
        if(matcher.matches()) {
            return true
        } else {
            TF_EmailAddress.error = getString(R.string.invalidEmail)
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