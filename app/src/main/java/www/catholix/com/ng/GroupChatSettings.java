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
import android.content.DialogInterface;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
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
    String img_url, username, chat_id, admin;
    ImageView profileImgView;
    ImageButton imgUpload;
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
    CardView layoutAddUser, layoutLeave, layoutClearChats;
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
        layoutAddUser = findViewById(R.id.activity_group_settings_add_new_user_layout);
        layoutLeave = findViewById(R.id.activity_group_settings_leave_group_layout);
        layoutClearChats = findViewById(R.id.activity_group_settings_clear_chats_layout);

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

    }

    private void getMembers() {
        FirebaseDatabase.getInstance().getReference().child("Users").child(chat_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                admin = dataSnapshot.child("admin").getValue(String.class);
                if(SharedPref.getInstance(GroupChatSettings.this).getId().equals(admin)) {
                    imgUpload.setVisibility(View.VISIBLE);
                    layoutAddUser.setVisibility(View.VISIBLE);
                }
                else {
                    imgUpload.setVisibility(View.INVISIBLE);
                    layoutAddUser.setVisibility(View.GONE);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_settings_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return super.onMenuOpened(featureId, menu);
    }
}
