package adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import config.SharedPref;
import de.hdodenhof.circleimageview.CircleImageView;
import model.ChatDao;
import model.Conv;
import www.catholix.com.ng.ChatActivity;
import www.catholix.com.ng.R;

public class ChatHistoryAdapter extends RecyclerView.Adapter<ChatHistoryAdapter.viewHolder>{

    private List<String> list;
    private List<Conv> convs;
    private Context context;

    public ChatHistoryAdapter(List<String> list, List<Conv> convs, Context context) {
        this.list = list;
        this.convs = convs;
        this.context = context;
    }


    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChatHistoryAdapter.viewHolder(LayoutInflater.from(context).inflate(R.layout.user_chat_history_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        String id = list.get(position);
        Conv conv = convs.get(position);
        final String[] username = new String[1];
        final String[] img_url = new String[1];
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference().child("messages").child(SharedPref.getInstance(context).getId()).child(id);
        Query messageQuery = chatRef.limitToLast(1);
        DatabaseReference mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(id);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if(dataSnapshot.exists()) {
                    ChatDao message = dataSnapshot.getValue(ChatDao.class);
                    if(message.getFrom().equals(SharedPref.getInstance(context).getId())) {
                        holder.textUsername.setText(message.getToUsername());
                        username[0] = message.getToUsername();
                    }
                    else {
                        holder.textUsername.setText(message.getFromUsername());
                        username[0] = message.getToUsername();
                    }
                    if(conv.isSeen())
                        holder.textSeen.setVisibility(View.GONE);
                    else{
                        holder.textSeen.setText("new");
                        holder.textSeen.setVisibility(View.VISIBLE);
                    }
                    if(message.getMsg_type().equalsIgnoreCase("image"))
                        holder.textMessage.setText("image");
                    else if(message.getMsg_type().equalsIgnoreCase("audio"))
                        holder.textMessage.setText("audio");
                    else
                    holder.textMessage.setText(message.getMsg_body());
                    holder.textTime.setText(DateUtils.getRelativeTimeSpanString(message.getTime_stamp()));
                }else{
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, list.size());
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                img_url[0] = dataSnapshot.child("img_url").getValue(String.class);
                Glide.with(context).load(dataSnapshot.child("img_url").getValue(String.class)).placeholder(R.drawable.ic_person_profile_24dp).into(holder.circleImageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.linearLayout.setOnClickListener( view ->
            holder.startChat(id, username[0], img_url[0])
        );
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class viewHolder extends RecyclerView.ViewHolder{

        TextView textUsername, textMessage, textTime, textSeen;
        CircleImageView circleImageView;
        LinearLayout linearLayout;

        private viewHolder(@NonNull View itemView) {
            super(itemView);
            textUsername = itemView.findViewById(R.id.user_chat_history_text_username);
            textMessage = itemView.findViewById(R.id.user_chat_history_text_single_message);
            textTime = itemView.findViewById(R.id.user_chat_history_text_time);
            textSeen = itemView.findViewById(R.id.user_chat_history_text_new_msg);
            circleImageView = itemView.findViewById(R.id.user_chat_history_image_profile);
            linearLayout = itemView.findViewById(R.id.user_chat_history_linear_layout);
        }

        private void startChat(String id, String username, String img_url) {
            context.startActivity(new Intent(context, ChatActivity.class).putExtra("userID", id)
                    .putExtra("username", username).putExtra("img_url", img_url));
        }
    }
}