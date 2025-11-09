package com.example.hyperlocal_forum.data
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseRepository constructor() {
    private val functions = FirebaseFunctions.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun callCloudFunction(data: Map<String, Any>): String {
        return try {
            val result = functions
                .getHttpsCallable("yourFunctionName")
                .call(data)
                .await()

            result.data as String
        } catch (e: Exception) {
            throw Exception("Ошибка при вызове функции: ${e.message}")
        }
    }

    suspend fun addDataToFirestore(data: Map<String, Any>): String {
        return try {
            val documentRef = firestore.collection("yourCollection").add(data).await()
            documentRef.id
        } catch (e: Exception) {
            throw Exception("Ошибка при добавлении данных: ${e.message}")
        }
    }

    suspend fun getDataFromFirestore(documentId: String): Map<String, Any>? {
        return try {
            val document = firestore.collection("yourCollection").document(documentId).get().await()
            if (document.exists()) document.data else null
        } catch (e: Exception) {
            throw Exception("Ошибка при получении данных: ${e.message}")
        }
    }
}