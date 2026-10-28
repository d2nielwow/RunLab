package com.dmirzayants.runlab.db

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.io.File

class Migration1To2(private val imagesDir: File) : Migration(1, 2) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE running_table_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                imagePath TEXT,
                timestamp INTEGER NOT NULL,
                avgSpeedInKMH REAL NOT NULL,
                distanceInMeters INTEGER NOT NULL,
                timeInMillis INTEGER NOT NULL,
                caloriesBurned INTEGER NOT NULL
            )
            """.trimIndent()
        )

        imagesDir.mkdirs()
        db.query(
            "SELECT id, length(img), timestamp, avgSpeedInKMH, distanceInMeters, timeInMillis, " +
                "caloriesBurned FROM running_table"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val imageSize = if (cursor.isNull(1)) 0 else cursor.getInt(1)
                val imagePath = if (imageSize > 0) {
                    val file = File(imagesDir, "run_legacy_$id.png")
                    copyBlobToFile(db, id, imageSize, file)
                    file.absolutePath
                } else {
                    null
                }
                val values = ContentValues().apply {
                    put("id", id)
                    put("imagePath", imagePath)
                    put("timestamp", cursor.getLong(2))
                    put("avgSpeedInKMH", cursor.getFloat(3))
                    put("distanceInMeters", cursor.getInt(4))
                    put("timeInMillis", cursor.getLong(5))
                    put("caloriesBurned", cursor.getInt(6))
                }
                db.insert("running_table_new", SQLiteDatabase.CONFLICT_REPLACE, values)
            }
        }

        db.execSQL("DROP TABLE running_table")
        db.execSQL("ALTER TABLE running_table_new RENAME TO running_table")
    }

    private fun copyBlobToFile(db: SupportSQLiteDatabase, id: Long, size: Int, file: File) {
        file.outputStream().use { out ->
            var offset = 1
            while (offset <= size) {
                db.query(
                    "SELECT substr(img, ?, ?) FROM running_table WHERE id = ?",
                    arrayOf<Any>(offset, CHUNK_SIZE, id)
                ).use { chunk ->
                    if (chunk.moveToFirst()) out.write(chunk.getBlob(0))
                }
                offset += CHUNK_SIZE
            }
        }
    }

    private companion object {
        const val CHUNK_SIZE = 512 * 1024
    }
}
