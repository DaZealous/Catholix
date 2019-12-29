package adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import model.Users;
import www.catholix.com.ng.FriendsProfile;
import www.catholix.com.ng.R;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.viewHolder>{
    private Context context;
    private List<Users> list;

    public UsersAdapter(Context context, List<Users> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.users_list_layout, parent, false);
        return new UsersAdapter.viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final viewHolder holder, int position) {
        final Users users = list.get(position);
        holder.textView.setText(holder.getName(users.getFirstname(), users.getSurname()));
        Glide.with(context).load("https://www.catholix.com.ng/files/images/profilepics/"+users.getPhoto()).placeholder(R.drawable.ic_person_profile_24dp).into(holder.imageView);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context,
                        holder.imageView, "profileImg");
                context.startActivity(new Intent(context, FriendsProfile.class)
                        .putExtra("id", users.getUserId())
                        .putExtra("fullname", holder.getName(users.getFirstname(), users.getSurname()))
                .putExtra("username", holder.getOtherName(users.getUsername()))
                .putExtra("photo", holder.getOtherName(users.getPhoto()))
                .putExtra("phone", holder.getOtherName(users.getPhone()))
                .putExtra("country", holder.getOtherName(users.getCountry()))
                .putExtra("email", holder.getOtherName(users.getEmail()))
                        .putExtra("state", holder.getOtherName(users.getState()))
                        .putExtra("gender", holder.getOtherName(users.getGender())),
                        optionsCompat.toBundle());
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class viewHolder extends RecyclerView.ViewHolder{

        CircleImageView imageView;
        TextView textView;
        CardView cardView;

        private viewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.users_list_layout_image_profile);
            textView = itemView.findViewById(R.id.users_list_layout_username);
            cardView = itemView.findViewById(R.id.users_list_layout_card);
        }

        private String getName(String first, String last) {
            if(first != null && last != null){
                return first+" "+last;
            }
            if(first == null && last == null){
                return "";
            }
            if(first != null){
                return first;
            }
            return last;
        }

        private   String getOtherName(String name){
            if(name != null)
                return name;
                return "";
        }
    }
}
