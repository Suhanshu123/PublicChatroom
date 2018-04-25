package com.example.android.authfirebase;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.authfirebase.models.ChatMessage;
import com.example.android.authfirebase.models.Chatroom;
import com.example.android.authfirebase.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ChatRoomActivity extends AppCompatActivity {

    private static final String TAG = "ChatroomActivity";

    private TextView mChatroomName;
    private ListView mListView;
    private EditText mMessage;
    private ImageView mCheckmark;

    private ChatMessageListAdapter mAdapter;

    private Chatroom mChatroom;
    private List<ChatMessage> mMessagesList;

    private DatabaseReference mMessagesReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);


        mChatroomName = (TextView) findViewById(R.id.text_chatroom_name);
        mListView = (ListView) findViewById(R.id.listView);
        mMessage = (EditText) findViewById(R.id.input_message);
        mCheckmark = (ImageView) findViewById(R.id.checkmark);

        mMessagesList = new ArrayList<>();

        getChatroom();

        init();

    }

    private void getChatroom() {

        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.intent_chatroom))) {
            Chatroom chatroom = intent.getParcelableExtra(getString(R.string.intent_chatroom));
            Log.d(TAG, "getChatroom: chatroom: " + chatroom.toString());
            mChatroom = chatroom;
            mChatroomName.setText(mChatroom.getChatroom_name());
            enableChatroomListener();

        }
    }

    ValueEventListener mValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            getChatroomMessages();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void enableChatroomListener() {
         /*
            ---------- Listener that will watch the 'chatroom_messages' node ----------
         */
        mMessagesReference = FirebaseDatabase.getInstance().getReference().child("chatrooms")
                .child(mChatroom.getChatroom_id())
                .child(getString(R.string.field_chatroom_messages));

        mMessagesReference.addValueEventListener(mValueEventListener);
    }


    private void getChatroomMessages() {
        mMessagesList = new ArrayList<>();
        if (mMessagesList.size() > 0) {
            mMessagesList.clear();
            mAdapter.clear();
        }
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child("chatrooms")
                .child(mChatroom.getChatroom_id())
                .child(getString(R.string.field_chatroom_messages));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    DataSnapshot snapshot = singleSnapshot;


                    try {//need to catch null pointer here because the initial welcome message to the
                        //chatroom has no user id
                        ChatMessage message = new ChatMessage();
                        String userId = snapshot.getValue(ChatMessage.class).getUser_id();
                        if (userId != null) { //check and make sure it's not the first message (has no user id)
                            message.setMessage(snapshot.getValue(ChatMessage.class).getMessage());
                            message.setUser_id(snapshot.getValue(ChatMessage.class).getUser_id());
                            message.setTimestamp(snapshot.getValue(ChatMessage.class).getTimestamp());
                            mMessagesList.add(message);
                        } else {
                            message.setMessage(snapshot.getValue(ChatMessage.class).getMessage());
                            message.setTimestamp(snapshot.getValue(ChatMessage.class).getTimestamp());
                            mMessagesList.add(message);
                        }

                    } catch (NullPointerException e) {
                        Log.e(TAG, "onDataChange: NullPointerException: " + e.getMessage());
                    }
                }
                //query the users node to get the profile images and names
                getUserDetails();
                initMessagesList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void initMessagesList() {
        mAdapter = new ChatMessageListAdapter(ChatRoomActivity.this,
                R.layout.layout_chatmessage_listitem, mMessagesList);
        mListView.setAdapter(mAdapter);
        mListView.setSelection(mAdapter.getCount() - 1); //scroll to the bottom of the list
    }


    private void getUserDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        for (int i = 0; i < mMessagesList.size(); i++) {
            Log.d(TAG, "onDataChange: searching for userId: " + mMessagesList.get(i).getUser_id());
            final int j = i;
            if (mMessagesList.get(i).getUser_id() != null) {
                Query query = reference.child("users")
                        .orderByKey()
                        .equalTo(mMessagesList.get(i).getUser_id());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                        Log.d(TAG, "onDataChange: found user id: "
                                + singleSnapshot.getValue(User.class).getUser_id());
                        mMessagesList.get(j).setProfile_image(singleSnapshot.getValue(User.class).getProfile_image());
                        mMessagesList.get(j).setName(singleSnapshot.getValue(User.class).getName());
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }

    }


    private void init() {


        mCheckmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!mMessage.getText().toString().equals("")) {
                    String message = mMessage.getText().toString();
                    Log.d(TAG, "onClick: sending new message: " + message);

                    //create the new message object for inserting
                    ChatMessage newMessage = new ChatMessage();
                    newMessage.setMessage(message);
                    newMessage.setTimestamp(getTimestamp());
                    newMessage.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

                    //get a database reference
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                            .child("chatrooms")
                            .child(mChatroom.getChatroom_id())
                            .child(getString(R.string.field_chatroom_messages));

                    //create the new messages id
                    String newMessageId = reference.push().getKey();

                    //insert the new message into the chatroom
                    reference
                            .child(newMessageId)
                            .setValue(newMessage);

                    //clear the EditText
                    mMessage.setText("");

                    //refresh the messages list? Or is it done by the listener??
                }

            }
        });
    }






    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMessagesReference.removeEventListener(mValueEventListener);
    }

    private String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));
        return sdf.format(new Date());
    }

}
