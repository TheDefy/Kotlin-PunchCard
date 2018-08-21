package defy.com.punchcard.kotlin

import android.util.Log

class User(val id: Int, val name: String, val address: String)

fun saveUser(user: User) {
    //声明一个局部函数
    fun validate(value: String, fieldName: String) {
        if (value.isEmpty())
            throw IllegalArgumentException("can't save user ${user.id}:empty $fieldName")
    }

    validate(user.name, "Name")
    validate(user.address, "Address")
    // 保存user到数据库
}

fun saveUserTwo(user: User) {
    user.validateBeforeSave()
    //保存到数据库
    Log.d("88888", "${user.toString()}")
    Log.d("88888", "${user.id},${user.name},${user.address}")
    Log.d("88888", "${user.toStr()}")
    Log.d("88888", "${user.toStri()}")
}

fun User.validateBeforeSave() {
    fun validate(value: String, fieldName: String) {
        if (value.isEmpty())
            throw IllegalArgumentException("can't save user ${this.id}:empty $fieldName")
    }

    validate(this.name, "Name")
    validate(this.address, "Address")
}

fun User.toStr() = this.id.toString() + this.address + this.name
fun User.toStri(): String {
    val sb = StringBuffer()
    return sb.append("id:").append(this.id).append("name:").append(this.name).append("address:").append(this.address).toString()
}