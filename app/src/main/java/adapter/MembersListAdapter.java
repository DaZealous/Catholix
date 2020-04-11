package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import model.Users;
import view.GroupChatSettingsView;
import www.catholix.com.ng.R;

public class MembersListAdapter extends RecyclerView.Adapter<MembersListAdapter.viewHolder>{

    private List<Users> list;
    private Context context;
    private GroupChatSettingsView view;

    public MembersListAdapter(List<Users> list, Context context, GroupChatSettingsView view) {
        this.list = list;
        this.context = context;
        this.view = view;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.group_members_list, parent, false);
        return new MembersListAdapter.viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Users users = list.get(position);
        holder.username.setText(holder.getName(users.getFirstname(), users.getSurname()));
        Glide.with(context).load("https://www.catholix.com.ng/files/images/profilepics/" + users.getPhoto()).placeholder(R.drawable.ic_person_profile_24dp).into(holder.circleImageView);

        if(view.getId().equals(users.getUserId()))
            holder.img.setVisibility(View.VISIBLE);
        else
            holder.img.setVisibility(View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class viewHolder extends RecyclerView.ViewHolder{

        TextView username;
        CircleImageView circleImageView;
        ImageButton img;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.group_members_list_layout_username);
            circleImageView = itemView.findViewById(R.id.group_members_list_layout_image_profile);
            img = itemView.findViewById(R.id.group_members_list_layout_check_box);
        }

        private String getName(String first, String last) {
            if (first != null && last != null) {
                return first + " " + last;
            }
            if (first == null && last == null) {
                return "";
            }
            if (first != null) {
                return first;
            }
            return last;
        }

    }
}
