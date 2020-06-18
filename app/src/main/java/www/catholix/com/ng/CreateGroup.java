package www.catholix.com.ng;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
import adapter.SelectUserAdapter;
import adapter.UsersAdapter;
import api.RetrofitClient;
import config.SharedPref;
import de.hdodenhof.circleimageview.CircleImageView;
import model.Users;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import view.SelectUserView;

public class CreateGroup extends AppCompatActivity implements SelectUserView {

    List<Users> list;
    UserService service;
    SelectUserAdapter adapter;
    ProgressBar bar;
    List<String> users;
    TextView textParticipant;
    RecyclerView recyclerView;
    FloatingActionButton fabCreate;
    EditText editGroupName;
    DatabaseReference rootRef, groupRef;
    FirebaseDatabase firebaseDatabase;
    ProgressDialog dialog;
    CircleImageView circleImageView;
    private Uri resultUri;
    private String push_id;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        list = new ArrayList<>();
        adapter = new SelectUserAdapter(this, list, this);
        bar = findViewById(R.id.create_group_progress_bar);
        fabCreate = findViewById(R.id.activity_create_group_btn_create);
        textParticipant = findViewById(R.id.create_group_participant);
        recyclerView = findViewById(R.id.create_group_recycler_view);
        editGroupName = findViewById(R.id.activity_create_group_edit_text);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        users = new ArrayList<>();
        circleImageView = findViewById(R.id.activity_create_group_img_view);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create Group");

        resultUri = null;

        dialog = new ProgressDialog(this);
        dialog.setTitle("Creating Group");
        dialog.setMessage("Please wait");
        dialog.setCancelable(false);

        firebaseDatabase = FirebaseDatabase.getInstance();
        rootRef = firebaseDatabase.getReference();
        fabCreate.setOnClickListener(view -> {
            if (TextUtils.isEmpty(editGroupName.getText().toString()) || users.size() == 0) {
                Toast.makeText(this, "all fields are required", Toast.LENGTH_SHORT).show();
            } else {
                if (resultUri != null)
                    uploadImage();
                else {
                    dialog.show();
                    groupRef = rootRef.child("Users").push();
                    push_id = groupRef.getKey();
                    createGroup(resultUri);
                }
            }
        });

        circleImageView.setOnClickListener(view ->
                CropImage.activity(resultUri)
                        .setAspectRatio(1, 1)
                        .setMinCropWindowSize(500, 500)
                        .start(this));
        loadUsers();
    }

    private void uploadImage() {
        // File thumb_filePath = new File(resultUri.getPath());
        dialog.setTitle("Uploading Image");
        dialog.show();
        groupRef = rootRef.child("Users").push();

        push_id = groupRef.getKey();

        final ImageView imgContain = new ImageView(this);
        imgContain.setImageURI(resultUri);
        Bitmap bitmap = ((BitmapDrawable) imgContain.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        final byte[] thumb_byte = stream.toByteArray();


        StorageReference filepath = FirebaseStorage.getInstance().getReference().child("profile_images").child(push_id + ".jpg");
        //final StorageReference thumb_filepath = FirebaseStorage.getInstance().getReference().child("profile_images").child("thumbs").child(push_id + ".jpg");

        UploadTask uploadTask = filepath.putBytes(thumb_byte);

        uploadTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                filepath.getDownloadUrl().addOnSuccessListener(uri -> createGroup(uri));
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                circleImageView.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createGroup(Uri uri) {
        dialog.setTitle("Creating Group");
        String chat_group_ref = "Users/" + push_id;
        String chat_chat_ref = "Chat/" + push_id;

        Map GroupMap = new HashMap();
        GroupMap.put("username", editGroupName.getText().toString());
        GroupMap.put("admin", SharedPref.getInstance(this).getId());
        GroupMap.put("img_url", (uri != null) ? uri.toString() : "");
        GroupMap.put("created_at", System.currentTimeMillis());
        GroupMap.put("members", Arrays.asList(users.toArray()));

        HashMap ChatMap = new HashMap();
        ChatMap.put("seen", false);
        ChatMap.put("time_stamp", System.currentTimeMillis());

        Map singleGroupMap = new HashMap();
        singleGroupMap.put(chat_group_ref, GroupMap);
        singleGroupMap.put(chat_chat_ref, ChatMap);

        rootRef.updateChildren(singleGroupMap, ((databaseError, databaseReference) -> {
            if (databaseError == null) {
                for (int i = 0; i < users.size(); i++) {
                    String user = users.get(i);
                    rootRef.child("Chat").child(user).child(push_id).updateChildren(ChatMap, ((dbError, dbReference) -> {
                    }));
                    if (i == users.size() - 1) {
                        rootRef.child("Chat").child(SharedPref.getInstance(CreateGroup.this).getId()).child(push_id).updateChildren(ChatMap,
                                ((dbError2, dbRef2) -> {
                                    Toast.makeText(this, "group created successfully", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    onBackPressed();
                                    finish();
                                }));
                    }
                }
            } else {
                dialog.dismiss();
                Toast.makeText(this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        ));

    }

    private void loadUsers() {
        list.clear();
        if (isInternetAvailable(this)) {
            service = RetrofitClient.getInstance().create(UserService.class);
            service.getUsers().enqueue(new Callback<List<Users>>() {
                @Override
                public void onResponse(Call<List<Users>> call, Response<List<Users>> response) {
                    for (Users users : response.body()) {
                        if (!users.getUserId().equals(SharedPref.getInstance(CreateGroup.this).getId()))
                            list.add(users);
                    }
                    adapter.notifyDataSetChanged();
                    bar.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(Call<List<Users>> call, Throwable t) {
                    bar.setVisibility(View.GONE);
                    Toast.makeText(CreateGroup.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            bar.setVisibility(View.GONE);
            Toast.makeText(this, "no internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private static boolean isInternetAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager.getActiveNetworkInfo() != null
                && manager.getActiveNetworkInfo().isAvailable()
                && manager.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    @Override
    public void selectUser(String id) {
        users.add(id);
        setParticipants();
    }

    private void setParticipants() {
        textParticipant.setText("Participant : " + users.size());
    }

    @Override
    public void removeUser(String id) {
        users.remove(id);
        setParticipants();
    }

    @Override
    public String getUser(int position) {
        return users.get(position);
    }

    @Override
    public List<String> getUsers() {
        return users;
    }
}
