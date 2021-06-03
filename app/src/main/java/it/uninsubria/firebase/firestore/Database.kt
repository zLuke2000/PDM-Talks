package it.uninsubria.firebase.firestore

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore


class Database {
    val TAG = "Database"
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun addUserToDB (name: String, surname: String, email: String, nickname: String) {
        val utente: MutableMap<String, Any> = HashMap()
        utente["nome"] = name
        utente["cognome"] = surname
        utente["email"] = email
        utente["nickname"] = nickname

        db.collection("utenti")
            .add(utente)
            .addOnSuccessListener { documentReference -> Log.d(TAG, "DocumentSnapshot Utente aggiunto con ID: " + documentReference.id) }
            .addOnFailureListener { e -> Log.w(TAG, "[ERRORE] caricamento utente del DB", e) }
    }

    fun addTalkToDB (nickname: String, text: String, linkSource: String, callback: (Boolean) -> Unit) {
        val talk: MutableMap<String, Any> = HashMap()
        talk["nickname"] = nickname
        talk["content"] = text
        talk["linkSource"] = linkSource
        talk["timestamp"] = FieldValue.serverTimestamp()

        db.collection("talks")
            .add(talk)
            .addOnSuccessListener {
                documentReference -> Log.d(TAG,"DocumentSnapshot Talk aggiunto con ID: " + documentReference.id)
                callback(true)
            }
            .addOnFailureListener {
                e -> Log.w(TAG, "[ERRORE] caricamento talk del DB", e)
                callback(true)
            }
    }
}
