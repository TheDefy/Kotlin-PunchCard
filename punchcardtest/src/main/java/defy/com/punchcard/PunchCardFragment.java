package defy.com.punchcard;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bbtree.baselib.base.BaseFragment;
import com.bbtree.baselib.net.ResultObject;
import com.bigkoo.pickerview.TimePickerView;
import com.bigkoo.pickerview.listener.CustomListener;
import com.orhanobut.logger.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DefaultObserver;

/**
 * Created by chenglei on 2017/9/21.
 */

public class PunchCardFragment extends BaseFragment {

    @BindView(R.id.btn_go_work)
    Button goWork;

    @BindView(R.id.btn_out_work)
    Button outWork;

    @BindView(R.id.btn_punch_card_time)
    Button punchTime;

    @BindView(R.id.btn_punch_card)
    Button punchCard;

    private TimePickerView pvCustomTime;
    private Unbinder bind;
    private Date punchDate;

    //****************************程磊start****************************
    private String morningUrl1 = "http://cs-img.bbtree.com/4833/201804/4kcajmf0gkf1n_.jpg";
    private String morningUrl2 = "http://cs-img.bbtree.com/4833/201804/4kcajmf0gk7vh_.jpg";
//    private String morningUrl3 = "http://cs-img.bbtree.com/4833/201803/4kcajmaihhdb6_.jpg";
//    private String morningUrl4 = "http://cs-img.bbtree.com/4833/201803/4kcajmaihhcaa_.jpg";
//    private String morningUrl5 = "http://cs-img.bbtree.com/4833/201712/4kcajm472io2q_.jpg";

    private List<String> morningUrlList = new ArrayList<String>() {{
        add(morningUrl1);
        add(morningUrl2);
//        add(morningUrl3);
//        add(morningUrl4);
//        add(morningUrl5);
    }};

    private String nightImgUrl1 = "http://cs-img.bbtree.com/4833/201804/4kcajmf0gk7lf_.jpg";
    private String nightImgUrl2 = "http://cs-img.bbtree.com/4833/201804/4kcajmf0gk34t_.jpg";
//    private String nightImgUrl3 = "http://cs-img.bbtree.com/4833/201803/4kcajmagtohh9_.jpg";
//    private String nightImgUrl4 = "http://cs-img.bbtree.com/4833/201803/4kcajmagtodih_.jpg";

    private List<String> nightUrlList = new ArrayList<String>() {{
        add(nightImgUrl1);
        add(nightImgUrl2);
//        add(nightImgUrl3);
//        add(nightImgUrl4);
    }};
    //****************************程磊end****************************

/*
    // ****************************张龙start****************************
    private String morningUrl1 = "http://cs-img.bbtree.com/4833/201804/4kcajmf2mmvak_.jpg";
    private String morningUrl4 = "http://cs-img.bbtree.com/4833/201804/4kcajmf2mmtln_.jpg";

    private List<String> morningUrlList = new ArrayList<String>() {{
        add(morningUrl1);
        add(morningUrl4);
    }};

    private String nightImgUrl1 = "http://cs-img.bbtree.com/4833/201804/4kcajmf3n121p_.jpg";
    private String nightImgUrl3 = "http://cs-img.bbtree.com/4833/201804/4kcajmf3n115d_.jpg";

    private List<String> nightUrlList = new ArrayList<String>() {{
        add(nightImgUrl1);
        add(nightImgUrl3);
    }};

/****************************张龙end****************************
*/

