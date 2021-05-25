package it.uninsubria.firebase.firestore

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class Database constructor(){
    val TAG = "Database"
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun addUserToDB (name: String, surname: String, email: String, nickname: String) {
        val utente: MutableMap<String, Any> = HashMap()
        utente["nome"] = name
        utente["cognome"] = surname
        utente["email"] = email
        utente["nickname"] = nickname

        db.collection("utenti")
            .add(utente)
            .addOnSuccessListener { documentReference -> Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.id) }
            .addOnFailureListener { e -> Log.w(TAG, "Error adding document", e) }
    }
}
