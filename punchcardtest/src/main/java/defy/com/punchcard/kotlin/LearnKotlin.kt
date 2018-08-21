@file:JvmName("Joins")
package defy.com.punchcard.kotlin

import defy.com.punchcard.kotlin.LearnKotlin.Color.*
import java.sql.DriverManager.println


@JvmOverloads
fun <T> joinToStringss(
        collection: Collection<T>,
        separator: String = "",
        prefix: String = ",",
        postfix: String = ""
): String {
    val result = StringBuffer(prefix)
    for ((index, element) in collection.withIndex()) {
        if (index > 0) result.append(separator)
        result.append(element)
    }
    result.append(postfix)
    return result.toString()
}

class LearnKotlin {

    var a1: Int = 12

    val languages = arrayOf("java", "android")
    fun max(a: Int, b: Int): Int {

        val p = Person("xiaohuo", 3)
        println(p.name)
        p.name = "haha"
        println("${p.name} de nian ling ${p.age}")

        val people = People(p, "nihao")

        println(people.war)
        val p1 = people.p

        p1.name = "sfas"
        p1.age = 10


        val rectangle = Rectangle(9, 8)
        val square = rectangle.isSquare



        a1 = 13
        val s = languages[0]
        println("hello $a1")
        return if (a > b) a else b
    }

    class Person(var name: String, var age: Int)

    class People(val p: Person, var war: String)

    class Rectangle(val height: Int, val width: Int) {
        val isSquare: Boolean
            get() = this.height == this.width && this.height != 0
    }

    enum class Color {
        RED, ORANGE, YELLOW, GREEN, BLUE
    }

    fun getColorStr(color: Color) = when (color) {
        RED -> "red"
        ORANGE -> "orange"
        YELLOW -> "yellow"
        GREEN -> "green"
        BLUE -> "blue"
    }

    fun getColorInt(color: Color) = when (color) {
        LearnKotlin.Color.RED, LearnKotlin.Color.ORANGE -> 0
        LearnKotlin.Color.YELLOW -> 1
        LearnKotlin.Color.GREEN -> 2
        LearnKotlin.Color.BLUE -> 3
    }

    fun max(c1: Color, c2: Color) {
        when (arrayOf(c1, c2)) {
            arrayOf(BLUE, GREEN) -> Color.BLUE
            arrayOf(ORANGE, YELLOW) -> ORANGE
            else -> throw Exception("")

        }
    }

    class Animal


    val aa = 1
    val bb = 2
    var max2 = when {
        aa > bb -> {
            println("$aa")
            aa
        }
        else -> bb
    }

    fun whileTest(b: Boolean) {
        while (b) {

        }
    }

    fun whileTest1() {
        do {
            var i = 1
            i++
        } while (i < 10)
    }

    fun fizzBuzz(i: Int): String {
        when {
            i % 3 == 0 -> return "Fizz"
            i % 5 == 0 -> return "Buzz"
            i % 15 == 0 -> return "FizzBuzz"
            else -> return "$i"
        }
    }

    fun play() {
        for (i in 1..100) {
            println(fizzBuzz(i))
        }
    }


}