package www.catholix.com.ng;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Objects;
import config.SharedPref;


public class Settings extends Fragment {


    private Switch switchNotify;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        TextView btnChangeChatBack = view.findViewById(R.id.fragment_settings_change_chat_back);
        TextView btnLogout = view.findViewById(R.id.fragment_settings_logout);
        TextView btnNotifyText = view.findViewById(R.id.fragment_settings_mute_notifications_text);
        switchNotify = view.findViewById(R.id.fragment_settings_mute_notifications);

        if (SharedPref.getInstance(getContext()).getMuteNotify()) {
            btnNotifyText.setText(R.string.un_mute_notifications);
            switchNotify.setChecked(true);
        } else {
            btnNotifyText.setText(R.string.mute_notifications);
            switchNotify.setChecked(false);
        }

        btnChangeChatBack.setOnClickListener(view1 -> startActivity(new Intent(getContext(), ChangeChatBack.class)));
        btnLogout.setOnClickListener(view1 -> isConfirmed());

        switchNotify.setOnCheckedChangeListener((btn, isVal) -> {
            if (btn.isChecked())
                confirmNotify();
            else {
                SharedPref.getInstance(getContext()).muteNotify(false);
                Toast.makeText(getContext(), "Notification Un Mute", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void signOut() {
        ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setTitle("Signing Out...");
        dialog.setMessage("please wait");
        dialog.setCancelable(false);
        dialog.show();
        FirebaseDatabase.getInstance().getReference().child("Users").child(SharedPref.getInstance(getContext()).getId())
                .child("online")
                .setValue(System.currentTimeMillis())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseDatabase.getInstance().getReference().child("Users").child(SharedPref.getInstance(getContext()).getId())
                                .child("device_token")
                                .setValue("")
                                .addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        SharedPref.getInstance(getContext()).removeUser();
                                        startActivity(new Intent(getContext(), WelcomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                    } else {
                                        Toast.makeText(getContext(), Objects.requireNonNull(task2.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                });
                    } else {
                        Toast.makeText(getContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
    }

    private void isConfirmed() {
        AlertDialog.Builder alert = new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                .setTitle("Sign Out?")
                .setMessage("Are you sure you want to sign out?")
                .setCancelable(false)
                .setNegativeButton("NOPE", (dialog, i) -> dialog.dismiss())
                .setPositiveButton("SURE", (dialog, i) -> {
                    dialog.dismiss();
                    signOut();
                });
        alert.create().show();
    }

    private void confirmNotify() {
        AlertDialog.Builder alert = new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                .setTitle("Mute Notification?")
                .setMessage("You won't be able to receive any notification messages on this app.\nAre you sure you want to mute notification?")
                .setCancelable(false)
                .setNegativeButton("NOPE", (dialog, i) -> {
                    dialog.dismiss();
                    switchNotify.setChecked(false);
                })
                .setPositiveButton("SURE", (dialog, i) -> {
                    dialog.dismiss();
                    SharedPref.getInstance(getContext()).muteNotify(true);
                    Toast.makeText(getContext(), "Notification Mute Successfully", Toast.LENGTH_SHORT).show();
                });
        alert.create().show();
    }

}
