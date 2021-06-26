package it.uninsubria.firebase

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import it.uninsubria.models.User
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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
            .addOnSuccessListener { docRef -> Log.d(TAG, "DocumentSnapshot Utente aggiunto con ID: " + docRef.id) }
            .addOnFailureListener { e -> Log.w(TAG, "[ERRORE] caricamento utente del DB", e) }
    }

    fun addTalkToDB (email: String, text: String, linkSource: String, callback: (Boolean, String) -> Unit) {
        val talk: MutableMap<String, Any> = HashMap()
        Log.i(TAG, "--------------------------------------------------- aggiungo a DB: $email")
        getNicknameByEmail(email) { nicknameRes ->
            if(nicknameRes.isNotEmpty()) {
                talk["nickname"] = nicknameRes
                talk["content"] = text
                talk["linkSource"] = linkSource
                talk["timestamp"] = FieldValue.serverTimestamp()

                db.collection("talks")
                        .add(talk)
                        .addOnSuccessListener { docRef ->
                            Log.d(TAG, "DocumentSnapshot Talk aggiunto con ID: " + docRef.id)
                            callback(true, docRef.id)
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "[ERRORE] caricamento talk del DB", e)
                            callback(false, "")
                        }
                talk.clear()
            } else {
                Log.e(TAG, "nickname non trovato")
            }
        }

    }

    fun getNicknameByEmail(emailValue: String?, callback: (String) -> Unit) {
        db.collection("utenti")
                .whereEqualTo("email", emailValue)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (doc in task.result!!) {
                            Log.d(TAG, doc.id + " => " + doc.data)
                            if (doc.data["email"]?.equals(emailValue)!!) {
                                callback(doc.data["nickname"] as String)
                            }
                        }
                    }
                }
        callback("")
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
        db.collection("utenti")
                .whereEqualTo("nickname", nickname)
                .get()
                .addOnCompleteListener { task -> callback(task) }
    }

    fun deleteTalks(talksUid: String, callback: (Boolean) -> Unit) {
        db.collection("talks")
                .document(talksUid)
                .delete()
                .addOnSuccessListener {
                    Log.i(TAG, "$talksUid RIMOSSO")
                    callback(true)
                }.addOnFailureListener {
                    Log.i(TAG, "$talksUid RIMOZIONE FALLITA")
                    callback(false)
                }
    }

    fun getSimilarProfile(userToFind: String, callback: (ArrayList<User>?) -> Unit) {
        var matchUserList: ArrayList<User>
        db.collection("utenti")
                .orderBy("nickname", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener { task ->
                    matchUserList = arrayListOf()
                    for (doc in task) {
                        if((doc.data["nickname"].toString().toLowerCase(Locale.ROOT)).contains(userToFind.toLowerCase(Locale.ROOT)) or userToFind.isEmpty()) {
                            matchUserList.add(User(doc.data["nickname"] as String, doc.data["nome"] as String, doc.data["cognome"] as String))
                        }
                    }
                    for(u: User in matchUserList) {
                        Log.i(TAG, "--------------------------------------------> ${u.nickname}")
                    }
                    callback(matchUserList)
                }.addOnFailureListener {
                    callback(null)
                }
    }
}