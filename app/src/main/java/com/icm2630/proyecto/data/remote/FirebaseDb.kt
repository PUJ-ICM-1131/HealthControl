package com.icm2630.proyecto.data.remote

import com.google.firebase.firestore.FirebaseFirestore

object FirebaseDb {
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
}
