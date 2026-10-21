package com.icm2630.proyecto.data.remote

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore


object FirebaseDb {

    private const val TAG = "FirebaseDb"

    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }


    fun probarConexion() {
        firestore.collection("_conexion").document("prueba")
            .set(mapOf("ultimaConexion" to FieldValue.serverTimestamp()))
            .addOnSuccessListener { Log.d(TAG, "Conexión con Firestore OK") }
            .addOnFailureListener { e -> Log.e(TAG, "Error conectando con Firestore", e) }
    }
}