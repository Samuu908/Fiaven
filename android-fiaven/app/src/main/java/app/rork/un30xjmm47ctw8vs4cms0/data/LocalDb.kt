package app.rork.un30xjmm47ctw8vs4cms0.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Offline-first local store. Every entity is a JSON row keyed by (kind, id).
 * `dirty = 1` means the change has not reached the server yet.
 */
class LocalDb(context: Context) : SQLiteOpenHelper(context, "fiaven.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE records (
                kind TEXT NOT NULL, id TEXT NOT NULL, data TEXT NOT NULL,
                updated_at INTEGER NOT NULL, deleted INTEGER NOT NULL DEFAULT 0,
                dirty INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (kind, id))"""
        )
        db.execSQL("CREATE TABLE meta (key TEXT PRIMARY KEY, value TEXT NOT NULL)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    @Synchronized
    fun allRows(): List<LocalRow> {
        val out = ArrayList<LocalRow>()
        readableDatabase.rawQuery("SELECT kind, id, data, updated_at, deleted, dirty FROM records", null).use { c ->
            while (c.moveToNext()) {
                out += LocalRow(c.getString(0), c.getString(1), c.getString(2), c.getLong(3), c.getInt(4) == 1, c.getInt(5) == 1)
            }
        }
        return out
    }

    @Synchronized
    fun dirtyRows(limit: Int): List<LocalRow> {
        val out = ArrayList<LocalRow>()
        readableDatabase.rawQuery(
            "SELECT kind, id, data, updated_at, deleted, dirty FROM records WHERE dirty = 1 ORDER BY updated_at LIMIT $limit",
            null
        ).use { c ->
            while (c.moveToNext()) {
                out += LocalRow(c.getString(0), c.getString(1), c.getString(2), c.getLong(3), c.getInt(4) == 1, true)
            }
        }
        return out
    }

    @Synchronized
    fun pendingCount(): Int =
        readableDatabase.rawQuery("SELECT COUNT(*) FROM records WHERE dirty = 1", null).use { c ->
            if (c.moveToFirst()) c.getInt(0) else 0
        }

    @Synchronized
    fun putLocal(kind: String, id: String, data: String, deleted: Boolean = false) {
        val v = ContentValues().apply {
            put("kind", kind); put("id", id); put("data", data)
            put("updated_at", System.currentTimeMillis())
            put("deleted", if (deleted) 1 else 0); put("dirty", 1)
        }
        writableDatabase.insertWithOnConflict("records", null, v, SQLiteDatabase.CONFLICT_REPLACE)
    }

    /**
     * Writes a record from a backup only if it is missing or older here.
     * Keeps the original timestamp so newer server data still wins. Returns true when written.
     */
    @Synchronized
    fun putImported(kind: String, id: String, data: String, updatedAt: Long): Boolean {
        var write = true
        readableDatabase.rawQuery("SELECT updated_at, deleted FROM records WHERE kind = ? AND id = ?", arrayOf(kind, id)).use { c ->
            if (c.moveToFirst() && c.getLong(0) >= updatedAt) write = false
        }
        if (!write) return false
        val v = ContentValues().apply {
            put("kind", kind); put("id", id); put("data", data)
            put("updated_at", maxOf(updatedAt, 1L))
            put("deleted", 0); put("dirty", 1)
        }
        writableDatabase.insertWithOnConflict("records", null, v, SQLiteDatabase.CONFLICT_REPLACE)
        return true
    }

    /** Applies a sync round result atomically. */
    @Synchronized
    fun applySync(sent: List<LocalRow>, response: SyncResponse) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            // Mark sent rows clean if they weren't edited again meanwhile.
            for (row in sent) {
                db.execSQL(
                    "UPDATE records SET dirty = 0 WHERE kind = ? AND id = ? AND updated_at = ?",
                    arrayOf<Any>(row.kind, row.id, row.updatedAt)
                )
            }
            for (r in response.rejected) {
                db.execSQL("DELETE FROM records WHERE kind = ? AND id = ?", arrayOf(r.kind, r.id))
            }
            for (rec in response.records) {
                var skip = false
                db.rawQuery(
                    "SELECT updated_at, dirty FROM records WHERE kind = ? AND id = ?",
                    arrayOf(rec.kind, rec.id)
                ).use { c ->
                    if (c.moveToFirst()) {
                        val localUpdated = c.getLong(0)
                        val dirty = c.getInt(1) == 1
                        if (dirty && localUpdated > rec.updatedAt) skip = true
                    }
                }
                if (skip) continue
                val v = ContentValues().apply {
                    put("kind", rec.kind); put("id", rec.id); put("data", rec.data)
                    put("updated_at", rec.updatedAt); put("deleted", if (rec.deleted) 1 else 0); put("dirty", 0)
                }
                db.insertWithOnConflict("records", null, v, SQLiteDatabase.CONFLICT_REPLACE)
            }
            setMetaInternal(db, "cursor", response.cursor.toString())
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    @Synchronized
    fun getMeta(key: String): String? =
        readableDatabase.rawQuery("SELECT value FROM meta WHERE key = ?", arrayOf(key)).use { c ->
            if (c.moveToFirst()) c.getString(0) else null
        }

    @Synchronized
    fun setMeta(key: String, value: String) = setMetaInternal(writableDatabase, key, value)

    private fun setMetaInternal(db: SQLiteDatabase, key: String, value: String) {
        val v = ContentValues().apply { put("key", key); put("value", value) }
        db.insertWithOnConflict("meta", null, v, SQLiteDatabase.CONFLICT_REPLACE)
    }

    @Synchronized
    fun clearAll() {
        writableDatabase.execSQL("DELETE FROM records")
        writableDatabase.execSQL("DELETE FROM meta")
    }
}
