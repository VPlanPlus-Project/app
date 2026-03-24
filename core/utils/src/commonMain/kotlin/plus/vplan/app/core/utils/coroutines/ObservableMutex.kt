package plus.vplan.app.core.utils.coroutines

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ObservableMutex {
  private val mutex = Mutex()
  private val _isLocked = MutableStateFlow(false)
  val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

  // Use this for suspending critical sections
  suspend fun <T> withLock(block: suspend () -> T): T {
    // mark locked before acquiring to reflect intent, or after acquiring to reflect actual lock.
    // safer to mark after acquire to avoid false positives:
    return mutex.withLock {
      _isLocked.value = true
      try {
        block()
      } finally {
        _isLocked.value = false
      }
    }
  }

  // Non-blocking attempt to run critical section if lock available
  suspend fun <T> tryWithLock(block: suspend () -> T): T? {
    return if (mutex.tryLock()) {
      try {
        _isLocked.value = true
        block()
      } finally {
        _isLocked.value = false
        mutex.unlock()
      }
    } else {
      null
    }
  }

  // Expose simple helpers when you just want to lock/unlock manually
  suspend fun lock() {
    mutex.lock()
    _isLocked.value = true
  }

  fun unlock() {
    _isLocked.value = false
    mutex.unlock()
  }
}
