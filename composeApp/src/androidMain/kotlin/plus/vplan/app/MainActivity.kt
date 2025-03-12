package plus.vplan.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.core.FileKit
import io.ktor.http.URLBuilder

class MainActivity : FragmentActivity() {

    private var task: StartTask? by mutableStateOf(null)
    private var canStart by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onNewIntent(intent)

        activity = this
        FileKit.init(this)
        enableEdgeToEdge()

        setContent {
            fragmentActivity = LocalActivity.current as FragmentActivity
            if (canStart) App(task)
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
                    task = StartTask.SchulverwalterReconnect(token, vppId)
                }
            }

            if (intent.hasExtra("onClickData")) {
                Logger.d { "Intent Task: ${intent.getStringExtra("onClickData")}" }
                task = getTaskFromNotificationString(intent.getStringExtra("onClickData").orEmpty())
            }
        }

        canStart = true
    }
}

lateinit var activity: MainActivity
lateinit var fragmentActivity: FragmentActivity