package com.example

import com.example.data.BookCatalog
import com.example.data.R2StorageConfig
import org.junit.Test

class PrintBookUrlsTest {
    @Test
    fun printUrls() {
        val bookIds = listOf("c7_english_bv", "c8_math_bv", "ssc_physics_bv", "hsc_physics_1_bv")
        println("=== BOOK URL TRACE ===")
        bookIds.forEach { id ->
            val book = BookCatalog.getBookById(id)
            if (book != null) {
                val canonicalUrl = R2StorageConfig.getCanonicalUrl(book)
                println("ID: $id")
                println("Title: ${book.title}")
                println("R2 Key: ${book.r2Key}")
                println("Canonical URL: $canonicalUrl")
                println("---")
            } else {
                println("ID: $id NOT FOUND IN CATALOG")
            }
        }
    }
}
