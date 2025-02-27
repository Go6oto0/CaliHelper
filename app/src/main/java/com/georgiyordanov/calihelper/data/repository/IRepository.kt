package com.georgiyordanov.calihelper.data.repository

interface IRepository<T> {
    suspend fun create(entity: T): Unit
    suspend fun read(id: String) : T?
    suspend fun readAll(): List<T>?
    suspend fun update(id: String, updates: Map<String, Any?>): Unit
    suspend fun delete(id: String): Unit
}