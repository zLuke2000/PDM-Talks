package it.uninsubria.talks

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View

class Login : AppCompatActivity() {
    private val TAG = "Activity_Login"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun registraNuovoCliente(view: View) {
        Log.i(TAG, "Passo alla schermata <Registrazione>")
        startActivity(Intent(this, Registrazione::class.java))
    }
}