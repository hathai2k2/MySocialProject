package com.example.mysocialproject.extension

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Constant {
    val MIGRATION_2_3 = object : Migration(15, 16) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE audio ADD COLUMN sentiments TEXT")
        }
    }

    val TABLE_NAME = "social_database"
}