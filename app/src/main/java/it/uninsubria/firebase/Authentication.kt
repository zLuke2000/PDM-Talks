package it.uninsubria.firebase.firestore

import android.app.Activity
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Authentication {
    private val TAG = "Authentication"
    private val auth: FirebaseAuth = Firebase.auth

    fun signInWithEmailAndPassword(act: Activity, email: String, password: String, callback: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(act) { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "signInWithEmail:success")
                callback(true)
            } else {
                Log.w(TAG, "signInWithEmail:failure", task.exception)
                callback(false)
            }
        }
    }
}