package com.example.android.wechat;

import android.app.ProgressDialog;
import android.icu.text.DateFormat;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private TextView mProfileName,mProfileStatus,mProfileFriendsCount;
    private ImageView mProfileImage;
    private Button mProfileSendReqBttn,mProfileDeclineBttn;

    private DatabaseReference mUsersDatabase;
    private ProgressDialog mProgressDialog;
    private String mCurrent_status;
    private FirebaseUser mCurrent_user;

    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id=getIntent().getStringExtra("user_id");

        mProfileImage=(ImageView)findViewById(R.id.profile_image);
        mProfileName=(TextView)findViewById(R.id.profile_displayName);
        mProfileStatus=(TextView)findViewById(R.id.profile_status);
        mProfileFriendsCount=(TextView)findViewById(R.id.profile_totalFriends);
        mProfileSendReqBttn=(Button)findViewById(R.id.profile_send_req_bttn);
        mProfileDeclineBttn=(Button)findViewById(R.id.profile_decline_bttn);


        mCurrent_status="not_friends";
        mProgressDialog= new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait while we load the user data.");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mFriendDatabase=FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase=FirebaseDatabase.getInstance().getReference().child("Notifications");
        mFriendReqDatabase=FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mCurrent_user= FirebaseAuth.getInstance().getCurrentUser();
        mUsersDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    String display_name=dataSnapshot.child("name").getValue().toString();
                    String status=dataSnapshot.child("status").getValue().toString();
                    String image=dataSnapshot.child("image").getValue().toString();

                    mProfileName.setText(display_name);
                    mProfileStatus.setText(status);

                    Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_picture).into(mProfileImage);

                    mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(user_id))
                            {
                                String req_type=dataSnapshot.child(user_id).child("request_type").getValue().toString();
                                if(req_type.equals("received")){
                                    mCurrent_status="req_received";
                                    mProfileSendReqBttn.setText("Accept Friend Request");
                                    mProfileDeclineBttn.setVisibility(View.VISIBLE);
                                    mProfileDeclineBttn.setEnabled(true);
                                }else if(req_type.equals("sent")){
                                    mCurrent_status="req_sent";
                                    mProfileSendReqBttn.setText("Cancel Friend Request");
                                    mProfileDeclineBttn.setVisibility(View.INVISIBLE);
                                    mProfileDeclineBttn.setEnabled(false);
                                }
                                mProgressDialog.dismiss();
                            }else{
                                mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild(user_id))
                                        {
                                            mCurrent_status="friends";
                                            mProfileSendReqBttn.setText("Unfriend this Person");

                                            mProfileDeclineBttn.setVisibility(View.INVISIBLE);
                                            mProfileDeclineBttn.setEnabled(false);
                                        }
                                        mProgressDialog.dismiss();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        mProgressDialog.dismiss();
                                    }
                                });
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mProfileSendReqBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProfileSendReqBttn.setEnabled(false);
                if(mCurrent_status.equals("not_friends"))
                {
                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                {
                                    mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).child("request_type").setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            HashMap<String, String> notificationData=new HashMap<String, String>();
                                            notificationData.put("from",mCurrent_user.getUid());
                                            notificationData.put("type","request");

                                            mNotificationDatabase.child(user_id).push().setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mCurrent_status="req_sent";
                                                    mProfileSendReqBttn.setText("Cancel Friend Request");
                                                    Toast.makeText(ProfileActivity.this,"Friend request sent",Toast.LENGTH_LONG).show();

                                                    mProfileDeclineBttn.setVisibility(View.INVISIBLE);
                                                    mProfileDeclineBttn.setEnabled(false);
                                                }
                                            });

                                        }
                                    });
                                }
                                else {
                                    Toast.makeText(ProfileActivity.this,"Failed sending request",Toast.LENGTH_LONG).show();
                                }
                            mProfileSendReqBttn.setEnabled(true);
                        }
                    });
                }
                if(mCurrent_status.equals("req_sent"))
                {
                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                             mProfileSendReqBttn.setEnabled(true);
                                            mCurrent_status="not_friends";
                                            mProfileSendReqBttn.setText("Send Friend Request");
                                    mProfileDeclineBttn.setVisibility(View.INVISIBLE);
                                    mProfileDeclineBttn.setEnabled(false);
                                }
                            });
                            //Toast.makeText(ProfileActivity.this)
                        }
                    });
                }
                if(mCurrent_status.equals("req_received")){
                    final String currentDate= DateFormat.getDateTimeInstance().format(new Date());
                    mFriendDatabase.child(mCurrent_user.getUid()).child(user_id).setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(user_id).child(mCurrent_user.getUid()).setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mProfileSendReqBttn.setEnabled(true);
                                                    mCurrent_status="friends";
                                                    mProfileSendReqBttn.setText("Unfriend this person");
                                                    mProfileDeclineBttn.setVisibility(View.INVISIBLE);
                                                    mProfileDeclineBttn.setEnabled(false);
                                                }
                                            });
                                            //Toast.makeText(ProfileActivity.this)
                                        }
                                    });

                                }
                            });

                        }
                    });
                }
            }
        });

    }
}
