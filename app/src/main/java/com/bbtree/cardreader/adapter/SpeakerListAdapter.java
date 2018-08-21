package com.bbtree.cardreader.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.bbtree.cardreader.R;
import com.bbtree.cardreader.entity.requestEntity.SpeakerBean;

import java.util.List;

/**
 * 音箱列表适配器
 */
public class SpeakerListAdapter extends BaseAdapter {

    private Context mContext;
    private List<SpeakerBean> mSpeakerList;
    private LayoutInflater layoutInflater;
    private ClickListener onClickListener;


    public SpeakerListAdapter(Context mContext, List<SpeakerBean> mSpeakerList) {
        this.mContext = mContext;
        layoutInflater = LayoutInflater.from(mContext);
        this.mSpeakerList = mSpeakerList;
    }

    public void refreshData(List<SpeakerBean> mSpeakerList) {
        this.mSpeakerList = mSpeakerList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mSpeakerList.size();
    }

    @Override
    public Object getItem(int position) {
        return mSpeakerList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.fragment_speaker_setting2_item, parent, false);
            viewHolder.tv_speaker_name = (TextView) convertView.findViewById(R.id.tv_speaker_name);
            viewHolder.tv_speaker_number = (TextView) convertView.findViewById(R.id.tv_speaker_number);
            viewHolder.bt_setting = (Button) convertView.findViewById(R.id.bt_setting);
            viewHolder.bt_delete = (Button) convertView.findViewById(R.id.bt_delete);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        SpeakerBean speakerBean = mSpeakerList.get(position);
        viewHolder.tv_speaker_name.setText(speakerBean.getName());
        viewHolder.tv_speaker_number.setText(String.format(mContext.getResources().getString(R.string.speaker_setting2_item_number), speakerBean.getCode()));
        viewHolder.bt_setting.setTag(position);
        viewHolder.bt_setting.setOnClickListener(buttonClick);
        viewHolder.bt_delete.setTag(position);
        viewHolder.bt_delete.setOnClickListener(buttonClick);
        return convertView;
    }

    private class ViewHolder {
        TextView tv_speaker_name;
        TextView tv_speaker_number;
        Button bt_setting;
        Button bt_delete;
    }

    private View.OnClickListener buttonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            v.setOnClickListener(null);
            int position = (int) v.getTag();
            switch (v.getId()) {
                case R.id.bt_setting:
                    if (onClickListener != null) {
                        onClickListener.startSetFragment(position);
                    }
                    break;
                case R.id.bt_delete:
                    if (onClickListener != null) {
                        onClickListener.deleteSpeaker(position, mSpeakerList);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public interface ClickListener {

        void deleteSpeaker(int position, List<SpeakerBean> mSpeakerList);

        void startSetFragment(int position);
    }

    public void setOnClickListener(ClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
