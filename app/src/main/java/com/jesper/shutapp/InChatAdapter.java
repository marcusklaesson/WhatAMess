package com.jesper.shutapp;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jesper.shutapp.model.Chat;
import java.util.ArrayList;

public class InChatAdapter extends BaseAdapter {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    Context context;
    private ArrayList<Chat> chatList;
    FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

    public InChatAdapter(Context context, ArrayList<Chat> chatList) { //Constructor for InChatAdapter with the Context and our chatList.
        this.context = context;
        this.chatList = chatList;
    }

    @Override
    public int getCount() {
        return chatList.size();
    }

    @Override
    public Object getItem(int position) {
        return chatList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {
        TextView message;
        ImageView image;

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);

        ViewHolder holder = null;

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        holder = new ViewHolder();

        if (convertView == null) {
            if (type == 0) {
                convertView = mInflater.inflate(R.layout.chat_item_left, parent, false);
                holder.message = (TextView) convertView.findViewById(R.id.show_message);
                holder.image = convertView.findViewById(R.id.image_received);
                convertView.setTag(holder);
            } else {
                convertView = mInflater.inflate(R.layout.chat_item_right, parent, false);
                holder.message = (TextView) convertView.findViewById(R.id.show_message);
                holder.image = convertView.findViewById(R.id.image_sent);
                convertView.setTag(holder);
            }
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Chat chat_pos = chatList.get(position);
        if (chat_pos.getMessage().contains(".jpg") || chat_pos.getMessage().contains(".png")) {
            Glide.with(context).load(chat_pos.getMessage()).into(holder.image);
            Log.d("JesperChat", "getView: ");

        } else {
            holder.message.setText(chat_pos.getMessage());
        }
        return convertView;
    }

    //Method to check which sides of chat to put message.
    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        if (chatList.get(position).getSender().equals(fuser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}