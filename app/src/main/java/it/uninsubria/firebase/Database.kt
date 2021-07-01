package it.uninsubria.firebase

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import it.uninsubria.models.Profile
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class Database {
    // Current class TAG
    private val TAG = "Database"

    // Firebase Firestore reference
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun addUserToDB (name: String, surname: String, email: String, nickname: String) {
        val currentUser: MutableMap<String, Any> = HashMap()
        currentUser["name"] = name
        currentUser["surname"] = surname
        currentUser["email"] = email
        currentUser["nickname"] = nickname
        currentUser["hasPicture"] = false

        db.collection("profile")
            .add(currentUser)
            .addOnSuccessListener { docRef -> Log.i(TAG, "User added with ID: " + docRef.id) }
            .addOnFailureListener { error -> Log.e(TAG, "User not added: ", error) }
    }

    fun addTalkToDB (email: String, text: String, linkSource: String, hasImage: Boolean, callback: (Boolean, String) -> Unit) {
        val talk: MutableMap<String, Any> = HashMap()
        getNicknameByEmail(email) { nicknameRes ->
            if(nicknameRes.isNotEmpty()) {
                talk["nickname"] = nicknameRes
                talk["content"] = text
                talk["linkSource"] = linkSource
                talk["timestamp"] = FieldValue.serverTimestamp()
                talk["hasImage"] = hasImage

                db.collection("talks")
                        .add(talk)
                        .addOnSuccessListener { docRef ->
                            Log.i(TAG, "Talk added with ID: " + docRef.id)
                            callback(true, docRef.id)
                        }
                        .addOnFailureListener { error ->
                            Log.e(TAG, "Talk not added: ", error)
                            callback(false, "")
                        }
                talk.clear()
            } else {
                Log.e(TAG, "nickname not found")
            }
        }

    }

    fun getNicknameByEmail(emailValue: String?, callback: (String) -> Unit) {
        db.collection("profile")
                .whereEqualTo("email", emailValue)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (doc in task.result!!) {
                            Log.d(TAG, doc.id + " => " + doc.data)
                            callback(doc.data["nickname"] as String)
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
        db.collection("profile")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener { task -> callback(task) }
    }

    fun getUser(nickname: String, callback: (Task<QuerySnapshot>) -> Unit) {
        Database().db.collection("profile")
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
        db.collection("profile")
                .whereEqualTo("nickname", nickname)
                .get()
                .addOnCompleteListener { task -> callback(task) }
    }

    fun deleteTalks(talksUid: String, callback: (Boolean) -> Unit) {
        db.collection("talks")
                .document(talksUid)
                .delete()
                .addOnSuccessListener {
                    Log.i(TAG, "$talksUid successfully removed")
                    callback(true)
                }.addOnFailureListener {
                    Log.e(TAG, "$talksUid not removed")
                    callback(false)
                }
    }

    fun getSimilarProfile(userToFind: String, callback: (ArrayList<Profile>?) -> Unit) {
        var matchProfileList: ArrayList<Profile>
        db.collection("profile")
                .orderBy("nickname", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener { task ->
                    matchProfileList = arrayListOf()
                    for (doc in task) {
                        if((doc.data["nickname"].toString().toLowerCase(Locale.ROOT)).contains(userToFind.toLowerCase(Locale.ROOT)) or userToFind.isEmpty()) {
                            matchProfileList.add(Profile(doc.data["nickname"] as String, doc.data["name"] as String, doc.data["surname"] as String, doc.data["hasPicture"] as Boolean))
                        }
                    }
                    callback(matchProfileList)
                }.addOnFailureListener {
                    callback(null)
                }
    }

    fun setUserPictureStatus(email: String?, status: Boolean, callback: (Boolean) -> Unit) {
        db.collection("profile")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (doc in task.result!!) {
                            if (doc.data["email"]?.equals(email)!!) {
                                db.collection("profile")
                                        .document(doc.id)
                                        .update("hasPicture", status)
                                        .addOnSuccessListener { callback(true) }
                                        .addOnFailureListener { callback(false) }
                            }
                        }
                    }
                }.addOnFailureListener { callback(false) }
    }

    fun getUserPictureStatus(nickname: String?, callback: (Boolean) -> Unit) {
        db.collection("profile")
                .whereEqualTo("nickname", nickname)
                .get()
                .addOnCompleteListener { result ->
                    if(result.isSuccessful) {
                        for(doc in result.result!!) {
                            if(doc.data["hasPicture"] as Boolean) {
                                callback(true)
                            } else {
                                callback(false)
                            }
                        }
                    }
                }.addOnFailureListener { callback(false) }
    }
}
