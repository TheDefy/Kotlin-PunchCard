package defy.com.punchcard

import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bbtree.baselib.net.ResultObject
import com.bigkoo.pickerview.TimePickerView
import com.bigkoo.pickerview.TimePickerView.Builder
import com.bigkoo.pickerview.TimePickerView.OnTimeSelectListener
import com.bigkoo.pickerview.listener.CustomListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DefaultObserver
import kotlinx.android.synthetic.main.punch_card_kotlin.*
import java.text.SimpleDateFormat
import java.util.*

fun Context.toast(msg: String = "test Toast") {
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}

class PunchCardKotlin : Fragment() {

    private lateinit var pvCustomTime: TimePickerView

    private var punchDate: Date? = null

    private var imgUrl: String? = null


    val cl_morningUrl1: String = "http://cs-img.bbtree.com/4833/201805/4kcajmhl3s26b_.jpg"
    val cl_morningUrl2: String = "http://cs-img.bbtree.com/4833/201805/4kcajmhl3s26b_.jpg"
    val cl_morningUrl3: String = "http://cs-img.bbtree.com/4833/201805/4kcajmhl3s26b_.jpg"


    val cl_nightImgUrl1: String = "http://cs-img.bbtree.com/4833/201806/4kcajmhtpv2jo_.jpg"
    val cl_nightImgUrl2: String = "http://cs-img.bbtree.com/4833/201806/4kcajmhtpv3kr_.jpg"

    val cl_morningUrls = arrayListOf(cl_morningUrl1, cl_morningUrl2, cl_morningUrl3)
    val cl_nightUrls = arrayListOf(cl_nightImgUrl1, cl_nightImgUrl2)

    val cl_over_time: String = "http://cs.bbtree.com/0/201806/4kcajmiv3g0f5_zhs_card_cl.png"


    //---------------------------zhangLong--------------------------------------

    val zl_morningUrl1: String = "http://cs-img.bbtree.com/4833/201807/4kcajmkhptg9d_.jpg"
    val zl_morningUrl2: String = "http://cs-img.bbtree.com/4833/201807/4kcajmkhptg9d_.jpg"
    val zl_morningUrl3: String = "http://cs-img.bbtree.com/4833/201807/4kcajmkhptg9d_.jpg"


    val zl_nightImgUrl1: String = "http://cs-img.bbtree.com/4833/201807/4kcajmlpck4ra_.jpg"
    val zl_nightImgUrl2: String = "http://cs-img.bbtree.com/4833/201807/4kcajmlpck4ra_.jpg"

    val zl_morningUrls = arrayListOf(zl_morningUrl1, zl_morningUrl2, zl_morningUrl3)
    val zl_nightUrls = arrayListOf(zl_nightImgUrl1, zl_nightImgUrl2)

    val zl_over_time: String = "http://cs-img.bbtree.com/4833/201806/4kcajmisuspdq_.jpg"

    //---------------------------zhangLong--------------------------------------


    //--------------------------lixiaowen----------------------------------------

    val lxw_morningUrl1: String = "http://cs-img.bbtree.com/4833/201807/4kcajmm5apg63_.jpg"
    val lxw_morningUrl2: String = "http://cs-img.bbtree.com/4833/201807/4kcajmm5apg63_.jpg"
    val lxw_morningUrl3: String = "http://cs-img.bbtree.com/4833/201807/4kcajmm5apg63_.jpg"


    val lxw_nightImgUrl1: String = "http://cs-img.bbtree.com/4833/201807/4kcajmlk8mqdj_.jpg"
    val lxw_nightImgUrl2: String = "http://cs-img.bbtree.com/4833/201807/4kcajmlk8mqdj_.jpg"

    val lxw_morningUrls = arrayListOf(lxw_morningUrl1, lxw_morningUrl2, lxw_morningUrl3)
    val lxw_nightUrls = arrayListOf(lxw_nightImgUrl1, lxw_nightImgUrl2)

    val lxw_over_time: String = "http://cs-img.bbtree.com/4833/201807/4kcajmln4dc83_.jpg"

