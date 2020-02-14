package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import api.VolleyInstance;
import config.NetworkConfig;
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

        holder.btnDecline.setOnClickListener(view -> holder.decline(model.getList().getPostId()));

        holder.btnAccept.setOnClickListener(view -> holder.accept(model.getList().getPostId()));

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class viewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView userTextName, textTime;
        Button btnAccept, btnDecline;

        private viewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.notification_layout_image_profile);
            userTextName = itemView.findViewById(R.id.notification_layout_username);
            textTime = itemView.findViewById(R.id.notification_layout_time);
            btnAccept = itemView.findViewById(R.id.notification_layout_accept_btn);
            btnDecline = itemView.findViewById(R.id.notification_layout_decline_btn);
        }

        private void decline(String postId) {
            btnAccept.setAlpha(0.5f);
            btnAccept.setEnabled(false);
            btnDecline.setEnabled(false);
            btnDecline.setAlpha(0.5f);
            btnDecline.setText("Declining..");
            if (NetworkConfig.getInstance(context).networkAvailable()) {
                list.clear();
                HashMap<String, Object> map = new HashMap<>();
                map.put("req", "decline_friend_request");
                map.put("frndReqId", postId);
                JSONObject params = new JSONObject(map);
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "https://catholix.com.ng/api.developer/POST/req.php", params,
                        response -> {
                            try {
                                if(response.getString("message").equalsIgnoreCase("Friend request declined")){
                                    Toast.makeText(context, response.getString("message"), Toast.LENGTH_SHORT).show();
                                    notifyItemRemoved(getAdapterPosition());
                                    notifyItemRangeChanged(getAdapterPosition(), list.size());
                                }else{
                                    btnAccept.setAlpha(1f);
                                    btnAccept.setEnabled(true);
                                    btnDecline.setEnabled(true);
                                    btnDecline.setAlpha(1f);
                                    btnDecline.setText("Decline");
                                    Toast.makeText(context, "an error occurred", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        },
                        error -> {
                            btnAccept.setAlpha(1f);
                            btnAccept.setEnabled(true);
                            btnDecline.setEnabled(true);
                            btnDecline.setAlpha(1f);
                            btnDecline.setText("Decline");
                            Toast.makeText(context, "an error occurred", Toast.LENGTH_SHORT).show();
                        }
                ) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("Content-Type", "application/json");
                        return params;
                    }

                    @Override
                    public String getBodyContentType() {
                        return "application/json";
                    }

                };
                VolleyInstance.getInstance(context).addToQueue(request);
            } else {
                Toast.makeText(context, "no internet connection", Toast.LENGTH_SHORT).show();
            }
        }

        private void accept(String postId) {
            if (NetworkConfig.getInstance(context).networkAvailable()) {
                btnAccept.setAlpha(0.5f);
                btnAccept.setEnabled(false);
                btnDecline.setEnabled(false);
                btnDecline.setAlpha(0.5f);
                btnAccept.setText("Accepting..");
                HashMap<String, Object> map = new HashMap<>();
                map.put("req", "accept_friend_request");
                map.put("frndReqId", postId);
                JSONObject params = new JSONObject(map);
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "https://catholix.com.ng/api.developer/POST/req.php", params,
                        response -> {
                            try {
                                if(response.getString("message").equalsIgnoreCase("Friend request accepted")){
                                    Toast.makeText(context, response.getString("message"), Toast.LENGTH_SHORT).show();
                                    notifyItemRemoved(getAdapterPosition());
                                    notifyItemRangeChanged(getAdapterPosition(), list.size());
                                }else{
                                    btnAccept.setAlpha(1f);
                                    btnAccept.setEnabled(true);
                                    btnDecline.setEnabled(true);
                                    btnDecline.setAlpha(1f);
                                    btnAccept.setText("Accept");
                                    Toast.makeText(context, "an error occurred", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        },
                        error -> {
                            btnAccept.setAlpha(1f);
                            btnAccept.setEnabled(true);
                            btnDecline.setEnabled(true);
                            btnDecline.setAlpha(1f);
                            btnAccept.setText("Accept");
                            Toast.makeText(context, "an error occurred", Toast.LENGTH_SHORT).show();
                        }
                ) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("Content-Type", "application/json");
                        return params;
                    }

                    @Override
                    public String getBodyContentType() {
                        return "application/json";
                    }

                };
                VolleyInstance.getInstance(context).addToQueue(request);
            } else {
                Toast.makeText(context, "no internet connection", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
