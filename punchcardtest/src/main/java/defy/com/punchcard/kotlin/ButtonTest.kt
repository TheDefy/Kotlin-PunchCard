package defy.com.punchcard.kotlin

import android.util.Log

class ButtonTest : Clickable, Focusable {

    override fun showOff(): Int {
        return super<Focusable>.showOff()
        return super<Clickable>.showOff()
    }

    override fun click() {
        println("i was clicked")
        Log.d("88888", "i was clicked")

        showOff()
    }

}