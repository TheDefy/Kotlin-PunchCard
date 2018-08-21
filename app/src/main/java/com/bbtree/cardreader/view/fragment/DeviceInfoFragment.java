package com.bbtree.cardreader.view.fragment;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.bbtree.baselib.base.BaseFragment;
import com.bbtree.cardreader.BBTreeApp;
import com.bbtree.cardreader.R;
import com.bbtree.cardreader.entity.eventbus.TTSInfo;
import com.bbtree.cardreader.utils.FastBlur;
import com.bbtree.cardreader.utils.NetWorkUtil;
import com.bbtree.cardreader.utils.ReadPhoneInfo;
import com.bbtree.childservice.utils.QRCodeUtils;
import com.orhanobut.logger.Logger;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


/**
 * Function:
 * Created by BBTree Team
 * Author: EngrZhou
 * Create Date: 2015/07/03
 * Create Time: 下午2:17
 */
public class DeviceInfoFragment extends BaseFragment {

    private String TAG = DeviceInfoFragment.class.getSimpleName();
    private final static boolean qrCodeFucOpen = false;

    @BindView(R.id.device_info_layout)
    LinearLayout device_info_layout;
    @BindView(R.id.registerTip)
    TextView registerTip;
    @BindView(R.id.versionName)
    TextView versionName;
    @BindView(R.id.deviceId)
    TextView deviceId;
    @BindView(R.id.device_tablelayout)
    TableLayout device_tablelayout;
    @BindView(R.id.qrcode)
    ImageView qrcode;

    @Override
    public int contentView() {
        return R.layout.device_info_frg;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        ButterKnife.bind(this, getContentView());
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onStart() {
        super.onStart();
        BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.mipmap.screensaver_default);
        if (bitmapDrawable == null) {
            device_info_layout.setBackgroundColor(getResources().getColor(R.color.white));
            return;
        }

        Bitmap bitmap = bitmapDrawable.getBitmap();
        FastBlur.doBlurJniBitMap(bitmap, 20, true);
        device_info_layout.setBackground(new BitmapDrawable(getResources(), bitmap));

        updateUI();
    }

    public void updateUI() {
        if (!isAdded()) {
            return;
        }
        final String machineID = BBTreeApp.getApp().getMachineAlias();

        if (NetWorkUtil.isNetworkAvailable(mContext)) {
            registerTip.setText(R.string.no_register_tip);
            versionName.setText(ReadPhoneInfo.getAppVersionName(mContext));
            deviceId.setText(machineID);
            device_tablelayout.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(machineID)) {
                StringBuilder builder = new StringBuilder();

                int len = machineID.length();
                for (int i = 0; i < len; i++) {
                    builder.append(machineID.substring(i, i + 1));
                    if (i != len - 1) {
                        builder.append(",");
                    }
                }
                String tts = String.format(getString(R.string.no_register_read_machine_id), builder.toString());
                Logger.i(tts);
                EventBus.getDefault().post(new TTSInfo(tts));
            }
            if (!qrCodeFucOpen) {
                return;
            }
            String path = mContext.getCacheDir().getAbsolutePath();
            final String qtcodePath = path + File.separator + "qt.jpg";
            Observable.create(new ObservableOnSubscribe<String>() {
                @Override
                public void subscribe(ObservableEmitter<String> subscriber) throws Exception {
                    boolean result = QRCodeUtils.createQRImage("bbtree://cardreader/device/" + machineID, 250, 250, null, qtcodePath);
                    if (result) {
                        subscriber.onNext(qtcodePath);
                    }
                }
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(String str) {
                            Logger.i(">>>>>>>qtcodePath:" + str);
                            Picasso.with(BBTreeApp.getApp()).load(new File(str)).into(qrcode);
                        }
                    });

        } else {
            registerTip.setText(R.string.no_register_no_net);
            device_tablelayout.setVisibility(View.GONE);
        }
    }

}
