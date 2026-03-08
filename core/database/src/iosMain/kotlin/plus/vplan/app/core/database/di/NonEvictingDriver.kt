package plus.vplan.app.core.database.di

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.SQLiteStatement

/**
 * A [SQLiteDriver] wrapper whose connections return [SQLiteStatement] objects that override
 * [SQLiteStatement.close] to reset (but not finalize) the underlying sqlite3_stmt.
 *
 * Background: Room's [androidx.room.coroutines.ConnectionPoolImpl] wraps every prepared statement
 * in a `CachedStatement` backed by an LruCache (default size 25). Two failure modes exist:
 *
 * **Crash 1 – error 21 "statement is closed":** When the LruCache evicts an entry it calls
 * `oldValue.close()` → `sqlite3_finalize()` on the raw native statement. But the `CachedStatement`
 * wrapper for that same handle may still be live (held as `_stmt` in generated DAO code), and its
 * own `close()` subsequently calls `delegate.reset()` on the already-finalized handle.
 *
 * **Crash 2 – error 5 "cannot commit transaction – SQL statements in progress":** If eviction's
 * `close()` is suppressed entirely (pure no-op), evicted statements remain in a "stepped but not
 * reset" state. SQLite refuses to commit a transaction while any statement on the same connection
 * is still active (stepped but not reset), so `TriggerBasedInvalidationTracker.syncTriggers`
 * fails when it tries to execute `END TRANSACTION`.
 *
 * **Fix:** Override `close()` to call `reset()` + `clearBindings()` but NOT `sqlite3_finalize()`.
 * This deactivates the statement (allowing `END TRANSACTION`) without invalidating the handle
 * (so the still-live `CachedStatement` wrapper can safely call `reset()` again later).
 * All native handles are finalized when the connection closes (handled by the native layer).
 */
internal class NonEvictingDriver(private val delegate: SQLiteDriver) : SQLiteDriver {
    override fun open(fileName: String): SQLiteConnection =
        NonEvictingConnection(delegate.open(fileName))
}

private class NonEvictingConnection(
    private val delegate: SQLiteConnection,
) : SQLiteConnection by delegate {

    override fun prepare(sql: String): SQLiteStatement =
        NonEvictingStatement(delegate.prepare(sql))

    // close() is intentionally delegated as-is so Room can close the whole connection.
}

/**
 * A statement wrapper that overrides [close] to reset (but not finalize) the underlying
 * sqlite3_stmt. All other operations delegate to the real statement.
 *
 * Calling [reset] + [clearBindings] removes the statement from SQLite's "active" (stepped) state,
 * which is required before `END TRANSACTION` can succeed. Not calling the underlying `close()`
 * (i.e. not calling `sqlite3_finalize`) keeps the handle valid so that Room's `CachedStatement`
 * wrapper can still call `reset()` on it after eviction without crashing.
 *
 * The native sqlite3_stmt is finalized implicitly when the parent connection closes.
 */
private class NonEvictingStatement(
    private val delegate: SQLiteStatement,
) : SQLiteStatement by delegate {

    override fun close() {
        // Reset the statement so it is no longer "active" in SQLite's view (prevents
        // SQLITE_BUSY "cannot commit – SQL statements in progress"), but do NOT call
        // delegate.close() (which would sqlite3_finalize the handle and cause error 21
        // "statement is closed" when the still-live CachedStatement later calls reset()).
        delegate.reset()
        delegate.clearBindings()
    }
}
