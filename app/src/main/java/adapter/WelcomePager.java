package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import www.catholix.com.ng.R;

public class WelcomePager extends PagerAdapter {
    private LayoutInflater inflater;
    private Context context;
    public WelcomePager(Context context) {
        this.context = context;
    }

    private String[] authors = {"Hans Urs von Balthasar, Prayer", "Fulton J. Sheen", "Peter Kreeft"};
    private String[] quotes = {"“What you are is God's gift to you, what you become is your gift to God.”",
            "“Criticism of others is thus an oblique form of self-commendation. We think we make the picture hang straight on our wall by telling our neighbors that all his pictures are crooked.”",
            "“We sinned for no reason but an incomprehensible lack of love, and He saved us for no reason but an incomprehensible excess of love.”"
    };

    @Override
    public int getCount() {
        return authors.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.welcome_pager, container, false);
        TextView author = view.findViewById(R.id.welcome_text_author);
        TextView quote = view.findViewById(R.id.welcome_text_quote);
        author.setText(authors[position]);
        quote.setText(quotes[position]);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
