package it.uninsubria.firebase.firestore

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*


class Database {
    private val TAG = "Database"
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

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

    fun addTalkToDB (email: String, text: String, linkSource: String, callback: (Boolean) -> Unit) {
        val talk: MutableMap<String, Any> = HashMap()
        getNicknameByEmail(email) { nicknameRes ->
            if(nicknameRes != null) {
                talk["nickname"] = nicknameRes
                talk["content"] = text
                talk["linkSource"] = linkSource
                talk["timestamp"] = FieldValue.serverTimestamp()

                db.collection("talks")
                        .add(talk)
                        .addOnSuccessListener { documentReference ->
                            Log.d(TAG, "DocumentSnapshot Talk aggiunto con ID: " + documentReference.id)
                            callback(true)
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "[ERRORE] caricamento talk del DB", e)
                            callback(true)
                        }
            } else {
                Log.e(TAG, "nickname non trovato")
            }
        }

    }

    fun getNicknameByEmail(emailValue: String?, callback: (String?) -> Unit) {
        db.collection("utenti")
                .whereEqualTo("email", emailValue)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            Log.d(TAG, document.id + " => " + document.data)
                            if (document.data["email"]?.equals(emailValue)!!) {
                                callback(document.data["nickname"] as String)
                            }
                        }
                    }
                }
        callback(null)
    }

    fun getTalks(callback: (QuerySnapshot?) -> Unit) {
        db.collection("talks")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.e(TAG, "Firestore error: " + error.message.toString())
                        callback(null)
                    } else {
                        callback(value)
                    }
                }
    }

    fun getTaskForImage(email: String, callback: (Task<QuerySnapshot>) -> Unit ) {
        db.collection("utenti")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener { task -> callback(task) }
    }

    fun getUser(nickname: String, callback: (Task<QuerySnapshot>) -> Unit) {
        Database().db.collection("utenti")
                .whereEqualTo("nickname", nickname)
                .get()
                .addOnCompleteListener { task -> callback(task) }

    }

    fun getSingleUserTalks(nickname: String, callback: (QuerySnapshot?) -> Unit) {
        db.collection("talks")
                .whereEqualTo("nickname", nickname)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.e(TAG, "Firestore error: " + error.message.toString())
                        callback(null)
                    } else {
                        callback(value)
                    }
                }
    }

    fun checkUniqueUser(nickname: String, callback: (Task<QuerySnapshot>) -> Unit) {
        Database().db.collection("utenti")
                .whereEqualTo("nickname", nickname)
                .get()
                .addOnCompleteListener { task -> callback(task) }
    }
}
