package com.example.android.authfirebase.DialogsClass;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.android.authfirebase.ChatActivity;
import com.example.android.authfirebase.R;
import com.example.android.authfirebase.models.ChatMessage;
import com.example.android.authfirebase.models.Chatroom;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Suhanshu on 22-04-2018.
 */

public class ChatRoomDialogBox extends DialogFragment {

    private static final String TAG = "NewChatroomDialog";

    private EditText mChatroomName;
    private TextView mCreateChatroom;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.layout_chatroom_dialog, container, false);
        mChatroomName = (EditText) view.findViewById(R.id.input_chatroom_name);
        mCreateChatroom = (TextView) view.findViewById(R.id.create_chatroom);

        mCreateChatroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!mChatroomName.getText().toString().equals("")) {
                    Log.d(TAG, "onClick: creating new chat room");


                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    //get the new chatroom unique id
                    String chatroomId = reference
                            .child("chatrooms")
                            .push().getKey();

                    //create the chatroom
                    Chatroom chatroom = new Chatroom();

                    chatroom.setChatroom_name(mChatroomName.getText().toString());
                    chatroom.setCreator_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    chatroom.setChatroom_id(chatroomId);


                    //insert the new chatroom into the database
                    reference
                            .child("chatrooms")
                            .child(chatroomId)
                            .setValue(chatroom);

                    //create a unique id for the message
                    String messageId = reference
                            .child("chatrooms")
                            .push().getKey();

                    //insert the first message into the chatroom
                    ChatMessage message = new ChatMessage();

                    message.setMessage("Welcome to the new chatroom!");
                    message.setTimestamp(getTimestamp());
                    reference
                            .child("chatrooms")
                            .child(chatroomId)
                            .child("chatroom_messages")
                            .child(messageId)
                            .setValue(message);
                    ((ChatActivity) getActivity()).init();
                    getDialog().dismiss();

                }

            }
        });
        return view;
    }


    /**
     * Return the current timestamp in the form of a string
     *
     * @return
     */
    private String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));
        return sdf.format(new Date());
    }
}
