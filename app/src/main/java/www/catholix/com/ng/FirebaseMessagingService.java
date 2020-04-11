package www.catholix.com.ng;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import config.NotifyChannel;

import static config.NotifyChannel.ANDROID_CHANNEL_ID;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String, String> data = remoteMessage.getData();

        String from = data.get("fromUserId");
        String title = data.get("title");
        String msg_body = data.get("body");
        String icon = data.get("icon");
        String click_action = data.get("click_action");
        String msg_type = data.get("type");

        Intent intent = new Intent(click_action);
        intent.putExtra("userID", from)
                .putExtra("username", title).putExtra("img_url", icon);

        final Bitmap[] bitmap = {null};

        Glide.with(getApplicationContext())
                .asBitmap()
                .load(icon)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                        bitmap[0] = resource;
                        // TODO Do some work: pass this bitmap
                        PendingIntent pendingIntent = PendingIntent.getActivity(FirebaseMessagingService.this,
                                0,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                        int notificationId = (int) System.currentTimeMillis();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            if (msg_type.equalsIgnoreCase("text")) {
                                NotificationCompat.Builder nb = getAndroidChannelTextNotification(title, msg_body, ANDROID_CHANNEL_ID, bitmap[0]);
                                nb.setContentIntent(pendingIntent);
                                new NotifyChannel(FirebaseMessagingService.this).getManager().notify(notificationId, nb.build());
                            } else if (msg_type.equalsIgnoreCase("image")) {
                                Glide.with(getApplicationContext())
                                        .asBitmap()
                                        .load(msg_body)
                                        .into(new CustomTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                NotificationCompat.Builder nb = getAndroidChannelImageNotification(title, resource, ANDROID_CHANNEL_ID, bitmap[0]);
                                                nb.setContentIntent(pendingIntent);
                                                new NotifyChannel(FirebaseMessagingService.this).getManager().notify(notificationId, nb.build());
                                            }

                                            @Override
                                            public void onLoadCleared(@Nullable Drawable placeholder) {

                                            }
                                        });
                            } else if (msg_type.equalsIgnoreCase("audio")) {
                                NotificationCompat.Builder nb = getAndroidChannelTextNotification(title, "You received an audio file", ANDROID_CHANNEL_ID, bitmap[0]);
                                nb.setContentIntent(pendingIntent);
                                new NotifyChannel(FirebaseMessagingService.this).getManager().notify(notificationId, nb.build());
                            } else if (msg_type.equalsIgnoreCase("video")) {
                                NotificationCompat.Builder nb = getAndroidChannelTextNotification(title, "You received a video file", ANDROID_CHANNEL_ID, bitmap[0]);
                                nb.setContentIntent(pendingIntent);
                                new NotifyChannel(FirebaseMessagingService.this).getManager().notify(notificationId, nb.build());
                            } else {
                                NotificationCompat.Builder nb = getAndroidChannelTextNotification(title, "You received a document file", ANDROID_CHANNEL_ID, bitmap[0]);
                                nb.setContentIntent(pendingIntent);
                                new NotifyChannel(FirebaseMessagingService.this).getManager().notify(notificationId, nb.build());
                            }


                        } else {
                            if (msg_type.equalsIgnoreCase("text")) {
                                NotificationCompat.Builder mBuilder = getAndroidChannelTextNotification(title, msg_body, from, bitmap[0]);
                                mBuilder.setContentIntent(pendingIntent);
                                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                manager.notify(notificationId, mBuilder.build());
                            } else if (msg_type.equalsIgnoreCase("image")) {
                                Glide.with(getApplicationContext())
                                        .asBitmap()
                                        .load(msg_body)
                                        .into(new CustomTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                NotificationCompat.Builder mBuilder = getAndroidChannelImageNotification(title, resource, from, bitmap[0]);
                                                mBuilder.setContentIntent(pendingIntent);
                                                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                                manager.notify(notificationId, mBuilder.build());
                                            }

                                            @Override
                                            public void onLoadCleared(@Nullable Drawable placeholder) {

                                            }
                                        });
                            } else if (msg_type.equalsIgnoreCase("audio")) {
                                NotificationCompat.Builder mBuilder = getAndroidChannelTextNotification(title, "You received an audio file", from, bitmap[0]);
                                mBuilder.setContentIntent(pendingIntent);
                                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                manager.notify(notificationId, mBuilder.build());
                            } else if (msg_type.equalsIgnoreCase("video")) {
                                NotificationCompat.Builder mBuilder = getAndroidChannelTextNotification(title, "You received a video file", from, bitmap[0]);
                                mBuilder.setContentIntent(pendingIntent);
                                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                manager.notify(notificationId, mBuilder.build());
                            } else {
                                NotificationCompat.Builder mBuilder = getAndroidChannelTextNotification(title, "You received a document file", from, bitmap[0]);
                                mBuilder.setContentIntent(pendingIntent);
                                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                manager.notify(notificationId, mBuilder.build());
                            }
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });

    }

    public NotificationCompat.Builder getAndroidChannelImageNotification(String title, Bitmap body, String id, Bitmap bitmap) {
        return new NotificationCompat.Builder(this, id)
                .setSmallIcon(R.mipmap.app_logo)
                .setLargeIcon(bitmap)
                .setContentTitle(title)
                .setContentText("Image file received")
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setAutoCancel(true)
                .setStyle(
                        new NotificationCompat.BigPictureStyle().bigPicture(body))
                .setPriority(Notification.PRIORITY_HIGH);
    }

    public NotificationCompat.Builder getAndroidChannelTextNotification(String title, String body, String id, Bitmap bitmap) {
        return new NotificationCompat.Builder(this, id)
                .setSmallIcon(R.mipmap.app_logo)
                .setLargeIcon(bitmap)
                .setContentTitle(title)
                .setContentText(body)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setAutoCancel(true);
    }

}
