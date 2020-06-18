package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import model.Users;
import view.SelectUserView;
import www.catholix.com.ng.R;

public class AddNewUserAdapter extends RecyclerView.Adapter<AddNewUserAdapter.viewHolder>{

    private Context context;
    private List<Users> list;
    private SelectUserView view;

    public AddNewUserAdapter(Context context, List<Users> list, SelectUserView view) {
        this.context = context;
        this.list = list;
        this.view = view;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AddNewUserAdapter.viewHolder(LayoutInflater.from(context).inflate(R.layout.add_new_users_list_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Users users = list.get(position);
        holder.textUser.setText(holder.getName(users.getFirstname(), users.getSurname()));

        Glide.with(context).load("https://www.catholix.com.ng/files/images/profilepics/" + users.getPhoto()).placeholder(R.drawable.ic_person_profile_24dp).into(holder.circleImageView);

        if(view.getUsers().contains(users.getUserId()))
            holder.box.setImageResource(R.drawable.ic_check_circle_blue_24dp);
        else
            holder.box.setImageResource(R.drawable.ic_radio_button_unchecked_blue_24dp);

        holder.cardView.setOnClickListener(view1 -> {
            if(view.getUsers().contains(users.getUserId())){
                view.removeUser(users.getUserId());
                holder.box.setImageResource(R.drawable.ic_radio_button_unchecked_blue_24dp);
            }else{
                view.selectUser(users.getUserId());
                holder.box.setImageResource(R.drawable.ic_check_circle_blue_24dp);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class viewHolder extends RecyclerView.ViewHolder{

        CircleImageView circleImageView;
        TextView textUser;
        ImageButton box;
        CardView cardView;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            circleImageView = itemView.findViewById(R.id.add_new_users_list_layout_image_profile);
            textUser = itemView.findViewById(R.id.add_new_users_list_layout_username);
            box = itemView.findViewById(R.id.add_new_users_list_layout_check_box);
            cardView = itemView.findViewById(R.id.users_list_add_new_layout_card);
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
