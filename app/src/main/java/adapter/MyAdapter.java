package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

import model.ContactModel;
import www.catholix.com.ng.R;

public class MyAdapter extends ArrayAdapter<ContactModel> {
    private Context context;
    private List<ContactModel> model;

    public MyAdapter(Context context, List<ContactModel> list) {
        super(context, R.layout.contacts_view, list);
        this.context = context;
        model = list;
    }

    @Override
    public int getCount() {
        return model.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        LayoutInflater inflater = LayoutInflater.from(context);
        v = inflater.inflate(R.layout.contacts_view, parent, false);
        TextView contactName = v.findViewById(R.id.contacts_view_name);
        TextView contactPhone = v.findViewById(R.id.contacts_view_phone);
        contactName.setText(model.get(position).name);
        contactPhone.setText(model.get(position).mobileNumber);
        return v;
    }

}