    //--------------------------lixiaowen----------------------------------------

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater!!.inflate(R.layout.punch_card_kotlin, container, false)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewListener()
    }

    private fun setViewListener() {
        //上班
        btn_go_work.setOnClickListener {
            activity.toast("上班打卡图片已生成")
            btn_go_work.text = "上班图"
            btn_go_work.setTextColor(android.graphics.Color.RED)

            // TODO
            //val nextInt = Random().nextInt(cl_morningUrls.size)
            //imgUrl = cl_morningUrls[nextInt]

            //val nextInt = Random().nextInt(zl_morningUrls.size)
            //imgUrl = zl_morningUrls[nextInt]

            val nextInt = Random().nextInt(lxw_morningUrls.size)
            imgUrl = lxw_morningUrls[nextInt]
        }
        //下班
        btn_out_work.setOnClickListener {
            activity.toast("下班打卡图片已生成")
            btn_out_work.text = "下班图"
            btn_out_work.setTextColor(android.graphics.Color.RED)
            // TODO
            // val nextInt = Random().nextInt(cl_nightUrls.size)
            // imgUrl = cl_nightUrls[nextInt]

            //val nextInt = Random().nextInt(zl_nightUrls.size)
            //imgUrl = zl_nightUrls[nextInt]

            val nextInt = Random().nextInt(lxw_nightUrls.size)
            imgUrl = lxw_nightUrls[nextInt]
        }
        //无图
        btn_null_photo.setOnClickListener {
            activity.toast("您选择了不上传图片")
            btn_null_photo.text = "不上传图"
            btn_null_photo.setTextColor(android.graphics.Color.RED)
        }
        //选择打卡时间
        btn_punch_card_time.setOnClickListener {
            pvCustomTime.show()
        }
        // 加班
        btn_overtime.setOnClickListener {
            activity.toast("加班打卡图片已生成")
            btn_overtime.text = "加班图"
            btn_overtime.setTextColor(android.graphics.Color.RED)
            // TODO
            // imgUrl = cl_over_time

            //imgUrl = zl_over_time
            imgUrl = lxw_over_time
        }
        //打卡
        btn_punch_card.setOnClickListener {

            if (punchDate == null) {
                activity.toast("打卡时间未生成，请选择")
                return@setOnClickListener
            }
            // TODO
            // val initPunchCardInfo = initPunchCardInfo()

            //val initPunchCardInfo = initPunchCardInfoLong()

            val initPunchCardInfo = initPunchCardInfoWen()

            PunchCardModel.getInstance().uploadCardRecords(arrayListOf(initPunchCardInfo))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : DefaultObserver<ResultObject>() {
                        override fun onComplete() {
                        }

                        override fun onNext(t: ResultObject) {
                            activity.toast("上传成功")
                        }

                        override fun onError(e: Throwable) {
                            Log.d("88888", "${e.message}")
                        }
                    })
        }
    }

    /**
     *  组装打卡上传信息 chengLei
     */
    private fun initPunchCardInfo(): CardPunchRecord {
        val punchRecord = CardPunchRecord()
        punchRecord.card_holder = "/storage/emulated/0/Android/data/com.bbtree.cardreader/files/Pictures/zhs_card_1505981282463.jpg"
        punchRecord.cardNo = "100011025169656"
        punchRecord.macId = "916159227"
        punchRecord.imgUrl = imgUrl
        punchRecord.isHas_sync = true
        punchRecord.isHas_upload = true
        punchRecord.recordId = UUID.randomUUID().toString()
        punchRecord.punchTime = punchDate!!.time
        return punchRecord
    }

    /**
     * 组装打卡上传信息 zhangLong
     */
    private fun initPunchCardInfoLong(): CardPunchRecord {
        val punchRecord = CardPunchRecord()
        punchRecord.card_holder = "/storage/emulated/0/Android/data/com.bbtree.cardreader/files/Pictures/zhs_card_1505981282463.jpg"
        punchRecord.cardNo = "100008016175568"
        punchRecord.macId = "3237089904"
        punchRecord.imgUrl = imgUrl
        punchRecord.isHas_sync = true
        punchRecord.isHas_upload = true
        punchRecord.recordId = UUID.randomUUID().toString()
        punchRecord.punchTime = punchDate!!.time
        return punchRecord
    }


    /**
     * 组装打卡上传信息 lixiaowen 3823883662	201702190000023
     */
    private fun initPunchCardInfoWen(): CardPunchRecord {
        val punchRecord = CardPunchRecord()
        punchRecord.card_holder = "/storage/emulated/0/Android/data/com.bbtree.cardreader/files/Pictures/zhs_card_1505981282463.jpg"
        punchRecord.cardNo = "201702190000023"
        punchRecord.macId = "3823883662"
        punchRecord.imgUrl = imgUrl
        punchRecord.isHas_sync = true
        punchRecord.isHas_upload = true
        punchRecord.recordId = UUID.randomUUID().toString()
        punchRecord.punchTime = punchDate!!.time
        return punchRecord
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initCustomTimePicker()
    }

    /**
     * 时间选择器
     */
    private fun initCustomTimePicker() {
        val selectedDate = Calendar.getInstance()
        val startDate = Calendar.getInstance()
        startDate.set(2014, 1, 23)
        val endDate = Calendar.getInstance()
        endDate.set(2027, 2, 28)
        pvCustomTime = Builder(activity, OnTimeSelectListener() { date: Date, view: View? ->
            punchDate = date
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            btn_punch_card_time.text = format.format(date)
        }).setDate(selectedDate)
                .setRangDate(startDate, endDate)
                .setLayoutRes(R.layout.pickerview_custom_time, CustomListener {
                    val tvSubmit = it.findViewById(R.id.tv_finish) as TextView
                    val ivCancel = it.findViewById(R.id.iv_cancel) as ImageView

                    tvSubmit.setOnClickListener {
                        pvCustomTime.returnData()
                        pvCustomTime.dismiss()
                    }
                    ivCancel.setOnClickListener {
                        pvCustomTime.dismiss()
                    }
                })
                .setType(booleanArrayOf(true, true, true, true, true, true))
                .setLabel("年", "月", "日", "时", "分", "秒")
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .setDividerColor(0xFF24AD9D.toInt())
                .build()
    }

}