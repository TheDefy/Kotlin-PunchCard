package defy.com.punchcard.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;

public class CommDialog {


    private AlertDialog alertDialog;
    private LayoutInflater inflater;

    private AlertDialog.Builder builder;

    private OnDialogClick onDialogClick;


    public CommDialog(Context mContext,OnDialogClick onDialogClick) {

        this.onDialogClick = onDialogClick;

        builder = new AlertDialog.Builder(mContext);

        inflater = ((Activity) mContext).getLayoutInflater();

    }

    public void show(int resource) {

        alertDialog = builder.setView(inflater.inflate(resource, null))
                .setTitle("设置无线音箱频段")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onDialogClick.cancel();
                    }
                }).create();
        alertDialog.show();
    }

    public void cancel() {
        if (alertDialog != null && alertDialog.isShowing())
            alertDialog.cancel();
    }

    public interface OnDialogClick{
        void confirm();
        void cancel();
        String content();
    }
}
