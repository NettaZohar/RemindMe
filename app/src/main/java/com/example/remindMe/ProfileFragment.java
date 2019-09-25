package com.example.remindMe;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Profile fragment - contains user information from the Auth0 authentication and logout button
 */
public class ProfileFragment extends Fragment {

    private ImageView profileImg;
    private TextView UserName, emailString, userID;
    private Button logoutBtn;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment_layout, container, false);

        setAttributes(view);
        setUserInto();

        return view;
    }

    /**
     * this method updates the user information according to the Auth0 authentication info
     */
    private void setUserInto() {
        UserName.setText(MainActivity.curUser.getName());
        userID.setText(MainActivity.userId);
        emailString.setText(MainActivity.curUser.getEmail());
        Log.d("Profile", "user info is: " + MainActivity.userId
                + " " + MainActivity.curUser.getEmail() + " " + MainActivity.curUser.getName());

    }

    /**
     * this method get the attributes needed for this screen by their id as assigned in XML
     * @param view - View to get the objects from
     */
    private void setAttributes(View view) {
        UserName = (TextView) view.findViewById(R.id.user_first_name);
        userID = (TextView) view.findViewById(R.id.id_prof);
        emailString = (TextView) view.findViewById(R.id.profile_email);
        profileImg = (ImageView) view.findViewById(R.id.image);
        logoutBtn = (Button) view.findViewById(R.id.logout_button);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    /**
     * deals with the logout process for the currently authenticated user
     */
    private void logout() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.putExtra(LoginActivity.EXTRA_CLEAR_CREDENTIALS, true);
        startActivity(intent);
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();

    }
}
