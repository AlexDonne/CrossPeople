package com.example.extra.socialproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.extra.socialproject.model.ArrayAdapter;
import com.example.extra.socialproject.model.RecyclerItemClickListener;
import com.example.extra.socialproject.model.User;
import com.facebook.Profile;
import com.facebook.login.widget.ProfilePictureView;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {
    private ArrayList<String> list_of_meets_name = new ArrayList<>();
    private ArrayList<String> list_of_meets_uri = new ArrayList<>();

    FirebaseUser user;
    FirebaseAuth.AuthStateListener authListener;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ProfilePictureView profilePhoto;
        RecyclerView recyclerView;

        final RecyclerView.Adapter adapter;

        super.onCreate(savedInstanceState);
        auth=FirebaseAuth.getInstance();
        setContentView(R.layout.activity_profile);

        ArrayList<User> list_users = getIntent().getExtras().getParcelableArrayList("list_users");

        Profile profile = Profile.getCurrentProfile();

        TextView nom=(TextView)findViewById(R.id.user_profile_name);

        profilePhoto = (ProfilePictureView)findViewById(R.id.user_profile_photo);
        profilePhoto.setPresetSize(ProfilePictureView.LARGE);

        user = auth.getCurrentUser();

        nom.setText(profile.getName());
        profilePhoto.setProfileId(profile.getId());


        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse("https://www.hugoextrat.com"))
                .setQuote("J'utilise une nouvelle application qui me permet de trouver des personnes à proximité. C'est génial !!")
                .build();
        ShareButton shareButton = (ShareButton)findViewById(R.id.shareButton);
        shareButton.setShareContent(content);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ArrayAdapter(list_of_meets_name);
        recyclerView.setAdapter(adapter);

        authListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if(user != null){
                    String uid = user.getUid();
                    Log.i("userInfo",uid);
                }
                else {
                    Log.i("userInfo","no");
                }
            }
        };

        for(User u : list_users) {
            list_of_meets_name.add(u.getName());
            list_of_meets_uri.add(u.getLinkURI());
        }

        RecyclerItemClickListener.addTo(recyclerView).setOnItemClickListener(new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView rView, int position, View view) {
                Log.d("TEST", list_of_meets_uri.get(position));
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(list_of_meets_uri.get(position))));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_profil, menu);
        setTitle(R.string.menu_profil_text);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_maps:
                startActivity(new Intent(ProfileActivity.this, MapsActivity.class));
                break;
            case R.id.action_main :
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
