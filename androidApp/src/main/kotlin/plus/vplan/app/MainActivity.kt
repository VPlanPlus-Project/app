package plus.vplan.app

import android.content.Intent
import android.os.Bundle
import android.os.Process
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import co.touchlab.kermit.Logger
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.posthog.PostHog
import io.github.vinceglb.filekit.core.FileKit
import io.ktor.http.URLBuilder
import plus.vplan.app.di.ActivityProviderImpl
import kotlin.system.exitProcess

class MainActivity : FragmentActivity() {

    private var task: StartTask? by mutableStateOf(null)
    private var canStart by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onNewIntent(intent)

        ActivityProviderImpl.currentActivity = this
        FileKit.init(this)
        enableEdgeToEdge()

        PostHog.capture("App.Start")

        setContent {
            (LocalActivity.current as? FragmentActivity)?.let {
                ActivityProviderImpl.currentActivity = it
            }
            if (canStart) App(task)
        }

        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            Thread {
                val intent = Intent(this, ErrorActivity::class.java).apply {
                    putExtra("stacktrace", throwable.stackTraceToString())
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }

                Logger.e(throwable) { "Uncaught exception" }
                startActivity(intent)
                Firebase.crashlytics.recordException(throwable)
                captureError("UncaughtException", throwable.stackTraceToString())

                this.runOnUiThread { this.finish() }
            }.start()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        canStart = false

        intent.let {
            val action = it.action
            val data = it.data
            Logger.d { "Action: $action, Data: $data" }
            if (action == "android.intent.action.VIEW" && data.toString().startsWith("vpp://app/")) {
                val url = URLBuilder(data.toString())
                if (data.toString().startsWith("vpp://app/auth/")) {
                    val token = data.toString().substringAfter("vpp://app/auth/")
                    task = StartTask.VppIdLogin(token)
                } else if (data.toString().startsWith("vpp://app/schulverwalter-reconnect")) {
                    val token = url.pathSegments.last()
                    val vppId = url.parameters["user_id"]!!.toInt()
                    task = StartTask.SchulverwalterReconnectDone(token, vppId)
                }
            }

            if (intent.hasExtra("onClickData")) {
                Logger.d { "Intent Task: ${intent.getStringExtra("onClickData")}" }
                task = getTaskFromNotificationString(intent.getStringExtra("onClickData").orEmpty())
            }
        }

        canStart = true
    }

    override fun onDestroy() {
        super.onDestroy()
        if (ActivityProviderImpl.currentActivity == this) {
            ActivityProviderImpl.currentActivity = null
        }
    }
}