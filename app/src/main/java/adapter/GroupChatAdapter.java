package adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

import config.SharedPref;
import model.ChatDao;
import view.ChatsView;
import www.catholix.com.ng.FileViewer;
import www.catholix.com.ng.R;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.viewHolder> {

    private Context context;
    private List<ChatDao> list;
    private ChatsView chatsView;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying;
    private Handler handler;

    public GroupChatAdapter(Context context, List<ChatDao> list, ChatsView chatsView) {
        this.context = context;
        this.list = list;
        this.chatsView = chatsView;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.group_chat_layout, parent, false);
        return new GroupChatAdapter.viewHolder(v);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {

        holder.adminPlayVideoBtn.setVisibility(View.INVISIBLE);
        holder.userPlayVideoBtn.setVisibility(View.INVISIBLE);
        holder.userVideoBar.setVisibility(View.INVISIBLE);
        holder.adminVideoBar.setVisibility(View.INVISIBLE);

        ChatDao dao = list.get(position);
        holder.checkUser(dao.getFrom());
        holder.checkType(dao.getMsg_type(), dao.getMsg_body(), dao.getMsg_name());
        holder.setTextAdmin(dao.getMsg_body());
        holder.setTextUser(dao.getMsg_body());
        holder.setTextUserTime(Long.toString(dao.getTime_stamp()));
        holder.setTextAdminTime(Long.toString(dao.getTime_stamp()));
        holder.textOtherUser.setText(dao.getFromUsername());
        holder.imgOtherUser.setText(dao.getFromUsername());
        holder.audioOtherUser.setText(dao.getFromUsername());
        holder.textAdminFileUser.setText(dao.getFromUsername());
        holder.textAdminContactUser.setText(dao.getFromUsername());
        holder.imgChatUserTime.setText(DateUtils.getRelativeTimeSpanString(dao.getTime_stamp()));
        holder.imgChatAdminTime.setText(DateUtils.getRelativeTimeSpanString(dao.getTime_stamp()));
        holder.userAudioTime.setText(DateUtils.getRelativeTimeSpanString(dao.getTime_stamp()));
        holder.adminAudioTime.setText(DateUtils.getRelativeTimeSpanString(dao.getTime_stamp()));
        holder.textAdminFileTime.setText(DateUtils.getRelativeTimeSpanString(dao.getTime_stamp()));
        holder.textUserFileTime.setText(DateUtils.getRelativeTimeSpanString(dao.getTime_stamp()));
        holder.textAdminContactTime.setText(DateUtils.getRelativeTimeSpanString(dao.getTime_stamp()));
        holder.textUserContactTime.setText(DateUtils.getRelativeTimeSpanString(dao.getTime_stamp()));

        mediaPlayer = null;
        isPlaying = false;
        handler = new Handler();

        holder.imgLayout1.setOnClickListener(view -> {
            if(dao.getMsg_type().equalsIgnoreCase("image")) {
                if (holder.chatAdminImg.getDrawable() != null) {
                    Bitmap bitmap = ((BitmapDrawable) holder.chatAdminImg.getDrawable()).getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    final byte[] thumb_byte = stream.toByteArray();
                    holder.viewImg(thumb_byte, holder.chatAdminImg
                            , dao.getToUsername(), dao.getTime_stamp(), chatsView.getUserPhoto());
                }
            }else{
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Catholix/" + dao.getMsg_name());
                if (file.exists()) {
                    holder.viewVideo(file.getAbsolutePath(),
                            dao.getFromUsername(), dao.getTime_stamp(), chatsView.getUserPhoto());
                } else {
                    holder.downloadVideo(dao.getMsg_name(), dao.getMsg_body());
                }
            }
        });


        holder.imgLayout2.setOnClickListener(view -> {
            if(dao.getMsg_type().equalsIgnoreCase("image")) {
                if (holder.chatUserImg.getDrawable() != null) {
                    Bitmap bitmap = ((BitmapDrawable) holder.chatUserImg.getDrawable()).getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    final byte[] thumb_byte = stream.toByteArray();
                    holder.viewImg(thumb_byte, holder.chatUserImg
                            , dao.getFromUsername(), dao.getTime_stamp(), chatsView.getUserPhoto());
                }
            }else{
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Catholix/" + dao.getMsg_name());
                if (file.exists()) {
                    holder.viewVideo(file.getAbsolutePath(),
                            dao.getFromUsername(), dao.getTime_stamp(), chatsView.getUserPhoto());
                } else {
                    holder.downloadVideo(dao.getMsg_name(), dao.getMsg_body());
                }
            }
        });

        holder.userAudioPlayBtn.setOnClickListener(view -> {
            if (!new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Catholix/" + dao.getMsg_name()).exists()) {
                holder.downloadAudio(dao.getMsg_name(), dao.getMsg_body());
            } else {
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
                        mediaPlayer.setDataSource(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Catholix/" + dao.getMsg_name());
                        mediaPlayer.prepare();
                        holder.userAudioSeekBar.setMax(mediaPlayer.getDuration() / 1000);
                        mediaPlayer.start();
                        holder.userAudioPlayBtn.setImageResource(R.drawable.ic_pause_white_24dp);
                        handler.postDelayed(() -> {
                            if (mediaPlayer != null) {
                                holder.userAudioSeekBar.setProgress(mediaPlayer.getCurrentPosition() / 1000);
                                holder.adminAudioSeekBar.setProgress(mediaPlayer.getCurrentPosition() / 1000);
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
            if (!new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Catholix/" + dao.getMsg_name()).exists()) {
                holder.downloadAudio(dao.getMsg_name(), dao.getMsg_body());
            } else {
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
                        mediaPlayer.setDataSource(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Catholix/" + dao.getMsg_name());
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        holder.adminAudioSeekBar.setMax(mediaPlayer.getDuration() / 1000);
                        holder.adminAudioPlayBtn.setImageResource(R.drawable.ic_pause_white_24dp);
                        handler.postDelayed(() -> {
                            if (mediaPlayer != null) {
                                holder.userAudioSeekBar.setProgress(mediaPlayer.getCurrentPosition() / 1000);
                                holder.adminAudioSeekBar.setProgress(mediaPlayer.getCurrentPosition() / 1000);
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

        holder.imgUser.setOnLongClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setMessage("Are you sure you want to delete this message?")
                    .setPositiveButton("Sure", (dialogInterface, i) -> {
                        if (dao.getMsg_type().equalsIgnoreCase("image"))
                            holder.deleteMsgImage(dao.getKey(), dao.getFrom(), dao.getTo(), dao.getMsg_name(), position);
                        else if (dao.getMsg_type().equalsIgnoreCase("text"))
                            holder.deleteMsgText(dao.getKey(), dao.getFrom(), dao.getTo(), dao.getMsg_name(), position);
                        else
                            holder.deleteMsgAudio(dao.getKey(), dao.getFrom(), dao.getTo(), dao.getMsg_name(), position);

                    })
                    .setNegativeButton("Cancel", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    });
            builder.create().show();
            return true;
        });

        holder.textUserCard.setOnLongClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setMessage("Are you sure you want to delete this message?")
                    .setPositiveButton("Sure", (dialogInterface, i) -> {
                        if (dao.getMsg_type().equalsIgnoreCase("image"))
                            holder.deleteMsgImage(dao.getKey(), dao.getFrom(), dao.getTo(), dao.getMsg_name(), position);
                        else if (dao.getMsg_type().equalsIgnoreCase("text"))
                            holder.deleteMsgText(dao.getKey(), dao.getFrom(), dao.getTo(), dao.getMsg_name(), position);
                        else
                            holder.deleteMsgAudio(dao.getKey(), dao.getFrom(), dao.getTo(), dao.getMsg_name(), position);

                    })
                    .setNegativeButton("Cancel", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    });
            builder.create().show();
            return true;
        });


        holder.cardUserAudio.setOnLongClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setMessage("Are you sure you want to delete this message?")
                    .setPositiveButton("Sure", (dialogInterface, i) -> {
                        if (dao.getMsg_type().equalsIgnoreCase("image"))
                            holder.deleteMsgImage(dao.getKey(), dao.getFrom(), dao.getTo(), dao.getMsg_name(), position);
                        else if (dao.getMsg_type().equalsIgnoreCase("text"))
                            holder.deleteMsgText(dao.getKey(), dao.getFrom(), dao.getTo(), dao.getMsg_name(), position);
                        else
                            holder.deleteMsgAudio(dao.getKey(), dao.getFrom(), dao.getTo(), dao.getMsg_name(), position);

                    })
                    .setNegativeButton("Cancel", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    });
            builder.create().show();
            return true;
        });

        holder.userPlayVideoBtn.setOnClickListener(view -> {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Catholix/" + dao.getMsg_name());
            if (file.exists()) {
                holder.viewVideo(file.getAbsolutePath(),
                        dao.getFromUsername(), dao.getTime_stamp(), chatsView.getUserPhoto());
            } else {
                holder.downloadVideo(dao.getMsg_name(), dao.getMsg_body());
            }
        });

        holder.adminPlayVideoBtn.setOnClickListener(view1 -> {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Catholix/" + dao.getMsg_name());
            if (file.exists()) {
                holder.viewVideo(file.getAbsolutePath(),
                        dao.getFromUsername(), dao.getTime_stamp(), chatsView.getUserPhoto());
            } else {
                holder.downloadVideo(dao.getMsg_name(), dao.getMsg_body());
            }
        });

        holder.userContactBtnView.setOnClickListener(view1 -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + dao.getMsg_body()));
            context.startActivity(intent);
        });

        holder.adminContactBtnView.setOnClickListener(view1 -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + dao.getMsg_body()));
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class viewHolder extends RecyclerView.ViewHolder {

        LinearLayout adminLinear;
        RelativeLayout userLinear, imgLayout1, imgLayout2;
        CardView imgUser, imgAdmin, textAdminCard, textUserCard, cardAdminAudio, cardUserAudio, cardUserFile, cardAdminFile, cardUserContact, cardAdminContact;
        TextView textUser, textAdmin, adminTime, userTime, imgChatUserTime, imgChatAdminTime, adminAudioTime, userAudioTime,
                imgOtherUser, textOtherUser, audioOtherUser, textUserFile, textUserFileTime, textAdminFile, textAdminFileTime, textUserContact, textUserContactTime, textAdminContact, textAdminContactTime, textAdminFileUser, textAdminContactUser;
        ImageView chatAdminImg, chatUserImg, userFileImage, adminFileImage;
        ImageButton adminAudioPlayBtn, userAudioPlayBtn, adminPlayVideoBtn, userPlayVideoBtn;
        SeekBar adminAudioSeekBar, userAudioSeekBar;
        ProgressBar adminVideoBar, userVideoBar;
        Button userContactBtnView, adminContactBtnView;

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
            imgOtherUser = itemView.findViewById(R.id.chat_admin_img_text_user);
            textOtherUser = itemView.findViewById(R.id.chat_admin_text_text_user);
            audioOtherUser = itemView.findViewById(R.id.chat_admin_audio_text_user);
            adminVideoBar = itemView.findViewById(R.id.chat_video_admin_progress_bar);
            userVideoBar = itemView.findViewById(R.id.chat_video_user_progress_bar);
            adminPlayVideoBtn = itemView.findViewById(R.id.chat_video_admin_download_play_btn);
            userPlayVideoBtn = itemView.findViewById(R.id.chat_video_user_download_play_btn);
            cardUserFile = itemView.findViewById(R.id.chat_user_card_file);
            cardAdminFile = itemView.findViewById(R.id.chat_admin_card_file);
            cardUserContact = itemView.findViewById(R.id.chat_user_card_contact);
            cardAdminContact = itemView.findViewById(R.id.chat_admin_card_contact);
            textUserFile = itemView.findViewById(R.id.chat_user_file_text_name);
            textUserFileTime = itemView.findViewById(R.id.chat_user_file_text_time);
            userFileImage = itemView.findViewById(R.id.chat_user_file_img);
            textAdminFile = itemView.findViewById(R.id.chat_admin_file_text_name);
            textAdminFileTime = itemView.findViewById(R.id.chat_admin_file_text_time);
            adminFileImage = itemView.findViewById(R.id.chat_admin_file_img);
            textUserContact = itemView.findViewById(R.id.chat_user_contact_text_name);
            textAdminContact = itemView.findViewById(R.id.chat_admin_contact_text_name);
            textUserContactTime = itemView.findViewById(R.id.chat_user_contact_text_time);
            textAdminContactTime = itemView.findViewById(R.id.chat_admin_contact_text_time);
            userContactBtnView = itemView.findViewById(R.id.chat_user_contact_view_btn);
            adminContactBtnView = itemView.findViewById(R.id.chat_admin_contact_view_btn);
            textAdminFileUser = itemView.findViewById(R.id.chat_admin_file_text_user);
            textAdminContactUser = itemView.findViewById(R.id.chat_admin_contact_text_user);
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
                cardAdminFile.setVisibility(View.GONE);
                cardUserFile.setVisibility(View.GONE);
                cardAdminContact.setVisibility(View.GONE);
                cardUserContact.setVisibility(View.GONE);
            } else if (msg_type.equalsIgnoreCase("image")) {
                Glide.with(context).load(img).into(chatAdminImg);
                Glide.with(context).load(img).into(chatUserImg);
                imgAdmin.setVisibility(View.VISIBLE);
                imgUser.setVisibility(View.VISIBLE);
                textAdminCard.setVisibility(View.GONE);
                textUserCard.setVisibility(View.GONE);
                cardUserAudio.setVisibility(View.GONE);
                cardAdminAudio.setVisibility(View.GONE);
                userPlayVideoBtn.setVisibility(View.INVISIBLE);
                adminPlayVideoBtn.setVisibility(View.INVISIBLE);
                cardAdminFile.setVisibility(View.GONE);
                cardUserFile.setVisibility(View.GONE);
                cardAdminContact.setVisibility(View.GONE);
                cardUserContact.setVisibility(View.GONE);
            } else if (msg_type.equalsIgnoreCase("video")) {
                imgAdmin.setVisibility(View.VISIBLE);
                imgUser.setVisibility(View.VISIBLE);
                textAdminCard.setVisibility(View.GONE);
                textUserCard.setVisibility(View.GONE);
                cardUserAudio.setVisibility(View.GONE);
                cardAdminAudio.setVisibility(View.GONE);
                cardAdminFile.setVisibility(View.GONE);
                cardUserFile.setVisibility(View.GONE);
                cardAdminContact.setVisibility(View.GONE);
                cardUserContact.setVisibility(View.GONE);

                userPlayVideoBtn.setVisibility(View.VISIBLE);
                adminPlayVideoBtn.setVisibility(View.VISIBLE);

                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Catholix/" + msg_name);
                if (file.exists()) {
                    Glide.with(context).load(file.getAbsolutePath()).into(chatAdminImg);
                    Glide.with(context).load(file.getAbsolutePath()).into(chatUserImg);
                    userPlayVideoBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                    adminPlayVideoBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                } else {
                    Glide.with(context).load(img).into(chatAdminImg);
                    Glide.with(context).load(img).into(chatUserImg);
                    userPlayVideoBtn.setImageResource(R.drawable.ic_file_download_white_24dp);
                    adminPlayVideoBtn.setImageResource(R.drawable.ic_file_download_white_24dp);
                }

            } else if (msg_type.equalsIgnoreCase("file")) {
                imgAdmin.setVisibility(View.GONE);
                imgUser.setVisibility(View.GONE);
                textAdminCard.setVisibility(View.GONE);
                textUserCard.setVisibility(View.GONE);
                cardUserAudio.setVisibility(View.GONE);
                cardAdminAudio.setVisibility(View.GONE);
                cardAdminFile.setVisibility(View.VISIBLE);
                cardUserFile.setVisibility(View.VISIBLE);
                cardAdminContact.setVisibility(View.GONE);
                cardUserContact.setVisibility(View.GONE);

                textUserFile.setText(msg_name);
                textAdminFile.setText(msg_name);

                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Catholix/" + msg_name);

                if (file.exists()) {
                    Glide.with(context).load(file.getAbsolutePath()).placeholder(R.drawable.ic_insert_drive_file_white_24dp).into(userFileImage);
                    Glide.with(context).load(file.getAbsolutePath()).placeholder(R.drawable.ic_insert_drive_file_white_24dp).into(adminFileImage);
                } else {
                    Glide.with(context).load(img).placeholder(R.drawable.ic_insert_drive_file_white_24dp).into(userFileImage);
                    Glide.with(context).load(img).placeholder(R.drawable.ic_insert_drive_file_white_24dp).into(adminFileImage);
                }
            } else if (msg_type.equalsIgnoreCase("contact")) {
                imgAdmin.setVisibility(View.GONE);
                imgUser.setVisibility(View.GONE);
                textAdminCard.setVisibility(View.GONE);
                textUserCard.setVisibility(View.GONE);
                cardUserAudio.setVisibility(View.GONE);
                cardAdminAudio.setVisibility(View.GONE);
                cardAdminFile.setVisibility(View.GONE);
                cardUserFile.setVisibility(View.GONE);
                cardAdminContact.setVisibility(View.VISIBLE);
                cardUserContact.setVisibility(View.VISIBLE);

                textUserContact.setText(msg_name);
                textAdminContact.setText(msg_name);

            } else {
                textAdminCard.setVisibility(View.GONE);
                textUserCard.setVisibility(View.GONE);
                imgAdmin.setVisibility(View.GONE);
                imgUser.setVisibility(View.GONE);
                cardUserAudio.setVisibility(View.VISIBLE);
                cardAdminAudio.setVisibility(View.VISIBLE);
                cardAdminFile.setVisibility(View.GONE);
                cardUserFile.setVisibility(View.GONE);
                cardAdminContact.setVisibility(View.GONE);
                cardUserContact.setVisibility(View.GONE);

                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Catholix/" + msg_name);
                if (file.exists()) {
                    userAudioPlayBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                    adminAudioPlayBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                } else {
                    userAudioPlayBtn.setImageResource(R.drawable.ic_file_download_white_24dp);
                    adminAudioPlayBtn.setImageResource(R.drawable.ic_file_download_white_24dp);
                }
            }
        }

        private void viewImg(byte[] thumb_byte, ImageView imgAdmin, String username, long time, String img) {

            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context,
                    imgAdmin, "image");
            context.startActivity(new Intent(context, FileViewer.class).putExtra("username", username)
                    .putExtra("photo", thumb_byte).putExtra("time", time).putExtra("img_url", img).putExtra("type", "image"), optionsCompat.toBundle());
        }

        private void downloadAudio(String file, String msgBody) throws NullPointerException {
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

        private void deleteMsgImage(String key, String from, String to, String msg_name, int position) {
            chatsView.startProgress(View.VISIBLE);
            FirebaseDatabase.getInstance().getReference().child("messages").child(from).child(to).child(key).removeValue().addOnSuccessListener(aVoid -> {
                FirebaseDatabase.getInstance().getReference().child("messages").child(to).child(from).child(key).removeValue().addOnSuccessListener(aVoid1 -> {
                    StorageReference thumbRef = FirebaseStorage.getInstance().getReference().child("message_images").child(msg_name);
                    thumbRef.delete().addOnCompleteListener(task -> {
                        chatsView.startProgress(View.GONE);
                        chatsView.reload();
                    });
                });
            });
        }

        private void deleteMsgText(String key, String from, String to, String msg_name, int position) {
            chatsView.startProgress(View.VISIBLE);
            FirebaseDatabase.getInstance().getReference().child("messages").child(from).child(to).child(key).removeValue().addOnSuccessListener(aVoid -> {
                FirebaseDatabase.getInstance().getReference().child("messages").child(to).child(from).child(key).removeValue().addOnSuccessListener(aVoid1 -> {
                    chatsView.startProgress(View.GONE);
                    chatsView.reload();
                });
            });
        }

        private void deleteMsgAudio(String key, String from, String to, String msg_name, int position) {
            chatsView.startProgress(View.VISIBLE);
            FirebaseDatabase.getInstance().getReference().child("messages").child(from).child(to).child(key).removeValue().addOnSuccessListener(aVoid -> {
                FirebaseDatabase.getInstance().getReference().child("messages").child(to).child(from).child(key).removeValue().addOnSuccessListener(aVoid1 -> {
                    StorageReference thumbRef = FirebaseStorage.getInstance().getReference().child("message_audio").child(msg_name);
                    thumbRef.delete().addOnCompleteListener(task -> {
                        chatsView.startProgress(View.GONE);
                        chatsView.reload();
                    });
                });
            });
        }

        private void viewVideo(String filePath, String fromUsername, long time_stamp, String userPhoto) {
            context.startActivity(new Intent(context, FileViewer.class).putExtra("username", fromUsername)
                    .putExtra("file", filePath).putExtra("time", time_stamp).putExtra("img_url", userPhoto).putExtra("type", "video"));
        }

        private void downloadVideo(String file, String msgBody) {
            userVideoBar.setVisibility(View.VISIBLE);
            adminVideoBar.setVisibility(View.VISIBLE);
            userPlayVideoBtn.setVisibility(View.INVISIBLE);
            adminPlayVideoBtn.setVisibility(View.INVISIBLE);
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(msgBody));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir("/Catholix", file);
            downloadManager.enqueue(request);
            BroadcastReceiver onComplete = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    try {
                        userVideoBar.setVisibility(View.INVISIBLE);
                        adminVideoBar.setVisibility(View.INVISIBLE);
                        userPlayVideoBtn.setVisibility(View.VISIBLE);
                        adminPlayVideoBtn.setVisibility(View.VISIBLE);
                        userPlayVideoBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                        adminPlayVideoBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                    } catch (Exception e) {
                    }
                }
            };
            context.getApplicationContext().registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }
    }

}
