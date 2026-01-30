import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.truewarg.claude.shared.di.appModule
import com.truewarg.claude.ui.App
import org.koin.core.context.startKoin

fun main() {
    // Initialize Koin
    startKoin {
        modules(appModule)
    }

    application {
        Window(onCloseRequest = ::exitApplication, title = "Claude KMP Client") {
            App()
        }
    }
}
