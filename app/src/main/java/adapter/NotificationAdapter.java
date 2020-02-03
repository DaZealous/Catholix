package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import model.NotificationWithUserModel;
import www.catholix.com.ng.R;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.viewHolder> {

    private Context context;
    private List<NotificationWithUserModel> list;

    public NotificationAdapter(Context context, List<NotificationWithUserModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public NotificationAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notification_layout, parent, false);
        return new NotificationAdapter.viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.viewHolder holder, int position) {
        NotificationWithUserModel model = list.get(position);
        holder.userTextName.setText(model.getUsername());
        holder.textTime.setText(model.getList().getDate());
        Glide.with(context).load("https://www.catholix.com.ng/files/images/profilepics/" + model.getImgUrl()).placeholder(R.drawable.ic_person_profile_24dp).into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class viewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        TextView userTextName, textTime;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.notification_layout_image_profile);
            userTextName = itemView.findViewById(R.id.notification_layout_username);
            textTime = itemView.findViewById(R.id.notification_layout_time);
        }
    }
}
