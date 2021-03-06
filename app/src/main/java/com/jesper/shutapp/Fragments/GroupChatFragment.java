package com.jesper.shutapp.Fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jesper.shutapp.Activities.GroupInChatActivity;
import com.jesper.shutapp.R;
import com.jesper.shutapp.GroupInviteAdapter;
import com.jesper.shutapp.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupChatFragment extends Fragment {

    private ListView listView;
    private ArrayList<User> friendListGroup;
    private FirebaseUser fuser;
    private GroupInviteAdapter groupInviteAdapter;
    private Toolbar mToolbar;
    private DatabaseReference reference;
    private ArrayList<String> groupUsers;
    private EditText groupName;
    private String user;
    private String stringGroupName;
    private RelativeLayout relativeLayout;
    public ArrayList<String> userPictures;
    Intent intent;


    public GroupChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_chat, container, false);

        setHasOptionsMenu(true);
        init(view);
        getUsersFromFB();
        generateFriendList();

        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
            }
        });

        return view;
    }

    //Initiate all variables and views.
    private void init (View view) {
        groupUsers = new ArrayList<>();
        friendListGroup = new ArrayList<>();
        userPictures = new ArrayList<>();
        groupName = view.findViewById(R.id.edittext_groupchat);
        listView = view.findViewById(R.id.listview_friends_groupchat);
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        mToolbar = view.findViewById(R.id.include_toolbar_groupchat);
        relativeLayout = view.findViewById(R.id.layout_addgroupmembers);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        groupUsers.add(fuser.getUid());
        mToolbar.setTitle("");
    }

    //Inflate our toolbar.
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.groupchat_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //Switch case for toolbar.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.groupchat_check:
                stringGroupName = groupName.getText().toString();
                if (!stringGroupName.equals("")) {
                    addGroupToDatabase();
                    removeOldGroup();
                    addGroupNameToUsers();
                    startGroupChatActivity();
                }   else {
                    Toast.makeText(getActivity(), "You need to choose a Group name", Toast.LENGTH_SHORT).show();
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Adds the group to database.
    private void addGroupToDatabase() {

        reference = FirebaseDatabase.getInstance().getReference("users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    for (int i = 0; i < groupUsers.size() ; i++) {

                        if (snapshot.getKey().equals(groupUsers.get(i))) {
                            User user = snapshot.getValue(User.class);
                            DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference("groups");
                            groupRef.child(stringGroupName).child("members").child(user.getUid()).setValue(user);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //Gets the group-users from FireBase.
    private void getUsersFromFB() {

        stringGroupName = groupName.getText().toString();
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("groups").child(fuser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    user = snapshot.getKey();
                    groupUsers.add(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //Removes the old group from FireBase.
    private void removeOldGroup() {
        reference = FirebaseDatabase.getInstance().getReference("groups").child(fuser.getUid());
        reference.removeValue();
    }

    //Generate all user to the list.
    private void generateFriendList() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(fuser.getUid()).child("friends");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    friendListGroup.add(user);
                }
                groupInviteAdapter = new GroupInviteAdapter(getActivity(), friendListGroup);
                listView.setAdapter(groupInviteAdapter);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    //Adds group-names into Users profiles.
    private void addGroupNameToUsers(){
        reference = FirebaseDatabase.getInstance().getReference("users");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (int i = 0; i < groupUsers.size(); i++) {
                        if (snapshot.getKey().equals(groupUsers.get(i))){
                            reference.child(snapshot.getKey()).child("groups").child(stringGroupName).setValue(stringGroupName);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //Calls the GroupChatActivity.
    private void startGroupChatActivity (){
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("groups").child(stringGroupName).child("members");
        reference2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userPictures.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    userPictures.add(user.getProfile_picture());
                }

                intent = new Intent (getActivity(), GroupInChatActivity.class);
                intent.putExtra("groupname", stringGroupName);
                intent.putExtra("grouplist", groupUsers);
                intent.putStringArrayListExtra("pictures", userPictures);

                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //Method that hides the keyboard.
    private void hideKeyboard (){
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }
    private void addGroupsToUsers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");

        for (int i = 0; i < groupUsers.size() ; i++) {
            reference.child(groupUsers.get(i)).child("groups").child(stringGroupName).setValue(stringGroupName);
            Log.d("ANTON", groupUsers.get(i));

        }
        reference.child(fuser.getUid()).child("groups").child(stringGroupName).setValue(stringGroupName);
    }

}
