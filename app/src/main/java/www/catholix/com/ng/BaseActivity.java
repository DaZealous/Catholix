package www.catholix.com.ng;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import config.SharedPref;

public class BaseActivity extends AppCompatActivity {

    private DatabaseReference mUserRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(SharedPref.getInstance(this).getId());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUserRef.child("online").setValue("true");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mUserRef.child("online").setValue("true");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
    }
}
