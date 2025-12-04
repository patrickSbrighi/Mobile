package com.example.mobile.ui.data

import android.annotation.SuppressLint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

object FirebaseRepository {
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
                    createUserDocument(uid, username, email, role, onSuccess, onFailure)
                } else {
                    onFailure("Errore: UID nullo")
                }
            }
            .addOnFailureListener { onFailure(it.message ?: "Errore registrazione") }
    }

    private fun createUserDocument(uid: String, username: String, email: String, role: UserRole, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
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
            .addOnFailureListener { onFailure("Errore salvataggio dati utente") }
    }

    fun getUserProfile(onResult: (UserProfile?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onResult(null)

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

    fun getUserRole(onResult: (String?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onResult(null)
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc -> onResult(doc.getString("role")) }
            .addOnFailureListener { onResult(null) }
    }

    fun updateUserField(field: String, value: Any, onSuccess: () -> Unit = {}, onFailure: () -> Unit = {}) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).update(field, value)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure() }
    }

    fun createEvent(event: Event, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onFailure("Utente non loggato")
            return
        }

        val newEventRef = db.collection("events").document()

        val eventData = event.copy(
            id = newEventRef.id,
            organizerId = currentUser.uid
        )

        newEventRef.set(eventData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure("Errore creazione evento: ${it.message}") }
    }

    fun listenToEvents(onEventsUpdate: (List<Event>) -> Unit): ListenerRegistration {
        return db.collection("events").addSnapshotListener { snapshot, e ->
            if (e == null && snapshot != null) {
                val events = snapshot.documents.mapNotNull { doc ->
                    try {
                        Event(
                            id = doc.id,
                            title = doc.getString("title") ?: "Senza Titolo",
                            organizerId = doc.getString("organizerId") ?: "",
                            description = doc.getString("description") ?: "",
                            location = doc.getString("location") ?: "",
                            date = doc.getString("date") ?: "",
                            time = doc.getString("time") ?: "",
                            genre = doc.getString("genre") ?: "Altro",
                            imageUrl = doc.getString("imageUrl") ?: "",
                            hype = doc.getLong("hype")?.toInt() ?: 0,
                            lat = doc.getDouble("lat") ?: 0.0,
                            lng = doc.getDouble("lng") ?: 0.0
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                onEventsUpdate(events)
            }
        }
    }
}