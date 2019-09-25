package com.example.remindMe.Notifications;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.remindMe.MainActivity;
import com.example.remindMe.R;
import com.example.remindMe.ServerApi;
import com.example.remindMe.Users.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Notifications fragment - contain all the logs for previous notifications the user received
 */
public class NotificationFragment extends Fragment {

    private RecyclerView recyclerView;
    public static NotificationAdapter notificationAdapter;
    private List<NotificationObject> notificationList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        notificationList = MainActivity.curUser.getNotifications();
        if(notificationList == null){notificationList = new ArrayList<>();}

        handleNotificationsRecycle(view);

        final User[] userArr = new User[1];
        ServerApi.getInstance().getNotificationsList(userArr, notificationList, notificationAdapter);

        return view;
    }


    /**
     * this func handles the recycle viewer which displays the notifications
     * @param view - the view where the RecyclerView is in
     */
    private void handleNotificationsRecycle(View view) {

        recyclerView = view.findViewById(R.id.recycler_view_notif);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        notificationAdapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(notificationAdapter);

        notificationAdapter.notifyDataSetChanged();
    }

}