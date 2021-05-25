package it.uninsubria.firebase.authentication

import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import it.uninsubria.talks.Login
import it.uninsubria.talks.Registrazione

class Authentication {
    val TAG = "Authentication"
    var success = false

    fun registraUtenteNomePassword(ref: Registrazione, myAuth: FirebaseAuth, email: String, password: String): Boolean {
        success = false
        myAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(ref) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "[REG] createUserWithEmail:success")
                        val user = myAuth.currentUser
                        success = true
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "[REG] createUserWithEmail:failure", task.exception)
                    }
                }
        return success
    }

    fun autenticazioneUtenteNomePassword(ref: Login, myAuth: FirebaseAuth, email: String, password: String): Boolean {
        success = false
        myAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(ref) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        val user = myAuth.currentUser
                        success = true
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                    }
                }
        return success
    }

}