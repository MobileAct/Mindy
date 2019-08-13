package mindy.app

import kotlinx.android.synthetic.main.activity_main.*

class MainPresenter(
    private val textService: ITextService,
    private val mainActivity: MainActivity
) : IMainPresenter {

    override fun initView() {
        mainActivity.text.text = textService.helloWorld()
    }
}