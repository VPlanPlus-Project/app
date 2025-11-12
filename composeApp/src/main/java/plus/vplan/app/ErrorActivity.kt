package plus.vplan.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.core.FileKit

class ErrorActivity : FragmentActivity() {
    var error by mutableStateOf<Error?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onNewIntent(intent)

        FileKit.init(this)
        enableEdgeToEdge()

        setContent {
            error?.let { error ->
                ErrorPage(
                    error = error,
                    onOpenAppInfo = remember { {
                        val intent = Intent().apply {
                            action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = android.net.Uri.fromParts("package", packageName, null)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }

                        startActivity(intent)
                    } },
                    onRestartApp = remember { {
                        val intent = packageManager?.getLaunchIntentForPackage(packageName)
                        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        finish()
                    } }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        Logger.d("Received intent in ErrorActivity: $intent")

        val stacktrace = intent.getStringExtra("stacktrace") ?: "No stacktrace"
        error = Error(stacktrace = stacktrace)
    }
}