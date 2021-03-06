package www.catholix.com.ng;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import adapter.ChatAdapter;
import adapter.MyAdapter;
import config.ChatDatabase;
import config.FileConfig;
import config.GetTimeAgo;
import config.KeyboardUtils;
import config.SharedPref;
import de.hdodenhof.circleimageview.CircleImageView;
import model.ChatDao;
import model.ContactModel;
import view.ChatsView;

public class ChatActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, ChatsView {

    RecyclerView recyclerView;
    ImageButton btnSend, imgPickImage, imgAttach, backBtn, audioPick, btnCameraPick, btnVideoPick, btnDocumentPick, btnContactPick, popUpMenuBtn;
    EditText editText;
    ChatAdapter adapter;
    List<ChatDao> list;
    String userID, otherUser, username, otherUsername, imgUrl;
    SwipeRefreshLayout swipeRefreshLayout;
    ImageView imageView, chatBackImg;
    static final int DC_RESULT = 1, AUDIO_RESULT = 2, VIDEO_RESULT = 3, DOC_RESULT = 4, CONTACT_RESULT = 5;
    DatabaseReference rootRef;
    static final int TOTAL_ITEMS_TO_LOAD = 15;
    int mCurrentPage = 1;
    int itemPos = 0;
    int pos = 0;
    String mLastKey = "";
    String mPrevKey = "";
    StorageReference mImageStorage;
    private LinearLayoutManager mLinearLayout;
    TextView textUsername, textTimeStamp, textUserBlocked;
    private LinearLayout chooseFileLayout;
    private boolean isChooseFileVisible;
    Animation slideLeft, slideRight;
    CircleImageView userImage;
    private boolean isChat, isRecording;
    ProgressBar bar;
    MediaRecorder mediaRecorder;
    String outputFile, online;
    private File rootFile;
    String dir, fileName;
    boolean isBlock, isUserBlock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_chat);

            username = SharedPref.getInstance(this).getUser();
            userID = SharedPref.getInstance(this).getId();
            otherUser = getIntent().getStringExtra("userID");
            otherUsername = getIntent().getStringExtra("username");
            imgUrl = getIntent().getStringExtra("img_url");

            recyclerView = findViewById(R.id.activity_chat_recycler_view);
            btnSend = findViewById(R.id.activity_chat_btn_send);
            imgPickImage = findViewById(R.id.activity_chat_image_file_pick);
            audioPick = findViewById(R.id.activity_chat_audio_file_pick);
            imgAttach = findViewById(R.id.activity_chat_img_attach);
            editText = findViewById(R.id.activity_chat_edit_text);
            swipeRefreshLayout = findViewById(R.id.activity_chat_swipe_refresh);
            textUsername = findViewById(R.id.activity_chat_text_username);
            textTimeStamp = findViewById(R.id.activity_chat_text_time);
            chooseFileLayout = findViewById(R.id.activity_chat_card_choose_file_layout);
            userImage = findViewById(R.id.activity_chat_user_img);
            bar = findViewById(R.id.activity_chat_progress_file_upload);
            backBtn = findViewById(R.id.activity_chat_back_btn);
            btnCameraPick = findViewById(R.id.activity_chat_image_camera_pick);
            btnVideoPick = findViewById(R.id.activity_chat_video_file_pick);
            btnDocumentPick = findViewById(R.id.activity_chat_document_file_pick);
            btnContactPick = findViewById(R.id.activity_chat_contact_file_pick);
            chatBackImg = findViewById(R.id.activity_chat_img_back);
            popUpMenuBtn = findViewById(R.id.activity_chat_menu_options);
            textUserBlocked = findViewById(R.id.activity_chat_text_user_blocked);

            if (new ChatDatabase(this).findChatImage("1") == null)
                chatBackImg.setImageResource(R.drawable.chat_back_img);
            else
                chatBackImg.setImageBitmap(BitmapFactory.decodeByteArray(new ChatDatabase(this).findChatImage("1"),
                        0, new ChatDatabase(this).findChatImage("1").length));

            rootFile = Environment.getExternalStorageDirectory();

            Glide.with(this).load(imgUrl).placeholder(R.drawable.ic_person_profile_24dp).into(userImage);
            textUsername.setText(otherUsername);
            textTimeStamp.setText("");
            imageView = new ImageView(this);
            mLinearLayout = new LinearLayoutManager(this);
            swipeRefreshLayout.setOnRefreshListener(this);
            list = new ArrayList<>();
            adapter = new ChatAdapter(this, list, this);
            recyclerView.setLayoutManager(mLinearLayout);
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(adapter);
            rootRef = FirebaseDatabase.getInstance().getReference();
            mImageStorage = FirebaseStorage.getInstance().getReference();
            slideLeft = AnimationUtils.loadAnimation(this, R.anim.card_slide_left2);
            slideRight = AnimationUtils.loadAnimation(this, R.anim.slide_right2);
            outputFile = Environment.getExternalStorageDirectory().getAbsolutePath();
            online = "";

            rootRef.child("Chat").child(userID).child(otherUser).child("seen").setValue(true);

            rootRef.child("Users").child(otherUser).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child("online").getValue() != null) {
                        online = dataSnapshot.child("online").getValue().toString();
                        if (!TextUtils.isEmpty(online)) {
                            if (online.equals("true")) {
                                textTimeStamp.setText("Online");
                            } else {
                                long lastTime = Long.parseLong(online);
                                String lastSeenTime = "active " + GetTimeAgo.getTimeAgo(lastTime, getApplicationContext());
                                textTimeStamp.setText(lastSeenTime);
                            }
                        } else {
                            textTimeStamp.setText("");
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            slideRight.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    imgAttach.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            slideLeft.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    imgAttach.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            loadMessages();

            rootRef.child("Chat").child(userID).child(otherUser).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    String typing = dataSnapshot.child("typing").getValue(String.class);

                    if (dataSnapshot.child("is_block").exists())
                        isBlock = dataSnapshot.child("is_block").getValue(Boolean.class);
                    else
                        isBlock = false;

                    if (isBlock) {
                        editText.setEnabled(false);
                        editText.setAlpha(0.5f);
                        imgAttach.setEnabled(false);
                        btnSend.setEnabled(false);
                        btnSend.setAlpha(0.5f);
                        textUserBlocked.setText("You have been blocked by this user");
                        textUserBlocked.setVisibility(View.VISIBLE);
                    } else {
                        editText.setEnabled(true);
                        editText.setAlpha(1f);
                        imgAttach.setEnabled(true);
                        btnSend.setEnabled(true);
                        btnSend.setAlpha(1f);
                        textUserBlocked.setVisibility(View.INVISIBLE);
                    }

                    if (!TextUtils.isEmpty(typing))
                        if (typing.equals("true")) {

                            textTimeStamp.setText("typing...");

                        } else {
                            if (!TextUtils.isEmpty(online))
                                textTimeStamp.setText((online.equals("true") ? "Online" : "active " + GetTimeAgo.getTimeAgo(Long.parseLong(online), getApplicationContext())));
                            else
                                textTimeStamp.setText("");
                        }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            rootRef.child("Chat").child(otherUser).child(userID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child("is_block").exists())
                    isUserBlock = dataSnapshot.child("is_block").getValue(Boolean.class);
                    else
                        isUserBlock = false;

                    /*if (isUserBlock) {
                        editText.setEnabled(false);
                        editText.setAlpha(0.5f);
                        imgAttach.setEnabled(false);
                        btnSend.setEnabled(false);
                        btnSend.setAlpha(0.5f);
                        textUserBlocked.setText("You block this user");
                        textUserBlocked.setVisibility(View.VISIBLE);
                    } else {
                        editText.setEnabled(true);
                        editText.setAlpha(1f);
                        imgAttach.setEnabled(true);
                        btnSend.setEnabled(true);
                        btnSend.setAlpha(1f);
                        textUserBlocked.setVisibility(View.INVISIBLE);
                    }*/
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            rootRef.child("Chat").child(userID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (!dataSnapshot.hasChild(otherUser)) {

                        Map chatAddMap = new HashMap();
                        chatAddMap.put("seen", false);
                        chatAddMap.put("time_stamp", ServerValue.TIMESTAMP);

                        Map chatUserMap = new HashMap();
                        chatUserMap.put("Chat/" + userID + "/" + otherUser, chatAddMap);
                        chatUserMap.put("Chat/" + otherUser + "/" + userID, chatAddMap);

                        rootRef.updateChildren(chatUserMap, (databaseError, databaseReference) -> {

                        });

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        try {

            KeyboardUtils.addKeyboardToggleListener(this, isVisible -> {
                if (isVisible) {
                    imgAttach.startAnimation(slideRight);
                    rootRef.child("Chat").child(otherUser).child(userID).child("typing").setValue("true");
                } else {
                    imgAttach.startAnimation(slideLeft);
                    rootRef.child("Chat").child(otherUser).child(userID).child("typing").setValue("false");
                }
            });

            btnSend.setOnClickListener(view -> {
                if (isChat) {
                    if (!TextUtils.isEmpty(editText.getText().toString())) {
                        sendMessage(editText.getText().toString());
                    }
                } else {
                    if (isRecording) {
                        try {
                            isRecording = false;
                            mediaRecorder.stop();
                            mediaRecorder.release();
                            mediaRecorder = null;
                            btnSend.setImageResource(R.drawable.ic_keyboard_voice_white_24dp);
                            sendAudio(Uri.fromFile(new File(dir)));
                        } catch (Exception e) {
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        startRecording();
                    }
                }
            });

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (TextUtils.isEmpty(editText.getText().toString())) {
                        isChat = false;
                        btnSend.setImageResource(R.drawable.ic_keyboard_voice_white_24dp);
                    } else {
                        isChat = true;
                        btnSend.setImageResource(R.drawable.ic_send_white_24dp);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
            imgAttach.setOnClickListener(view -> {
                if (isChooseFileVisible) {
                    chooseFileLayout.setVisibility(View.GONE);
                    isChooseFileVisible = false;
                } else {
                    chooseFileLayout.setVisibility(View.VISIBLE);
                    isChooseFileVisible = true;
                }
            });

            backBtn.setOnClickListener(view -> onBackPressed());

            imgPickImage.setOnClickListener(view -> {
                chooseFileLayout.setVisibility(View.GONE);
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "SELECT IMAGE"), DC_RESULT);
            });

            audioPick.setOnClickListener(view -> {
                chooseFileLayout.setVisibility(View.GONE);
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/*");
                startActivityForResult(intent, AUDIO_RESULT);
            });

            btnCameraPick.setOnClickListener(view -> {
                chooseFileLayout.setVisibility(View.GONE);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, DC_RESULT);
            });

            btnVideoPick.setOnClickListener(view -> {
                chooseFileLayout.setVisibility(View.GONE);
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*");
                startActivityForResult(intent, VIDEO_RESULT);
            });

            btnDocumentPick.setOnClickListener(view -> {
                chooseFileLayout.setVisibility(View.GONE);
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, DOC_RESULT);
            });

            btnContactPick.setOnClickListener(view -> {
                chooseFileLayout.setVisibility(View.GONE);
                final List<ContactModel> contactsList = getContacts();
                final Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.contacts_list_view);
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(true);
                dialog.setTitle("Select Contact");
                ListView listView = dialog.findViewById(R.id.contacts_list_view);
                MyAdapter adapter = new MyAdapter(this, contactsList);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener((adapterView, view1, i, l) -> {
                    ContactModel model = contactsList.get(i);
                    dialog.dismiss();
                    sendContact(model);
                });
                dialog.show();
            });

            popUpMenuBtn.setOnClickListener(view -> {
                CharSequence options[] = new CharSequence[]{isUserBlock ? "Unblock User" : "Block User", "Clear Chats", "Delete Conversations"};
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("Select Options");
                builder.setItems(options, ((dialogInterface, i) -> {
                    switch (i) {
                        case 0:
                            blockUser();
                            break;
                        case 1:
                            clearChats();
                            break;
                        case 2:
                            deleteConv();
                            break;
                        default:
                            break;
                    }
                }
                ));
                builder.show();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteConv() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Delete Conversations")
                .setMessage("All your conversations including chat history will be deleted")
                .setNegativeButton("Cancel", ((dialogInterface, i) ->
                        dialogInterface.dismiss()))
                .setPositiveButton("OK", ((dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    ProgressDialog dialog = new ProgressDialog(this);
                    dialog.setTitle("Deleting Conversations...");
                    dialog.setMessage("please wait");
                    dialog.setCancelable(false);
                    dialog.show();
                    rootRef.child("messages").child(userID).child(otherUser).removeValue().addOnCompleteListener((task -> {
                        if (task.isSuccessful()) {
                            finish();
                            dialog.dismiss();
                            Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                        }
                    }));
                }));
        builder.create().show();
    }

    private void clearChats() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Clear Chats")
                .setMessage("All your conversations will be cleared")
                .setNegativeButton("Cancel", ((dialogInterface, i) ->
                        dialogInterface.dismiss()))
                .setPositiveButton("OK", ((dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    ProgressDialog dialog = new ProgressDialog(this);
                    dialog.setTitle("Clearing Chats...");
                    dialog.setMessage("please wait");
                    dialog.setCancelable(false);
                    dialog.show();
                    rootRef.child("messages").child(userID).child(otherUser).removeValue().addOnCompleteListener((task -> {
                        if (task.isSuccessful()) {
                            loadMessages();
                            dialog.dismiss();
                            Toast.makeText(this, "Chats cleared successfully", Toast.LENGTH_SHORT).show();
                        }
                    }));
                }));
        builder.create().show();
    }

    private void blockUser() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(isUserBlock ? "Unblock User?" : "Block User?")
                .setMessage("Sure to continue?")
                .setNegativeButton("Cancel", ((dialogInterface, i) ->
                        dialogInterface.dismiss()))
                .setPositiveButton("Sure", ((dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    ProgressDialog dialog = new ProgressDialog(this);
                    dialog.setTitle(isUserBlock ? "Unblocking..." : "Blocking...");
                    dialog.setMessage("please wait");
                    dialog.setCancelable(false);
                    dialog.show();
                    rootRef.child("Chat").child(otherUser).child(userID).child("is_block").setValue(!isUserBlock).addOnCompleteListener((task -> {
                        if (task.isSuccessful()) {
                            dialog.dismiss();
                            Toast.makeText(this, isUserBlock ? "User unblocked successfully" : "User blocked successfully", Toast.LENGTH_SHORT).show();
                        }
                    }));
                }));
        builder.create().show();
    }

    private void sendContact(ContactModel model) {
        String current_user_ref = "messages/" + userID + "/" + otherUser;
        String chat_user_ref = "messages/" + otherUser + "/" + userID;

        DatabaseReference user_message_push = rootRef.child("messages")
                .child(userID).child(otherUser).push();

        final String push_id = user_message_push.getKey();

        Map messageMap = new HashMap();
        messageMap.put("msg_body", model.mobileNumber);
        messageMap.put("key", push_id);
        messageMap.put("seen", false);
        messageMap.put("isRead", false);
        messageMap.put("msg_type", "contact");
        messageMap.put("time_stamp", ServerValue.TIMESTAMP);
        messageMap.put("from", userID);
        messageMap.put("to", otherUser);
        messageMap.put("msg_name", model.name);
        messageMap.put("fromUsername", username);
        messageMap.put("toUsername", otherUsername);

        Map messageUserMap = new HashMap();
        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

        editText.setText("");

        rootRef.child("Chat").child(userID).child(otherUser).child("seen").setValue(true);

        rootRef.child("Chat").child(userID).child(otherUser).child("time_stamp").setValue(ServerValue.TIMESTAMP);

        rootRef.child("Chat").child(otherUser).child(userID).child("seen").setValue(false);
        rootRef.child("Chat").child(otherUser).child(userID).child("time_stamp").setValue(ServerValue.TIMESTAMP);


        rootRef.updateChildren(messageUserMap, (databaseError, databaseReference) -> {

            if (databaseError == null) {

                rootRef.child("messages").child(userID).child(otherUser).child(push_id).child("seen").setValue(true);
                rootRef.child("messages").child(userID).child(otherUser).child(push_id).child("isRead").setValue(true);

            }

        });

    }

    public List<ContactModel> getContacts() {
        List<ContactModel> list = new ArrayList<>();
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            ContactModel model = new ContactModel();
            model.mobileNumber = phoneNumber;
            model.name = name;
            list.add(model);
        }
        phones.close();
        return list;
    }

    private void sendAudio(Uri uri) {
        bar.setVisibility(View.VISIBLE);
        Toast.makeText(this, "sending audio...", Toast.LENGTH_SHORT).show();
        final String current_user_ref = "messages/" + userID + "/" + otherUser;
        final String chat_user_ref = "messages/" + otherUser + "/" + userID;

        DatabaseReference user_message_push = rootRef.child("messages")
                .child(userID).child(otherUser).push();

        final String push_id = user_message_push.getKey();
        final StorageReference thumbRef = mImageStorage.child("message_audio").child(fileName);
        UploadTask uploadTask = thumbRef.putFile(uri);

        uploadTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                thumbRef.getDownloadUrl().addOnSuccessListener(uri1 -> {
                    String thumb_url = uri1.toString();
                    Map messageMap = new HashMap();
                    messageMap.put("msg_body", thumb_url);
                    messageMap.put("key", push_id);
                    messageMap.put("seen", false);
                    messageMap.put("msg_type", "audio");
                    messageMap.put("time_stamp", ServerValue.TIMESTAMP);
                    messageMap.put("from", userID);
                    messageMap.put("to", otherUser);
                    messageMap.put("msg_name", fileName);
                    messageMap.put("fromUsername", username);
                    messageMap.put("toUsername", otherUsername);


                    Map messageUserMap = new HashMap();
                    messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                    messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                    editText.setText("");

                    rootRef.child("Chat").child(userID).child(otherUser).child("seen").setValue(true);

                    rootRef.child("Chat").child(userID).child(otherUser).child("time_stamp").setValue(ServerValue.TIMESTAMP);

                    rootRef.child("Chat").child(otherUser).child(userID).child("seen").setValue(false);
                    rootRef.child("Chat").child(otherUser).child(userID).child("time_stamp").setValue(ServerValue.TIMESTAMP);

                    rootRef.updateChildren(messageUserMap, (databaseError, databaseReference) -> {

                        if (databaseError == null) {
                            bar.setVisibility(View.INVISIBLE);
                            Toast.makeText(ChatActivity.this, "uploaded successfully", Toast.LENGTH_SHORT).show();
                        }

                    });
                });
            } else {
                bar.setVisibility(View.INVISIBLE);
                Toast.makeText(ChatActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    private void sendVideo(Uri uri) {
        bar.setVisibility(View.VISIBLE);
        Toast.makeText(this, "sending video...", Toast.LENGTH_SHORT).show();
        final String current_user_ref = "messages/" + userID + "/" + otherUser;
        final String chat_user_ref = "messages/" + otherUser + "/" + userID;

        DatabaseReference user_message_push = rootRef.child("messages")
                .child(userID).child(otherUser).push();

        final String push_id = user_message_push.getKey();
        final StorageReference thumbRef = mImageStorage.child("message_video").child(fileName);
        UploadTask uploadTask = thumbRef.putFile(uri);

        uploadTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                thumbRef.getDownloadUrl().addOnSuccessListener(uri1 -> {
                    String thumb_url = uri1.toString();
                    Map messageMap = new HashMap();
                    messageMap.put("msg_body", thumb_url);
                    messageMap.put("key", push_id);
                    messageMap.put("seen", false);
                    messageMap.put("msg_type", "video");
                    messageMap.put("time_stamp", ServerValue.TIMESTAMP);
                    messageMap.put("from", userID);
                    messageMap.put("to", otherUser);
                    messageMap.put("msg_name", fileName);
                    messageMap.put("fromUsername", username);
                    messageMap.put("toUsername", otherUsername);


                    Map messageUserMap = new HashMap();
                    messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                    messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                    editText.setText("");

                    rootRef.child("Chat").child(userID).child(otherUser).child("seen").setValue(true);

                    rootRef.child("Chat").child(userID).child(otherUser).child("time_stamp").setValue(ServerValue.TIMESTAMP);

                    rootRef.child("Chat").child(otherUser).child(userID).child("seen").setValue(false);
                    rootRef.child("Chat").child(otherUser).child(userID).child("time_stamp").setValue(ServerValue.TIMESTAMP);

                    rootRef.updateChildren(messageUserMap, (databaseError, databaseReference) -> {

                        if (databaseError == null) {
                            bar.setVisibility(View.INVISIBLE);
                            Toast.makeText(ChatActivity.this, "uploaded successfully", Toast.LENGTH_SHORT).show();
                        }

                    });
                });
            } else {
                bar.setVisibility(View.INVISIBLE);
                Toast.makeText(ChatActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    private void startRecording() {
        isRecording = true;
        btnSend.setImageResource(R.drawable.ic_pause_white_24dp);
        Toast.makeText(this, "recording started", Toast.LENGTH_SHORT).show();
        try {
            String time = Long.toString(System.currentTimeMillis());
            dir = rootFile.getAbsolutePath() + "/Catholix/" + time + ".3gp";
            fileName = time + ".3gp";
            File file = new File(rootFile.getAbsolutePath() + "/Catholix");
            if (!file.exists())
                file.mkdir();
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
            mediaRecorder.setOutputFile(dir);
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendMessage(String message) {

        if (!TextUtils.isEmpty(message)) {

            String current_user_ref = "messages/" + userID + "/" + otherUser;
            String chat_user_ref = "messages/" + otherUser + "/" + userID;

            DatabaseReference user_message_push = rootRef.child("messages")
                    .child(userID).child(otherUser).push();

            final String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("msg_body", message);
            messageMap.put("key", push_id);
            messageMap.put("seen", false);
            messageMap.put("isRead", false);
            messageMap.put("msg_type", "text");
            messageMap.put("time_stamp", ServerValue.TIMESTAMP);
            messageMap.put("from", userID);
            messageMap.put("to", otherUser);
            messageMap.put("msg_name", message);
            messageMap.put("fromUsername", username);
            messageMap.put("toUsername", otherUsername);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            editText.setText("");

            rootRef.child("Chat").child(userID).child(otherUser).child("seen").setValue(true);

            rootRef.child("Chat").child(userID).child(otherUser).child("time_stamp").setValue(ServerValue.TIMESTAMP);

            rootRef.child("Chat").child(otherUser).child(userID).child("seen").setValue(false);
            rootRef.child("Chat").child(otherUser).child(userID).child("time_stamp").setValue(ServerValue.TIMESTAMP);


            rootRef.updateChildren(messageUserMap, (databaseError, databaseReference) -> {

                if (databaseError == null) {

                    rootRef.child("messages").child(userID).child(otherUser).child(push_id).child("seen").setValue(true);
                    rootRef.child("messages").child(userID).child(otherUser).child(push_id).child("isRead").setValue(true);

                }

            });

        }
    }

    private void loadMessages() {
        swipeRefreshLayout.setRefreshing(true);
        DatabaseReference chatRef = rootRef.child("messages").child(userID).child(otherUser);
        Query messageQuery = chatRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);
        list.clear();
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.hasChildren()) {
                    ChatDao message = dataSnapshot.getValue(ChatDao.class);

                    if (pos == 0 && message.getSeen())
                        pos = itemPos;

                    rootRef.child("messages").child(userID).child(otherUser).child(dataSnapshot.getKey()).child("seen").setValue(true);
                    rootRef.child("messages").child(userID).child(otherUser).child(dataSnapshot.getKey()).child("isRead").setValue(true);
                    itemPos++;

                    if (itemPos == 1) {

                        String messageKey = dataSnapshot.getKey();

                        mLastKey = messageKey;
                        mPrevKey = messageKey;

                    }

                    list.add(message);
                    adapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                    swipeRefreshLayout.setRefreshing(false);
                } else
                    swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == DC_RESULT && resultCode == RESULT_OK) {

            try {
                Uri imageUri = data.getData();
                final ImageView imgContain = new ImageView(this);
                imgContain.setImageURI(imageUri);
                Bitmap bitmap = ((BitmapDrawable) imgContain.getDrawable()).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                final byte[] thumb_byte = stream.toByteArray();
                doUpload(thumb_byte);
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == AUDIO_RESULT && resultCode == RESULT_OK) {
            Uri audioUri = data.getData();
            if (audioUri != null) {
                dir = audioUri.getPath();
                File file = new File(getFilePath(audioUri, new String[]{MediaStore.Audio.Media.DATA}));
                fileName = file.getName();
                if (getFileSize(audioUri, new String[]{OpenableColumns.SIZE}) > 5)
                    Toast.makeText(this, "you cannot send audio file greater than 5mb", Toast.LENGTH_SHORT).show();
                else {
                    String dirDest = rootFile.getAbsolutePath() + "/Catholix/";
                    try {
                        saveFile(file, new File(dirDest));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    sendAudio(audioUri);
                    // Toast.makeText(this, fileName, Toast.LENGTH_LONG).show();
                }
            }
        } else if (requestCode == VIDEO_RESULT && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            if (videoUri != null) {
                dir = videoUri.getPath();
                File file = new File(getFilePath(videoUri, new String[]{MediaStore.Video.Media.DATA}));
                fileName = file.getName();
                if (getFileSize(videoUri, new String[]{OpenableColumns.SIZE}) > 5)
                    Toast.makeText(this, "you cannot send video file greater than 5mb", Toast.LENGTH_SHORT).show();
                else {
                    String dirDest = rootFile.getAbsolutePath() + "/Catholix/";
                    try {
                        saveFile(file, new File(dirDest));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    sendVideo(videoUri);
                }
            }
        } else if (requestCode == DOC_RESULT && resultCode == RESULT_OK) {
            Uri docUri = data.getData();
            if (docUri != null) {
                dir = docUri.getPath();
                File file = new File(getFilePath(docUri, new String[]{MediaStore.Files.FileColumns.DATA}));
                fileName = file.getName();
                if (getFileSize(docUri, new String[]{OpenableColumns.SIZE}) > 5)
                    Toast.makeText(this, "you cannot send a file greater than 5mb", Toast.LENGTH_SHORT).show();
                else {
                    String dirDest = rootFile.getAbsolutePath() + "/Catholix/";
                    try {
                        saveFile(file, new File(dirDest));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    sendFile(docUri);
                }
            }
        }

    }

    private void sendFile(Uri uri) {
        bar.setVisibility(View.VISIBLE);
        Toast.makeText(this, "sending file...", Toast.LENGTH_SHORT).show();
        final String current_user_ref = "messages/" + userID + "/" + otherUser;
        final String chat_user_ref = "messages/" + otherUser + "/" + userID;

        DatabaseReference user_message_push = rootRef.child("messages")
                .child(userID).child(otherUser).push();

        final String push_id = user_message_push.getKey();
        final StorageReference thumbRef = mImageStorage.child("message_files").child(fileName);
        UploadTask uploadTask = thumbRef.putFile(uri);

        uploadTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                thumbRef.getDownloadUrl().addOnSuccessListener(uri1 -> {
                    String thumb_url = uri1.toString();
                    Map messageMap = new HashMap();
                    messageMap.put("msg_body", thumb_url);
                    messageMap.put("key", push_id);
                    messageMap.put("seen", false);
                    messageMap.put("msg_type", "file");
                    messageMap.put("time_stamp", ServerValue.TIMESTAMP);
                    messageMap.put("from", userID);
                    messageMap.put("to", otherUser);
                    messageMap.put("msg_name", fileName);
                    messageMap.put("fromUsername", username);
                    messageMap.put("toUsername", otherUsername);


                    Map messageUserMap = new HashMap();
                    messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                    messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                    editText.setText("");

                    rootRef.child("Chat").child(userID).child(otherUser).child("seen").setValue(true);

                    rootRef.child("Chat").child(userID).child(otherUser).child("time_stamp").setValue(ServerValue.TIMESTAMP);

                    rootRef.child("Chat").child(otherUser).child(userID).child("seen").setValue(false);
                    rootRef.child("Chat").child(otherUser).child(userID).child("time_stamp").setValue(ServerValue.TIMESTAMP);

                    rootRef.updateChildren(messageUserMap, (databaseError, databaseReference) -> {

                        if (databaseError == null) {
                            bar.setVisibility(View.INVISIBLE);
                            Toast.makeText(ChatActivity.this, "uploaded successfully", Toast.LENGTH_SHORT).show();
                        }

                    });
                });
            } else {
                bar.setVisibility(View.INVISIBLE);
                Toast.makeText(ChatActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveFile(File src, File dst) throws IOException {
        FileUtils.copyFileToDirectory(src, dst);
    }

    private String getFilePath(Uri uri, String[] filePathColumn) {
        Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        return picturePath;
    }

    private long getFileSize(Uri uri, String[] filePathColumn) {
        Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int size = cursor.getColumnIndex(filePathColumn[0]);
        long bytes = cursor.getLong(size);
        cursor.close();
        long kilo = bytes / 1024;
        long mega = kilo / 1024;
        return mega;
    }

    private void doUpload(byte[] thumb_byte) {
        bar.setVisibility(View.VISIBLE);
        Toast.makeText(this, "uploading....", Toast.LENGTH_LONG).show();
        final String current_user_ref = "messages/" + userID + "/" + otherUser;
        final String chat_user_ref = "messages/" + otherUser + "/" + userID;

        DatabaseReference user_message_push = rootRef.child("messages")
                .child(userID).child(otherUser).push();

        final String push_id = user_message_push.getKey();
        final StorageReference thumbRef = mImageStorage.child("message_images").child(push_id + ".jpg");
        final UploadTask uploadTask = thumbRef.putBytes(thumb_byte);

        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    thumbRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String thumb_url = uri.toString();
                            Map messageMap = new HashMap();
                            messageMap.put("msg_body", thumb_url);
                            messageMap.put("key", push_id);
                            messageMap.put("seen", false);
                            messageMap.put("msg_type", "image");
                            messageMap.put("time_stamp", ServerValue.TIMESTAMP);
                            messageMap.put("from", userID);
                            messageMap.put("to", otherUser);
                            messageMap.put("msg_name", push_id + ".jpg");
                            messageMap.put("fromUsername", username);
                            messageMap.put("toUsername", otherUsername);


                            Map messageUserMap = new HashMap();
                            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                            editText.setText("");

                            rootRef.child("Chat").child(userID).child(otherUser).child("seen").setValue(true);

                            rootRef.child("Chat").child(userID).child(otherUser).child("time_stamp").setValue(ServerValue.TIMESTAMP);

                            rootRef.child("Chat").child(otherUser).child(userID).child("seen").setValue(false);
                            rootRef.child("Chat").child(otherUser).child(userID).child("time_stamp").setValue(ServerValue.TIMESTAMP);

                            rootRef.updateChildren(messageUserMap, (databaseError, databaseReference) -> {

                                if (databaseError == null) {
                                    bar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(ChatActivity.this, "uploaded successfully", Toast.LENGTH_SHORT).show();

                                }

                            });
                        }
                    });
                } else {
                    bar.setVisibility(View.INVISIBLE);
                    Toast.makeText(ChatActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onRefresh() {

        mCurrentPage++;
        itemPos = 0;
        loadMoreMessages();
    }

    private void loadMoreMessages() {

        DatabaseReference messageRef = rootRef.child("messages").child(userID).child(otherUser);

        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(15);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.hasChildren()) {
                    rootRef.child("messages").child(userID).child(otherUser).child(dataSnapshot.getKey()).child("seen").setValue(true);
                    rootRef.child("messages").child(userID).child(otherUser).child(dataSnapshot.getKey()).child("isRead").setValue(true);
                    ChatDao message = dataSnapshot.getValue(ChatDao.class);
                    String messageKey = dataSnapshot.getKey();

                    if (!mPrevKey.equals(messageKey)) {

                        list.add(itemPos++, message);

                    } else {

                        mPrevKey = mLastKey;

                    }


                    if (itemPos == 1) {

                        mLastKey = messageKey;

                    }

                    adapter.notifyDataSetChanged();

                    swipeRefreshLayout.setRefreshing(false);

                    mLinearLayout.scrollToPositionWithOffset(10, 0);
                } else
                    swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public String getUserPhoto() {
        return imgUrl;
    }

    @Override
    public String getUsername() {
        return otherUsername;
    }

    @Override
    public void startProgress(int view) {
        bar.setVisibility(view);
    }

    @Override
    public void reload() {
        loadMessages();
    }

    @Override
    protected void onPause() {
        super.onPause();
        rootRef.child("Chat").child(otherUser).child(userID).child("typing").setValue("false");
    }

}

