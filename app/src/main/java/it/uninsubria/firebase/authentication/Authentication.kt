package it.uninsubria.firebase.authentication

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import it.uninsubria.talks.Login
import it.uninsubria.talks.Registrazione

class Authentication {
    val TAG = "Authentication"

    fun registraUtenteNomePassword(ref: Registrazione, context: Context, myAuth: FirebaseAuth, email: String, password: String) {
        myAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(ref) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "[REG] createUserWithEmail:success")
                        val user = myAuth.currentUser
                        Toast.makeText(context, "Utente registrato con successo", Toast.LENGTH_SHORT).show()
                        Registrazione().chiudiActivity()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "[REG] createUserWithEmail:failure", task.exception)
                        Toast.makeText(context, "Autenticazione Fallita", Toast.LENGTH_SHORT).show()
                    }
                }
    }

    fun autenticazioneUtenteNomePassword(ref: Login, context: Context, myAuth: FirebaseAuth, email: String, password: String) {
        myAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(ref) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        val user = myAuth.currentUser
                        Toast.makeText(context, "Accesso effettuato", Toast.LENGTH_SHORT).show()
                        Login().chiudiActivity()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(context, "Accesso fallito", Toast.LENGTH_SHORT).show()
                    }
                }
    }
}