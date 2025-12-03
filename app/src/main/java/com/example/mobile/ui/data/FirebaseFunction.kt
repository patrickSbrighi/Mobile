package com.example.mobile.ui.data

import android.annotation.SuppressLint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

object FirebaseFunction {
    private val auth = FirebaseAuth.getInstance()
    @SuppressLint("StaticFieldLeak")
    private val db = FirebaseFirestore.getInstance()

    fun getCurrentUser() = auth.currentUser

    fun logout() {
        auth.signOut()
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Errore login") }
    }

    fun register(email: String, pass: String, username: String, role: UserRole, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid != null) {
                    val userData = hashMapOf(
                        "username" to username,
                        "email" to email,
                        "role" to role.name,
                        "genres" to emptyList<String>(),
                        "city" to "",
                        "profileImageUrl" to ""
                    )
                    db.collection("users").document(uid).set(userData)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onFailure("Errore salvataggio dati") }
                }
            }
            .addOnFailureListener { onFailure(it.message ?: "Errore registrazione") }
    }

    fun getUserProfile(onResult: (UserProfile?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val profile = UserProfile(
                        username = doc.getString("username") ?: "Utente",
                        email = doc.getString("email") ?: "",
                        role = doc.getString("role") ?: "FAN",
                        genres = (doc.get("genres") as? List<String>) ?: emptyList(),
                        city = doc.getString("city") ?: "",
                        profileImageUrl = doc.getString("profileImageUrl") ?: ""
                    )
                    onResult(profile)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener { onResult(null) }
    }

    fun updateUserField(field: String, value: Any, onSuccess: () -> Unit = {}, onFailure: () -> Unit = {}) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).update(field, value)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure() }
    }

    fun listenToEvents(onEventsUpdate: (List<Event>) -> Unit): ListenerRegistration {
        return db.collection("events").addSnapshotListener { snapshot, e ->
            if (e == null && snapshot != null) {
                val events = snapshot.documents.map { doc ->
                    Event(
                        id = doc.id,
                        title = doc.getString("title") ?: "Senza Titolo",
                        location = doc.getString("location") ?: "",
                        date = doc.getString("date") ?: "",
                        genre = doc.getString("genre") ?: "Altro",
                        hype = doc.getLong("hype")?.toInt() ?: 0,
                        lat = doc.getDouble("lat") ?: 0.0,
                        lng = doc.getDouble("lng") ?: 0.0
                    )
                }
                onEventsUpdate(events)
            }
        }
    }
}