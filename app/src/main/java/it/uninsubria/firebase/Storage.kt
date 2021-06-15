package it.uninsubria.firebase

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.File

class Storage {

    var myStorage: FirebaseStorage = Firebase.storage

    fun downloadBitmap(path: String, callback: (Bitmap?) -> Unit) {
        val bitmapRef = myStorage.reference.child(path)
        val localFile = File.createTempFile("tempImg", "jpg")
        val fdt: FileDownloadTask = bitmapRef.getFile(localFile)
        fdt.addOnSuccessListener {
            val filePath = localFile.path
            val bitmap = BitmapFactory.decodeFile(filePath)
            callback(bitmap)
        }.addOnFailureListener{
            callback(null)
        }
    }

    fun uploadBitmap(path: String, imgData: ByteArray, callback: (Boolean) -> Unit) {
        val bitmapRef = myStorage.reference.child(path)
        bitmapRef.putBytes(imgData).addOnSuccessListener{
            callback(true)
        }.addOnFailureListener {
            callback(false)
        }
    }
}