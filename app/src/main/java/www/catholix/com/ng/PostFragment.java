package www.catholix.com.ng;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import Service.FeedsService;
import api.RetrofitClient;
import api.VolleyInstance;
import config.NetworkConfig;
import config.SharedPref;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

public class PostFragment extends BottomSheetDialogFragment {


    public PostFragment() {
    }

    private static int RC_IMG_PICK = 1;
    private CardView pickImage;
    private ImageView pickImgView;
    private TextInputEditText postEdit;
    private Button btnPost;
    private String image;
    private File imageFile;
    private ResponseBody responseBody;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.fragment_post, container, false);
       pickImage = view.findViewById(R.id.post_pick_img_upload_card);
       pickImgView = view.findViewById(R.id.new_post_image_view_feed);
       postEdit = view.findViewById(R.id.fragment_post_edit_text_post);
       btnPost = view.findViewById(R.id.fragment_post_btn_post);

       btnPost.setOnClickListener(view12 -> {
           if(!TextUtils.isEmpty(image))
           doPost();
           else
               Toast.makeText(getContext(), "select image", Toast.LENGTH_SHORT).show();
       });

       postEdit.addTextChangedListener(new TextWatcher() {
           @Override
           public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

           }

           @Override
           public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
               if(!TextUtils.isEmpty(charSequence.toString())) {
                   if (charSequence.toString().length() <= 1000) {
                       btnPost.setAlpha(1f);
                       btnPost.setEnabled(true);
                   } else {
                       btnPost.setAlpha(0.6f);
                       btnPost.setEnabled(false);
                   }
               }else{
                   btnPost.setAlpha(0.6f);
                   btnPost.setEnabled(false);
               }

           }

           @Override
           public void afterTextChanged(Editable editable) {

           }
       });
       pickImage.setOnClickListener(view1 -> {
           Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
           startActivityForResult(Intent.createChooser(intent, "SELECT IMAGE"), RC_IMG_PICK);

       });
       return view;
    }

    private void doPost() {
        if(NetworkConfig.getInstance(getContext()).networkAvailable()){
            Toast.makeText(getContext(), "uploading your post...", Toast.LENGTH_SHORT).show();
            btnPost.setAlpha(0.6f);
            btnPost.setEnabled(false);

            imageFile = new File(image);

            try {
                FeedsService service = RetrofitClient.getInstance().create(FeedsService.class);
                Observable<ResponseBody> call = service.postNewsFeed(imageFile, "post", SharedPref.getInstance(getContext()).getId(),
                        "2", "3", postEdit.getText().toString(), "set")
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io());

               call.subscribe(new Observer<ResponseBody>() {
                   @Override
                   public void onSubscribe(Disposable d) {

                   }

                   @Override
                   public void onNext(ResponseBody value) {
                       responseBody = value;
                       btnPost.setAlpha(1);
                       btnPost.setEnabled(true);
                       try {
                           Toast.makeText(getContext(), value.string(), Toast.LENGTH_SHORT).show();
                       } catch (IOException e) {
                           Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                       }
                   }

                   @Override
                   public void onError(Throwable e) {
                       btnPost.setAlpha(1);
                       btnPost.setEnabled(true);
                       Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                   }

                   @Override
                   public void onComplete() {
                       btnPost.setAlpha(1);
                       btnPost.setEnabled(true);
                       try {
                           Toast.makeText(getContext(), responseBody.string(), Toast.LENGTH_SHORT).show();
                       } catch (IOException e) {
                           Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                       }
                   }
               });
            } catch (Exception e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }else
            Toast.makeText(getContext(), "no internet connection", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_IMG_PICK && resultCode == RESULT_OK){
            Uri filePath = data.getData();
           // Glide.with(getContext()).load(filePath).into(pickImgView);
            try{
                /*Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                pickImgView.setImageBitmap(bitmap);
                Bitmap lastBitmap;
                lastBitmap = bitmap;
                image = getStringImage(lastBitmap);*/
               // Toast.makeText(getContext(), image, Toast.LENGTH_SHORT).show();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContext().getContentResolver().query(filePath, filePathColumn, null, null, null, null);
                assert cursor != null;
                cursor.moveToFirst();
                int index = cursor.getColumnIndex(filePathColumn[0]);
                image = cursor.getString(index);
                pickImgView.setImageBitmap(BitmapFactory.decodeFile(image));
                cursor.close();
            }catch (Exception e){

            }
        }
    }

    private String getStringImage(Bitmap lastBitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        lastBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageByte = stream.toByteArray();
        return Base64.encodeToString(imageByte, Base64.DEFAULT);
    }
}
