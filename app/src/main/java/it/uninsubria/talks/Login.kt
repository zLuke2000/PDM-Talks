package it.uninsubria.talks

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_registrazione.*

class Login : AppCompatActivity() {
    private val TAG = "Activity_Login"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun loginUtente(view: View) {
        val password: String = TF_Password.text.toString().trim()
        val nickname: String = TF_Nickname.text.toString().trim()
        Log.i(TAG, "psw: $password")
        Log.i(TAG, "nick: $nickname")

    }

    fun registraNuovoCliente(view: View) {
        Log.i(TAG, "[LOGIN] Passo alla schermata <Registrazione>")
        startActivity(Intent(this, Registrazione::class.java))
    }
}