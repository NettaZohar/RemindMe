package com.example.remindMe.Notifications;


import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.remindMe.R;

import java.util.List;

/**
 * a adapter class for the notification fragment recycle view.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.MyViewHolder> {

    private final List<NotificationObject> notificationsList;

    /** constructor **/
    public NotificationAdapter(List<NotificationObject> list) {
        this.notificationsList = list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        holder.bind(notificationsList.get(position));

    }

    @Override
    public int getItemCount() {
        Log.d("NOTIFICATION_LIST_SIZE", "the size is: " + notificationsList.size());
        return notificationsList.size();
    }

    /**
     * a local static class which uses as a ViewHolder for the comments RecyclerViewer
     */
    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView text, date;
        private ImageView icon;

        /** constructor **/
        public MyViewHolder(View view) {
            super(view);
            icon = (ImageView) view.findViewById(R.id.image_checked);
            text = (TextView) view.findViewById(R.id.comment_notif);
            date = (TextView) view.findViewById(R.id.time_notif);
        }

        /**
         * this function binds each comment and set it's content in place.
         * @param not - the comment to bind
         */
        public void bind(final NotificationObject not) {
            text.setText(not.getText());
            date.setText(not.getTime());
            icon.setImageResource(R.drawable.checked);
        }
    }

}


