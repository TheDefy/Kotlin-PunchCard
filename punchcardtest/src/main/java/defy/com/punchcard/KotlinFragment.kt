package defy.com.punchcard

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.bbtree.baselib.base.BaseFragment
import defy.com.punchcard.kotlin.*
import kotlinx.android.synthetic.main.kotlin_fragment.*
import java.io.BufferedReader
import java.util.*


@JvmOverloads
fun <T> joinToString(
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

const val TAG = "KotlinFragment"

/**
 * Created by chenglei on 2017/9/21.
 */

class KotlinFragment : BaseFragment() {


//    var bind: Unbinder? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        play()
//        mapTest()

//        collectionTest()
//        parameterTest()

//        stringSplitTest()

//        interfaceTest()

        lambdaTest()
    }

    private fun lambdaTest() {

        val list1 = listOf<Int>(1, 2, 3, 4)
        val filterList = list1.filter {
            it % 2 == 0
        }
        Log.d("88888", "filter list: $filterList")

        val mapList = list1.map { it * 2 }
        Log.d("88888", "map list: $mapList")

        val persons = listOf(LearnKotlin.Person("yuanxue", 10), LearnKotlin.Person("chenglei", 31))
        val name = persons.filter { it.age > 11 }.map(LearnKotlin.Person::name)
        Log.d("88888", "name: $name")

        val age = persons.maxBy(LearnKotlin.Person::age)?.age ?: 0
        persons.filter { it.age == age }

        val filter = persons.map(LearnKotlin.Person::name).filter { it.startsWith("c") }
        Log.d("88888", "filter: $filter")

        val toList = persons.asSequence().map(LearnKotlin.Person::name).filter { it.startsWith("y") }.toList()


    }

    val onClickListener = View.OnClickListener { view ->
        when(view.id) {
            R.id.__leak_canary_action->{

            }
            else->{

            }

        }
    }

    private fun tryToCountButtonOnClicks(button: Button): Int {
        var clicks = 0
        var clicksTemp = 0
        button.setOnClickListener {
            clicks++
            clicksTemp = clicks
        }

        return clicksTemp
    }


    override fun contentView(): Int {
        return R.layout.kotlin_fragment
    }

    override fun initView(savedInstanceState: Bundle?) {

    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setViewListener()
    }

    private var clickTemp = 0

    private fun setViewListener() {

        var clicks = 0

        content.text = "kotlin hello world"
        bt_other.text = "kotlin button"

        bt_other.setOnClickListener(View.OnClickListener {
            Toast.makeText(mContext, "bt_other点击了", Toast.LENGTH_LONG).show()

            clicks++
            clickTemp = clicks

        })
    }

    @SuppressLint("MissingSuperCall")
    override fun onDestroyView() {
        super.onDestroyView()
    }

    fun whenTest(i: Int) = when {
        i % 3 == 0 -> "被3整除"
        i % 5 == 0 -> "被5整除"
        i % 15 == 0 -> "被15整除"
        else -> "$i"
    }


    fun play() {
        for (i in 1..100) {
            Log.d("88888", "" + whenTest(i))
            mContext.toast("$i ${whenTest(i)}")
        }
    }

    fun Context.toast(mes: CharSequence) = Toast.makeText(mContext, mes, Toast.LENGTH_LONG).show()

    val binaryReps = TreeMap<Char, String>()

    fun mapTest() {

        for (c in 'A'..'F') {
            val toBinaryString = Integer.toBinaryString(c.toInt())
            binaryReps[c] = toBinaryString
        }
        for ((key, value) in binaryReps) {
            Log.d("88888", "key: $key , value $value")
        }


        val arrayListOf = arrayListOf<String>("ni", "hao", "ma")
        for ((index, element) in arrayListOf.withIndex()) {
            Log.d("88888", "index: $index , element: $element")

            Log.d("88888", "index: $index is letter ${isLetter(index.toString())}")

            Log.d("88888", "element: $element is letter ${isLetter(element)}")

            val parseInt = Integer.parseInt(element)
        }
    }

    fun isLetter(c: String) = c in "a".."z" || c in "A".."Z"

    fun readNumber(reader: BufferedReader): Int? {
        try {
            val readLine = reader.readLine()
            return Integer.parseInt(readLine)
        } catch (e: Exception) {
            return null
        } finally {
            reader.close()
        }
    }

    val set = setOf<Int>(1, 2, 3)
    val list = listOf<Int>(1, 2, 3)
    val map = mapOf<Int, String>(1 to "one", 2 to "two")
    val arrayList = arrayListOf<Int>(1, 2, 3)


    fun collectionTest() {
        Log.d("88888", "set: ${set.javaClass}")
        Log.d("88888", "list: ${list.javaClass}")
        Log.d("88888", "map: ${map.javaClass}")
        Log.d("88888", "arrayList: ${arrayList.javaClass}")

        Log.d("88888", "$set")

        Log.d("88888", "${joinToString(arrayList, ";", "(", ")")}")
        Log.d("88888", "${joinToString(arrayList)}")
        joinToStrings(set)
        joinToStringss(set)

        Log.d("88888", "Kotlin".lastChar().toString())
        list.joinToString(";")

        set.max()

    }

    fun String.lastChar(): Char = this.get(this.length - 1)

    fun parameterTest() {
        val array = arrayOf("a", "b")
        val array1 = arrayOf(1, 2)
        Log.d("88888", "${listOf("c", array)}")
        Log.d("88888", "${listOf("c", *array1)}")
        Log.d("88888", "${listOf<String>("a", *array)}")

        val listOfs = listOf("c", *array1)
        for ((index, element) in listOfs.withIndex()) {
            Log.d("88888", "index: $index , element: $element , ${element.javaClass}")
        }

        val map = mapOf(1 to "one", "23" to 3)

        for ((key, value) in map) {
            Log.d("88888", "key: $key , value: $value , ${value.javaClass}")
        }

        val split = "12.345-6.A".split("\\.|-".toRegex())
        Log.d("88888", "$split")
    }

    val pair = 1 to "one"


    fun stringSplitTest() {
        val path = "/Users/chenglei/Library/Google/chapter.adoc"

        val directory = path.substringBeforeLast("/")
        val fullName = path.substringAfterLast("/")
        val fileName = fullName.substringBeforeLast(".")
        val extension = fullName.substringAfterLast(".")
        Log.d("88888", "directory: $directory, fileName: $fileName, extension: $extension")

        val kotlinLogo = """|//
            .|//
            .|/\
            """.trimMargin(".")
        Log.d("88888", "$kotlinLogo")

//        val kotlinLogo1 = """nihao          .wo
//
//
//
//                                                           .shi""".trimMargin(".")
//
//        Log.d("88888", "${kotlinLogo1}")

        val user = User(10001, "chenglei", "beijing")
        saveUserTwo(user)
    }


    fun interfaceTest() {
//        val button = ButtonTest()
//        button.click()

        val viewText = ViewText()
        viewText.min2()
    }

}




