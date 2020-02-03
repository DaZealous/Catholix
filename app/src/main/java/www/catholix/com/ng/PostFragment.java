package www.catholix.com.ng;

import android.content.Intent;
import android.graphics.Bitmap;
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
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import api.VolleyInstance;
import config.NetworkConfig;
import config.SharedPref;

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
           Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
           intent.setType("image/*");
           startActivityForResult(Intent.createChooser(intent, "SELECT IMAGE"), RC_IMG_PICK);

       });
       return view;
    }

    private void doPost() {
        if(NetworkConfig.getInstance(getContext()).networkAvailable()){
            Toast.makeText(getContext(), "uploading your post...", Toast.LENGTH_SHORT).show();
            btnPost.setAlpha(0.6f);
            btnPost.setEnabled(false);

            Map<String, String> map = new HashMap<>();
            map.put("feed_media", image);
            map.put("title", "post");
            map.put("userID", SharedPref.getInstance(getContext()).getId());
            map.put("category", "2");
            map.put("reach", "3");
            map.put("article", postEdit.getText().toString());
            map.put("post_feed_req", "set");
            StringRequest request = new StringRequest(Request.Method.POST, "https://www.catholix.com.ng/api.developer/POST/post_feeds.php",
                    response -> {
                        try {
                                Toast.makeText(getContext(), String.valueOf(response), Toast.LENGTH_SHORT).show();
                            btnPost.setAlpha(1f);
                            btnPost.setEnabled(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        NetworkResponse response = error.networkResponse;
                        try {
                            String res = new String(response.data,
                                    HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                            Toast.makeText(getContext(), res, Toast.LENGTH_SHORT).show();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        btnPost.setAlpha(1f);
                        btnPost.setEnabled(true);
                    }
            ) {
                @Override
                public byte[] getBody() throws AuthFailureError {
                    /*ByteArrayOutputStream stream = null;
                    try {
                     stream = new ByteArrayOutputStream();
                        ObjectOutputStream out = new ObjectOutputStream(stream);
                        out.writeObject(map);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
                    try {
                        return map.toString().getBytes("utf-8");
                    } catch (UnsupportedEncodingException e) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", map.toString(), "utf-8");
                        return null;
                    }
                }

                /*@Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("Content-Type", "x-www-form-urlencoded");
                    return params;
                }

                @Override
                public String getBodyContentType() {
                    return "x-www-form-urlencoded";
                }*/

               /* @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> map = new HashMap<>();
                    map.put("feed_media", image);
                    map.put("title", "post");
                    map.put("userID", SharedPref.getInstance(getContext()).getId());
                    map.put("category", "2");
                    map.put("reach", "3");
                    map.put("article", postEdit.getText().toString());
                    map.put("post_feed_req", "set");
                    return map;
                }*/
            };

            RetryPolicy policy = new DefaultRetryPolicy(30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            request.setRetryPolicy(policy);
            VolleyInstance.getInstance(getContext()).addToQueue(request);
        }else
            Toast.makeText(getContext(), "no internet connection", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_IMG_PICK && resultCode == RESULT_OK){
            Uri filePath = data.getData();
            Glide.with(getContext()).load(filePath).into(pickImgView);
            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                Bitmap lastBitmap = null;
                lastBitmap = bitmap;
                image = getStringImage(lastBitmap);
               // Toast.makeText(getContext(), image, Toast.LENGTH_SHORT).show();
            }catch (Exception e){

            }
            //pickImgView.setImageURI(data.getData());
        }
    }

    private String getStringImage(Bitmap lastBitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        lastBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageByte = stream.toByteArray();
        return Base64.encodeToString(imageByte, Base64.DEFAULT);
    }
}
