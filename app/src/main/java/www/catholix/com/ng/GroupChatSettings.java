package www.catholix.com.ng;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Service.UserService;
import adapter.MembersListAdapter;
import api.RetrofitClient;
import config.SharedPref;
import model.Users;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import view.GroupChatSettingsView;

public class GroupChatSettings extends AppCompatActivity implements GroupChatSettingsView {

    Toolbar toolbar;
    String img_url, username, chat_id, admin, userId;
    ImageView profileImgView;
    ImageButton imgUpload, btnAddMember;
    private Uri resultUri;
    DatabaseReference rootRef;
    FirebaseDatabase firebaseDatabase;
    ProgressBar bar;
    RecyclerView recyclerView;
    MembersListAdapter adapter;
    List<Users> list;
    List<String> userIDs;
    ProgressBar membersBar;
    UserService service;
    CardView layoutLeave;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_group_chat_settings);

        toolbar = findViewById(R.id.activity_group_settings_toolbar);
        setSupportActionBar(toolbar);
        img_url = getIntent().getStringExtra("img_url");
        username = getIntent().getStringExtra("username");
        chat_id = getIntent().getStringExtra("chat_id");
        profileImgView = findViewById(R.id.activity_group_settings_profile_img_view);
        imgUpload = findViewById(R.id.activity_group_settings_img_btn_upload);
        bar = findViewById(R.id.activity_group_settings_progress_bar_img);
        membersBar = findViewById(R.id.activity_group_settings_users_list_bar);
        recyclerView = findViewById(R.id.activity_group_settings_users_recycler_lists);
        layoutLeave = findViewById(R.id.activity_group_settings_leave_group_layout);
        btnAddMember = findViewById(R.id.activity_group_settings_img_btn_add_member);

        dialog = new ProgressDialog(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        list = new ArrayList<>();
        userIDs = new ArrayList<>();
        adapter = new MembersListAdapter(list, this, this);
        recyclerView.setAdapter(adapter);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        supportPostponeEnterTransition();
        getSupportActionBar().setTitle(username);

        resultUri = null;
        admin = "";
        userId = SharedPref.getInstance(this).getId();

        firebaseDatabase = FirebaseDatabase.getInstance();
        rootRef = firebaseDatabase.getReference();

        imgUpload.setOnClickListener(view -> {
            /*Intent galleryIntent = new Intent();
            galleryIntent.setType("image/*");
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);*/

                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .setAutoZoomEnabled(true)
                        .setMinCropWindowSize(500, 500)
                        .start(this);

        });

        btnAddMember.setOnClickListener(view -> {
            if(isInternetAvailable(this))
            startActivity(new Intent(this, AddNewMember.class).putExtra("id", chat_id).putExtra("username", username)
            .putExtra("userIDs", userIDs.toArray(new String[0])));
            else
                Toast.makeText(this, "no internet", Toast.LENGTH_SHORT).show();
        });

        try {
            Glide.with(this)
                    .load(img_url)
                    .dontAnimate()
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            supportStartPostponedEnterTransition();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            supportStartPostponedEnterTransition();
                            return false;
                        }
                    })
                    .into(profileImgView);
        } catch (Exception e) {
            e.printStackTrace();
        }

        getMembers();

        layoutLeave.setOnClickListener(view -> {
            confirmLeaveDialog();
        });
    }

    private void confirmLeaveDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Leave Group")
                .setMessage("You won't be able send or receive messages in this group again, until the admin adds you back.")
                .setCancelable(true)
                .setNegativeButton("Cancel", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                })
                .setPositiveButton("OK", ((dialogInterface, i) ->{
                    leaveGroup();
                })).create()
                .show();
    }

    private void leaveGroup() {
        dialog.setTitle("Leaving...");
        dialog.setMessage("Please wait");
        dialog.setCancelable(false);
        dialog.show();

        userIDs.remove(userId);

        rootRef.child("Users").child(chat_id).child("admin").setValue(admin.equals(userId)?userIDs.get(0):admin);
        rootRef.child("Users").child(chat_id).child("members").setValue(Arrays.asList(userIDs.toArray()));

        String current_user_ref = "messages/" + userId + "/" + chat_id;
        String chat_user_ref = "messages/" + chat_id;

        DatabaseReference user_message_push = rootRef.child("messages")
                .child(userId).child(chat_id).push();

        final String push_id = user_message_push.getKey();

        Map messageMap = new HashMap();
        messageMap.put("msg_body", " left the group");
        messageMap.put("key", push_id);
        messageMap.put("seen", false);
        messageMap.put("isRead", false);
        messageMap.put("msg_type", "left");
        messageMap.put("time_stamp", ServerValue.TIMESTAMP);
        messageMap.put("from", userId);
        messageMap.put("to", chat_id);
        messageMap.put("msg_name", "You left");
        messageMap.put("fromUsername", SharedPref.getInstance(this).getUser());
        messageMap.put("toUsername", chat_id);

        Map messageUserMap = new HashMap();
        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

        rootRef.child("Chat").child(userId).child(chat_id).child("seen").setValue(true);

        rootRef.child("Chat").child(userId).child(chat_id).child("time_stamp").setValue(ServerValue.TIMESTAMP);

        rootRef.child("Chat").child(chat_id).child(userId).child("seen").setValue(false);
        rootRef.child("Chat").child(chat_id).child(userId).child("time_stamp").setValue(ServerValue.TIMESTAMP);


        rootRef.updateChildren(messageUserMap, (databaseError, databaseReference) -> {

            if (databaseError == null) {

                rootRef.child("messages").child(userId).child(chat_id).child(push_id).child("seen").setValue(true);
                rootRef.child("messages").child(userId).child(chat_id).child(push_id).child("isRead").setValue(true);

                dialog.dismiss();
                finish();
            }

        });

    }

    private void getMembers() {
        firebaseDatabase.getReference().child("Users").child(chat_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                admin = dataSnapshot.child("admin").getValue(String.class);
                if(userId.equals(admin)) {
                    imgUpload.setVisibility(View.VISIBLE);
                    btnAddMember.setVisibility(View.VISIBLE);
                }
                else {
                    imgUpload.setVisibility(View.INVISIBLE);
                    btnAddMember.setVisibility(View.GONE);
                }
                GenericTypeIndicator<List<String>> genericTypeIndicator = new GenericTypeIndicator<List<String>>(){};
                userIDs = dataSnapshot.child("members").getValue(genericTypeIndicator);
                userIDs.add(admin);
                loadUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void uploadImage() {
        bar.setVisibility(View.VISIBLE);
        imgUpload.setVisibility(View.INVISIBLE);

        final ImageView imgContain = new ImageView(this);
        imgContain.setImageURI(resultUri);
        Bitmap bitmap = ((BitmapDrawable) imgContain.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        final byte[] thumb_byte = stream.toByteArray();


        StorageReference filepath = FirebaseStorage.getInstance().getReference().child("profile_images").child(chat_id + ".jpg");
        //final StorageReference thumb_filepath = FirebaseStorage.getInstance().getReference().child("profile_images").child("thumbs").child(push_id + ".jpg");

        UploadTask uploadTask = filepath.putBytes(thumb_byte);

        uploadTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                filepath.getDownloadUrl().addOnSuccessListener(uri -> {

                    profileImgView.setImageURI(resultUri);
                    rootRef.child("Users").child(chat_id).child("img_url").setValue(uri.toString()).addOnSuccessListener(aVoid -> {
                        bar.setVisibility(View.INVISIBLE);
                        imgUpload.setVisibility(View.VISIBLE);
                    });

                });
            }
        });

    }

    private void loadUsers() {
        if(isInternetAvailable(this)){
            service = RetrofitClient.getInstance().create(UserService.class);
            service.getUsers().enqueue(new Callback<List<Users>>(){
                @Override
                public void onResponse(Call<List<Users>> call, Response<List<Users>> response) {
                    for(Users users : response.body()){
                        if(userIDs.contains(users.getUserId()))
                            list.add(users);
                    }
                    adapter.notifyDataSetChanged();
                    recyclerView.setVisibility(View.VISIBLE);
                    membersBar.setVisibility(View.GONE);

                }

                @Override
                public void onFailure(Call<List<Users>> call, Throwable t) {
                    membersBar.setVisibility(View.GONE);
                    Toast.makeText(GroupChatSettings.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            Toast.makeText(this, "no internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private static boolean isInternetAvailable(Context context){
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager.getActiveNetworkInfo() != null
                && manager.getActiveNetworkInfo().isAvailable()
                && manager.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                uploadImage();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public String getId() {
        return admin;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return super.onMenuOpened(featureId, menu);
    }
}
