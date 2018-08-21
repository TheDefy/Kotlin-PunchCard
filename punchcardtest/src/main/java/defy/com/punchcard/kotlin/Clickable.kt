package defy.com.punchcard.kotlin

import android.util.Log

interface Clickable {
    fun click()
    fun showOff() = Log.d("88888", "I'm clickable")

}

interface Focusable{
    fun showOff() = Log.d("88888","I'm focusable")
}