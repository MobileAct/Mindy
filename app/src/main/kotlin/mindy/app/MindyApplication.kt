package mindy.app

import android.app.Application
import mindy.IReadOnlyMindy
import mindy.Mindy
import mindy.register
import mindy.resolve

class MindyApplication : Application() {

    @ExperimentalStdlibApi
    lateinit var mindy: IReadOnlyMindy

    @ExperimentalStdlibApi
    override fun onCreate() {
        super.onCreate()

        mindy = Mindy().apply {
            register<ITextService>(isSingletonOnly = true) { TextService() }
            register<IMainPresenter> { MainPresenter(resolve(), resolve()) }
        }
    }
}