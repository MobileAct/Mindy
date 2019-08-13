package mindy.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import mindy.create
import mindy.register

class MainActivity : AppCompatActivity() {

    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mindyApplication = application as? MindyApplication ?: return
        val mainPresenter = mindyApplication.mindy.create<IMainPresenter> {
            // pass argument
            register { this@MainActivity }
        }
        mainPresenter.initView()
    }
}
