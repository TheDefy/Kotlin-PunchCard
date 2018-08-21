package defy.com.punchcard.kotlin

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import java.util.jar.Attributes

class RichButtonImp : RichButton() {
    override fun animate() {
    }

    override fun stopAnimating() {
        super.stopAnimating()
    }

    override fun showOff(): Int {
        return super.showOff()
    }

    val innerTests = OuterTest().InnerTest()

}

open class User01(val nickName: String)

class TestUser(nickNames: String) : User01(nickNames)

class TestsUser(name: String) : User01(name)

class Secretive private constructor()


open class ViewText() {
    constructor(context: Context) : this()
    private constructor(context: Context, attributes: Attributes) : this(context)

    fun min() {

        val privateUser = PrivateUser()
        val nickNames = privateUser.nickNames

        Log.d("88888", nickNames)

        val qSubscribingUser = SubscribingUser("chenglei@163.com")
        val nickNames1 = qSubscribingUser.nickNames

        Log.d("88888", nickNames1)

        val client = Client("", 9)

        val clientNew = ClientNew("", 0)

        Log.d("88888", clientNew.toString())

        val copy = clientNew.copy("chenglei", 10)

        Log.d("88888", copy.toString())

        val persons = listOf<PersonNew>(PersonNew("ewqe"), PersonNew("dsds"))

        persons.sortedWith(PersonNew.NameComparator)

        val newInstance = A.newInstance()
        A.TAG

        A.C.newInstance()
        A.C.TAG

        Log.d("88888", B.TAG)

        val strLen = A.newInstance().strLen("")
        Log.d("88888", "$strLen")

        val strLen2 = A.newInstance().strLen01("")
        Log.d("88888", "$strLen2")

        val data = Address("sfs", "shandong", "china")
        val copyAddress = data.copy("sfs", "beijing", "china")

        Log.d("88888", data.toString())
    }

    fun min2() {
        verifyUserInput(null)
    }

    val sum = { x: Int, y: Int -> x + y }

    val lists = listOf<String>("sfs", "ewe")

    val maps = mapOf<Int, String>(1 to "a", 2 to "b")


    fun verifyUserInput(intput: String?) {
        if (intput.isNullOrBlank()) {
            Log.d("88888", "Please fill in the required fields")
        }

        val sum1 = sum(1, 5)
        Log.d("88888", "$sum1")

        lists.forEach {

        }

        maps.forEach {
            println("${it.key},${it.value}")

            Log.d("88888", "[${it.key},${it.value}]")
        }
    }

    fun <T> printHashCode(t: T) = t?.hashCode() ?: 0

    fun <T : Any> printHashCode01(t: T) = t.hashCode()
}

class Button(context: Context) : ViewText(context) {

}

interface UserNew {
    val nickNames: String
}

class PrivateUser : UserNew {
    override val nickNames: String
        get() = "jfsalfjaskf"
}

class SubscribingUser(val email: String) : UserNew {
    override val nickNames = email.substringBefore("@")
}

class Client(var name: String, var age: Int) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readInt()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(age)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Client> {
        override fun createFromParcel(parcel: Parcel): Client {
            return Client(parcel)
        }

        override fun newArray(size: Int): Array<Client?> {
            return arrayOfNulls(size)
        }
    }

}

data class ClientNew(val name: String, val age: Int)


data class PersonNew(val name: String) {

    object NameComparator : Comparator<PersonNew> {

        override fun compare(o1: PersonNew?, o2: PersonNew?): Int = o1!!.name.compareTo(o2!!.name)

    }
}

class A private constructor() {
    companion object C {
        fun newInstance() = A()
        val TAG = "tag"
    }


    fun strLen(s: String) = s.length

    fun strLen01(s: String?) = s!!.length

    fun strLen02(s: String?) = s?.length //?.安全调用运算符

    fun strLen03(s: String?) = if (s == null) 0 else s.length

    fun strLen04(s: String?) = s?.length ?: 0
}

class B private constructor() {
    companion object {
        fun newIntance(): B = B()
        val TAG = B.javaClass.simpleName
    }
}

data class Address(val name: String, val city: String, val country: String)