    private String imgUrl;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initCustomTimePicker();
    }

    @Override
    public int contentView() {
        return R.layout.punch_card_fragment;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        bind = ButterKnife.bind(this, getContentView());
        setViewListener();
    }

    private void setViewListener() {
        goWork.setOnClickListener(onClickListener);
        outWork.setOnClickListener(onClickListener);
        punchTime.setOnClickListener(onClickListener);
        punchCard.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_go_work://上班
                    Random random = new Random();
                    int i = random.nextInt(morningUrlList.size());
                    imgUrl = morningUrlList.get(i);
                    goWork.setText("上班打卡图片已生成");
                    break;
                case R.id.btn_out_work://下班
                    Random random1 = new Random();
                    int i1 = random1.nextInt(nightUrlList.size());
                    imgUrl = nightUrlList.get(i1);
                    outWork.setText("下班打卡图片已生成");
                    break;
                case R.id.btn_punch_card_time://打卡时间
                    if (null != pvCustomTime)
                        pvCustomTime.show();
                    break;
                case R.id.btn_punch_card://打卡
                    if (TextUtils.isEmpty(imgUrl)) {
                        Toast.makeText(mContext, "打卡图片未生成，请选择", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    if (punchDate == null) {
                        Toast.makeText(mContext, "打卡时间未生成，请选择", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    CardPunchRecord punchRecord = new CardPunchRecord();
                    punchRecord.setCard_holder("/storage/emulated/0/Android/data/com.bbtree.cardreader/files/Pictures/zhs_card_1505981282463.jpg");
                    punchRecord.setCardNo("100011025169656");
                    punchRecord.setMacId("916159227");
                    // TODO zhou yun long
//                    punchRecord.setCardNo("100008016175568");
//                    punchRecord.setMacId("3237089904");
                    punchRecord.setImgUrl(imgUrl);
                    punchRecord.setHas_sync(true);
                    punchRecord.setHas_upload(true);
                    UUID uuid = UUID.randomUUID();
                    punchRecord.setRecordId(uuid.toString());
                    punchRecord.setPunchTime(punchDate.getTime());

                    PunchCardModel.getInstance().uploadCardRecords(Arrays.asList(punchRecord))
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new DefaultObserver<ResultObject>() {
                                @Override
                                public void onNext(@NonNull ResultObject resultObject) {
                                    Toast.makeText(mContext, "上传成功", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onError(@NonNull Throwable e) {
                                    Logger.t(" punchchenglei request json").i(e.getMessage());
                                }

                                @Override
                                public void onComplete() {

                                }
                            });

                    break;
            }
        }
    };

    /**
     * @description 注意事项：
     * 1.自定义布局中，id为 optionspicker 或者 timepicker 的布局以及其子控件必须要有，否则会报空指针.
     * 具体可参考demo 里面的两个自定义layout布局。
     * 2.因为系统Calendar的月份是从0-11的,所以如果是调用Calendar的set方法来设置时间,月份的范围也要是从0-11
     * setRangDate方法控制起始终止时间(如果不设置范围，则使用默认时间1900-2100年，此段代码可注释)
     */
    private void initCustomTimePicker() {
        Calendar selectedDate = Calendar.getInstance();//系统当前时间
        Calendar startDate = Calendar.getInstance();
        startDate.set(2014, 1, 23);
        Calendar endDate = Calendar.getInstance();
        endDate.set(2027, 2, 28);
        //时间选择器 ，自定义布局
        pvCustomTime = new TimePickerView.Builder(mContext, new TimePickerView.OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {//选中事件回调
                punchDate = date;
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                punchTime.setText(format.format(date));
            }
        })
                .setDate(selectedDate)
                .setRangDate(startDate, endDate)
                .setLayoutRes(R.layout.pickerview_custom_time, new CustomListener() {

                    @Override
                    public void customLayout(View v) {
                        final TextView tvSubmit = (TextView) v.findViewById(R.id.tv_finish);
                        ImageView ivCancel = (ImageView) v.findViewById(R.id.iv_cancel);
                        tvSubmit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pvCustomTime.returnData();
                                pvCustomTime.dismiss();
                            }
                        });
                        ivCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pvCustomTime.dismiss();
                            }
                        });
                    }
                })
                .setType(new boolean[]{true, true, true, true, true, true})
                .setLabel("年", "月", "日", "时", "分", "秒")
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .setDividerColor(0xFF24AD9D)
                .build();

    }

    @Override
    public void onDestroy() {
        bind.unbind();
        super.onDestroy();
    }
}
