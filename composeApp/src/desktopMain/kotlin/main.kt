import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.truewarg.claude.ui.App

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Claude KMP Client") {
        App()
    }
}
