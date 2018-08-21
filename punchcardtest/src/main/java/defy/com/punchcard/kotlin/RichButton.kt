package defy.com.punchcard.kotlin

abstract class RichButton : Clickable {
    final override fun click() {
    }

    abstract fun animate()

    open fun stopAnimating() {}

    fun animateTwice() {}
}


class OuterTest{
    inner class InnerTest{
        fun getOuter() = this@OuterTest
    }
}
