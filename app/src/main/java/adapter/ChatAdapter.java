package adapter;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import config.SharedPref;
import model.ChatDao;
import view.ChatsView;
import www.catholix.com.ng.FileViewer;
import www.catholix.com.ng.R;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.viewHolder> {

    private Context context;
    private List<ChatDao> list;
    private ChatsView chatsView;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying;
    private Handler handler;

    public ChatAdapter(Context context, List<ChatDao> list, ChatsView chatsView) {
        this.context = context;
        this.list = list;
        this.chatsView = chatsView;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.chats_layout, parent, false);
        return new ChatAdapter.viewHolder(v);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        try {
            ChatDao dao = list.get(position);
            holder.checkUser(dao.getFrom());
            holder.checkType(dao.getMsg_type(), dao.getMsg_body(), dao.getMsg_name());
            holder.setTextAdmin(dao.getMsg_body());
            holder.setTextUser(dao.getMsg_body());
            holder.setTextUserTime(Long.toString(dao.getTime_stamp()));
            holder.setTextAdminTime(Long.toString(dao.getTime_stamp()));
            holder.imgChatUserTime.setText(DateUtils.getRelativeTimeSpanString(dao.getTime_stamp()));
            holder.imgChatAdminTime.setText(DateUtils.getRelativeTimeSpanString(dao.getTime_stamp()));
            holder.userAudioTime.setText(DateUtils.getRelativeTimeSpanString(dao.getTime_stamp()));
            holder.adminAudioTime.setText(DateUtils.getRelativeTimeSpanString(dao.getTime_stamp()));
            mediaPlayer = null;
            isPlaying = false;
            handler = new Handler();

                holder.imgLayout1.setOnClickListener(view ->{
                                //Toast.makeText(context, "clicked", Toast.LENGTH_SHORT).show();
                    if(holder.chatAdminImg.getDrawable() != null) {
                        Bitmap bitmap = ((BitmapDrawable) holder.chatAdminImg.getDrawable()).getBitmap();
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream);
                        final byte[] thumb_byte = stream.toByteArray();
                        holder.viewImg(thumb_byte, holder.chatAdminImg
                                , dao.getToUsername(), dao.getTime_stamp(), chatsView.getUserPhoto());
                    }
            });
                holder.imgLayout2.setOnClickListener(view -> {
                   // Toast.makeText(context, "clicked 2", Toast.LENGTH_SHORT).show();
                    if(holder.chatUserImg.getDrawable() != null) {
                        Bitmap bitmap = ((BitmapDrawable) holder.chatUserImg.getDrawable()).getBitmap();
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream);
                        final byte[] thumb_byte = stream.toByteArray();
                        holder.viewImg(thumb_byte, holder.chatUserImg
                                , dao.getFromUsername(), dao.getTime_stamp(), chatsView.getUserPhoto());
                    }
                });

                holder.userAudioPlayBtn.setOnClickListener(view -> {
                    if(!new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Catholix/"+dao.getMsg_name()).exists()){
                        holder.downloadAudio(dao.getMsg_name(), dao.getMsg_body());
                    }else {
                        if (isPlaying) {
                            holder.userAudioPlayBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                            if (mediaPlayer != null)
                                mediaPlayer.pause();
                            isPlaying = false;
                        } else {
                            try {
                                holder.userAudioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                    @Override
                                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                                        if (mediaPlayer != null && b) {
                                            mediaPlayer.seekTo(i);
                                        }
                                    }

                                    @Override
                                    public void onStartTrackingTouch(SeekBar seekBar) {

                                    }

                                    @Override
                                    public void onStopTrackingTouch(SeekBar seekBar) {

                                    }
                                });
                            isPlaying = true;
                            mediaPlayer = new MediaPlayer();
                                mediaPlayer.setDataSource(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Catholix/"+dao.getMsg_name());
                                mediaPlayer.prepare();
                                holder.userAudioSeekBar.setMax(mediaPlayer.getDuration()/1000);
                                mediaPlayer.start();
                                holder.userAudioPlayBtn.setImageResource(R.drawable.ic_pause_white_24dp);
                                handler.postDelayed(() -> {
                                    if(mediaPlayer != null){
                                        holder.userAudioSeekBar.setProgress(mediaPlayer.getCurrentPosition()/1000);
                                        holder.adminAudioSeekBar.setProgress(mediaPlayer.getCurrentPosition()/1000);
                                    }
                                    mediaPlayer.setOnCompletionListener(mediaPlayer -> {
                                        isPlaying = false;
                                        holder.userAudioPlayBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                                        holder.adminAudioPlayBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                                    });
                                }, 1000);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

            holder.adminAudioPlayBtn.setOnClickListener(view -> {
                if(!new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Catholix/"+dao.getMsg_name()).exists()){
                    holder.downloadAudio(dao.getMsg_name(), dao.getMsg_body());
                }else {
                    if (isPlaying) {
                        holder.adminAudioPlayBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                        if (mediaPlayer != null)
                            mediaPlayer.pause();
                        isPlaying = false;
                    } else {
                        holder.adminAudioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                                if (mediaPlayer != null && b) {
                                    mediaPlayer.seekTo(i);
                                }
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                            }
                        });
                        isPlaying = true;
                        mediaPlayer = new MediaPlayer();
                        try {
                            mediaPlayer.setDataSource(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Catholix/"+dao.getMsg_name());
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                            holder.adminAudioSeekBar.setMax(mediaPlayer.getDuration()/1000);
                            holder.adminAudioPlayBtn.setImageResource(R.drawable.ic_pause_white_24dp);
                            handler.postDelayed(() -> {
                                if(mediaPlayer != null){
                                    holder.userAudioSeekBar.setProgress(mediaPlayer.getCurrentPosition()/1000);
                                    holder.adminAudioSeekBar.setProgress(mediaPlayer.getCurrentPosition()/1000);
                                }
                                mediaPlayer.setOnCompletionListener(mediaPlayer -> {
                                    isPlaying = false;
                                    holder.userAudioPlayBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                                    holder.adminAudioPlayBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                                });
                            }, 1000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class viewHolder extends RecyclerView.ViewHolder {

        LinearLayout adminLinear;
        RelativeLayout userLinear, imgLayout1, imgLayout2;
        CardView imgUser, imgAdmin, textAdminCard, textUserCard, cardAdminAudio, cardUserAudio;
        TextView textUser, textAdmin, adminTime, userTime, imgChatUserTime, imgChatAdminTime, adminAudioTime, userAudioTime;
        ImageView chatAdminImg, chatUserImg;
        ImageButton adminAudioPlayBtn, userAudioPlayBtn;
        SeekBar adminAudioSeekBar, userAudioSeekBar;

        private viewHolder(@NonNull View itemView) {
            super(itemView);
            userLinear = itemView.findViewById(R.id.chat_layout_user_layout);
            adminLinear = itemView.findViewById(R.id.chat_layout_admin_layout);
            imgUser = itemView.findViewById(R.id.chat_user_img_card);
            imgAdmin = itemView.findViewById(R.id.chat_admin_img_card);
            textUser = itemView.findViewById(R.id.chat_user_text);
            textAdmin = itemView.findViewById(R.id.chat_admin_text);
            textAdminCard = itemView.findViewById(R.id.chat_admin_text_card);
            textUserCard = itemView.findViewById(R.id.chat_user_text_card);
            adminTime = itemView.findViewById(R.id.chat_admin_text_time);
            userTime = itemView.findViewById(R.id.chat_user_text_time);
            chatAdminImg = itemView.findViewById(R.id.chat_admin_img_btn);
            chatUserImg = itemView.findViewById(R.id.chat_user_img_btn);
            imgChatAdminTime = itemView.findViewById(R.id.chat_img_admin_time);
            imgChatUserTime = itemView.findViewById(R.id.chat_img_user_time);
            imgLayout1 = itemView.findViewById(R.id.chat_admin_img_btn_layout);
            imgLayout2 = itemView.findViewById(R.id.chat_user_img_btn_layout);
            cardAdminAudio = itemView.findViewById(R.id.chat_admin_card_audio);
            cardUserAudio = itemView.findViewById(R.id.chat_user_card_audio);
            adminAudioTime = itemView.findViewById(R.id.chat_admin_audio_time);
            userAudioTime = itemView.findViewById(R.id.chat_user_audio_time);
            userAudioPlayBtn = itemView.findViewById(R.id.chat_user_audio_play_btn);
            adminAudioPlayBtn = itemView.findViewById(R.id.chat_admin_audio_play_btn);
            adminAudioSeekBar = itemView.findViewById(R.id.chat_admin_audio_seek_bar);
            userAudioSeekBar = itemView.findViewById(R.id.chat_user_audio_seek_bar);
        }

        private void setTextUser(String text) {
            textUser.setText(text);
        }

        private void setTextAdmin(String text) {
            textAdmin.setText(text);
        }

        private void setTextUserTime(String text) {
            userTime.setText(DateUtils.getRelativeTimeSpanString(Long.parseLong(text)));
        }

        private void setTextAdminTime(String time) {
            adminTime.setText(DateUtils.getRelativeTimeSpanString(Long.parseLong(time)));
        }

        private void checkUser(String user) {
            if (user.equalsIgnoreCase(SharedPref.getInstance(context).getId())) {
                adminLinear.setVisibility(View.GONE);
                userLinear.setVisibility(View.VISIBLE);
            } else {
                userLinear.setVisibility(View.GONE);
                adminLinear.setVisibility(View.VISIBLE);
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        private void checkType(String msg_type, String img, String msg_name) {
            if (msg_type.equalsIgnoreCase("text")) {
                textAdminCard.setVisibility(View.VISIBLE);
                textUserCard.setVisibility(View.VISIBLE);
                imgAdmin.setVisibility(View.GONE);
                imgUser.setVisibility(View.GONE);
                cardUserAudio.setVisibility(View.GONE);
                cardAdminAudio.setVisibility(View.GONE);
            } else if(msg_type.equalsIgnoreCase("image")){
                Glide.with(context).load(img).into(chatAdminImg);
                Glide.with(context).load(img).into(chatUserImg);
                imgAdmin.setVisibility(View.VISIBLE);
                imgUser.setVisibility(View.VISIBLE);
                textAdminCard.setVisibility(View.GONE);
                textUserCard.setVisibility(View.GONE);
                cardUserAudio.setVisibility(View.GONE);
                cardAdminAudio.setVisibility(View.GONE);
            }else{
                textAdminCard.setVisibility(View.GONE);
                textUserCard.setVisibility(View.GONE);
                imgAdmin.setVisibility(View.GONE);
                imgUser.setVisibility(View.GONE);
                cardUserAudio.setVisibility(View.VISIBLE);
                cardAdminAudio.setVisibility(View.VISIBLE);
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Catholix/"+msg_name);
                if(file.exists()) {
                    userAudioPlayBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                    adminAudioPlayBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                }else{
                    userAudioPlayBtn.setImageResource(R.drawable.ic_file_download_white_24dp);
                    adminAudioPlayBtn.setImageResource(R.drawable.ic_file_download_white_24dp);
                }
            }
        }

        private void viewImg(byte[] thumb_byte, ImageView imgAdmin, String username, long time, String img) {

            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context,
                    imgAdmin, "image");
            context.startActivity(new Intent(context, FileViewer.class).putExtra("username", username)
                    .putExtra("photo", thumb_byte).putExtra("time", time).putExtra("img_url", img), optionsCompat.toBundle());
        }

        private void downloadAudio(String file, String msgBody) throws NullPointerException{
            chatsView.startProgress(View.VISIBLE);
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(msgBody));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir("/Catholix", file);
            downloadManager.enqueue(request);
            BroadcastReceiver onComplete = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    try {
                        chatsView.startProgress(View.GONE);
                        userAudioPlayBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                        adminAudioPlayBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                    } catch (Exception e) {
                    }
                }
            };
            context.getApplicationContext().registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        }
    }
}
