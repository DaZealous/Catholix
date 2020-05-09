package www.catholix.com.ng;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;

import config.ChatDatabase;

public class ChangeChatBack extends AppCompatActivity {

    Button btnRemove, btnChange, btnOk;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_chat_back);
        btnRemove = findViewById(R.id.activity_change_chat_back_remove);
        btnChange = findViewById(R.id.activity_change_chat_back_change);
        imageView = findViewById(R.id.activity_change_chat_back_image);
        btnOk = findViewById(R.id.activity_change_chat_back_btn_ok);

        if (new ChatDatabase(this).findChatImage("1") == null) {
            imageView.setImageResource(R.drawable.chat_back_img);
            btnRemove.setEnabled(false);
            btnRemove.setAlpha(0.5f);
        } else {
            btnRemove.setEnabled(true);
            btnRemove.setAlpha(1);
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(new ChatDatabase(this).findChatImage("1"),
                    0, new ChatDatabase(this).findChatImage("1").length));
        }

        btnChange.setOnClickListener(view -> CropImage.activity().start(this));

        btnRemove.setOnClickListener(view -> removePic());

        btnOk.setOnClickListener(view -> changePic());


    }

    private void removePic() {
        try {
            imageView.setImageResource(R.drawable.chat_back_img);
            new ChatDatabase(this).deleteChatImage();
            btnRemove.setEnabled(false);
            btnRemove.setAlpha(0.5f);
        } catch (Exception e) {
            showAlert();
        }
    }


    private void changePic() {
        try {
            byte[] pic = getByte();

            if (new ChatDatabase(this).findChatImage("1") == null)
                new ChatDatabase(this).addChatImage(pic);
            else
                new ChatDatabase(this).updateChatImage(pic);

            btnRemove.setEnabled(true);
            btnRemove.setAlpha(1);
            btnOk.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Chat Background changed successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            showAlert();
        }

    }

    private byte[] getByte() {
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        return stream.toByteArray();
    }

    private void showAlert() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this)
                .setTitle("Error!")
                .setMessage("An error occurred, pls try again")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, i) -> dialog.dismiss());
        alert.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                assert result != null;
                Uri resultUri = result.getUri();
                imageView.setImageURI(resultUri);
                btnOk.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "please select an image", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
