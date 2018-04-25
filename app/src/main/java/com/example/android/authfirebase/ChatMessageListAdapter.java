package com.example.android.authfirebase;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.authfirebase.models.ChatMessage;
import com.example.android.authfirebase.models.User;

import java.util.List;

/**
 * Created by Suhanshu on 25-04-2018.
 */

public class ChatMessageListAdapter extends ArrayAdapter<ChatMessage> {

    private static final String TAG = "ChatMessageListAdapter";

    private int mLayoutResource;
    private Context mContext;

    public ChatMessageListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<ChatMessage> objects) {
        super(context, resource, objects);
        mContext = context;
        mLayoutResource = resource;
    }

    public static class ViewHolder {
        TextView name, message;
        ImageView mProfileImage;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mLayoutResource, parent, false);
            holder = new ViewHolder();

            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.message = (TextView) convertView.findViewById(R.id.message);
            holder.mProfileImage = (ImageView) convertView.findViewById(R.id.profile_image);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            holder.name.setText("");
            holder.message.setText("");
        }

        try {
            //set the message
            holder.message.setText(getItem(position).getMessage());

            holder.name.setText(getItem(position).getName());

            Glide.with(mContext)
                    .load(getItem(position).getProfile_image())
                    .into(holder.mProfileImage);


        } catch (NullPointerException e) {
            Log.e(TAG, "getView: NullPointerException: ", e.getCause());
        }

        return convertView;
    }

}

















