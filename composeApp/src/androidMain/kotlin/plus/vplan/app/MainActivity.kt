package plus.vplan.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import co.touchlab.kermit.Logger

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity = this

        enableEdgeToEdge()

        var task: StartTask? = null

        intent?.let {
            val action = it.action
            val data = it.data
            Logger.d { "Action: $action, Data: $data" }
            if (action == "android.intent.action.VIEW" && data.toString().startsWith("vpp://app/auth/")) {
                val token = data.toString().substringAfter("vpp://app/auth/")
                task = StartTask.VppIdLogin(token)
            }
        }

        setContent { App(task) }
    }
}

lateinit var activity: MainActivity