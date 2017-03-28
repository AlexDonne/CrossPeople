package com.example.extra.socialproject;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.example.extra.socialproject.model.RoomCreator;
import com.example.extra.socialproject.model.RecyclerItemClickListener;
import com.example.extra.socialproject.model.User;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Activité principale permettant d'afficher la liste des salles de chat et sauvegarder
 * la liste des utilisateurs qu'on a déjà croisé
 */
public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private DatabaseReference mDataBase;

    private String userId;
    private List<String> userIds;

    private GeoQuery geoQuery;
    private LocationManager locationManager;
    private String provider;
    private GeoFire geoFire;

    private List<User> mUsers;

    private ValueEventListener mUserValueListener;

    private ValueEventListener mChatRoomEventListener;
    private ArrayList<String> list_of_rooms1 = new ArrayList<>();
    private ArrayList<String> list_of_rooms2 = new ArrayList<>();

    private String name;
    private DatabaseReference root = FirebaseDatabase.getInstance().getReference().child("chatConversation");

    private RecyclerView.Adapter adapter;

    private RoomCreator addRoom;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RecyclerView recyclerView;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        }

        launchMainActivity();
        prepareGps();

        mUsers = new ArrayList<>();
        setUpUsers();
        setUpFirebase();
        setUpListeners();

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new com.example.extra.socialproject.model.ArrayAdapter(list_of_rooms1);
        recyclerView.setAdapter(adapter);

        name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        mDataBase.child("chatConversation").addValueEventListener(mChatRoomEventListener);

        root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Set<String> set = new HashSet<>();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    if (child.getKey().contains(name)) {
                        set.add(child.getKey());
                    }
                }

                String[] stringArray = new String[set.size()];
                list_of_rooms1.clear();
                list_of_rooms2.clear();
                for(String str : set.toArray(stringArray)){
                    String[] parts = str.split(" - ");
                    if(!parts[0].contains(name)) {
                        list_of_rooms1.add(parts[0]);
                        list_of_rooms2.add(str);
                    }
                    else {
                        list_of_rooms1.add(parts[1]);
                        list_of_rooms2.add(str);
                    }
                }
                //list_of_rooms.clear();
                //list_of_rooms.addAll(set);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        RecyclerItemClickListener.addTo(recyclerView).setOnItemClickListener(new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView rView, int position, View view) {
                Intent intent = new Intent(getApplicationContext(), ChatRoom.class);
                intent.putExtra("room_name", list_of_rooms2.get(position));
                intent.putExtra("user_name", name);
                view.getContext().startActivity(intent);
            }
        });
    }

    /**
     * Remplit la liste mUsers avec le fichier users qui contient la liste des users déjà croisés (pour ne pas refaire la salle de chat
     */
    @SuppressWarnings("unchecked")
    void setUpUsers() {
        try {
            FileInputStream fis = openFileInput("users");
            ObjectInputStream reader = new ObjectInputStream(fis);
            try {
                mUsers.addAll((List<User>) reader.readObject());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            fis.close();
            for (User u : mUsers) {
                //addRoom = new RoomCreator(name,u.getName());
                //root.updateChildren(addRoom.getMapRoom());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Création de la Geoquery (sert à être notifier quand 2 personnes sont dans un même rayon défini)
     * onKeyEntered appelé quand un user est à bonne distance
     * On modifie la query à chaque changement de localisation du user
     */
    private void prepareGps() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(provider, 60000, 20, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    geoFire.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    geoQuery = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), 1);
                    geoQuery.removeAllListeners();
                    geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                        @Override
                        public void onKeyEntered(String key, GeoLocation location) {
                            if (!key.equals(userId)) {

                                    userIds.add(key);

                            }
                        }

                        @Override
                        public void onGeoQueryReady() {
                            for (String userId : userIds) {
                                addUserListener(userId);
                            }
                        }

                        @Override
                        public void onKeyExited(String key) {

                        }

                        @Override
                        public void onKeyMoved(String key, GeoLocation location) {

                        }

                        @Override
                        public void onGeoQueryError(DatabaseError databaseError) {

                        }

                        private void addUserListener(String userid) {
                            mDataBase.child("users").child(userid).addValueEventListener(mUserValueListener);
                        }

                    });
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                /**
                 * Gps activé
                 * @param provider
                 */
                @Override
                public void onProviderEnabled(String provider) {
                    try{
                        Location lastKnown= locationManager.getLastKnownLocation(provider);
                        if (lastKnown!= null){
                            geoFire.setLocation(userId, new GeoLocation(lastKnown.getLatitude(),lastKnown.getLongitude()));
                        }
                    }
                    catch (SecurityException e){
                        e.printStackTrace();
                    }
                }

                /**
                 * Gps désactivé
                 * @param provider
                 */
                @Override
                public void onProviderDisabled(String provider) {
                    geoFire.removeLocation(userId);
                }
            });
        }
    }


    /**
     * Prépare les attributs en rapport avec Firebase
     */
    private void setUpFirebase() {
        mDataBase = FirebaseDatabase.getInstance().getReference();
        geoFire = new GeoFire(mDataBase.child("geofire"));
        auth = FirebaseAuth.getInstance();
        userIds = new ArrayList<>();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();
        mDataBase.child("users").child(userId).setValue(new User(userId, user.getDisplayName(), Profile.getCurrentProfile().getLinkUri().toString()));
    }


    /**
     * Créer les eventListener affectés sur des DatabaseReference, un pour chaque changement/ajout d'une conv, un à chaque changement
     * du user qui est entré dans la zone de proximité
     */
    @SuppressWarnings("unchecked")
    private void setUpListeners() {
        mUserValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User u = dataSnapshot.getValue(User.class);

                if (!mUsers.contains(u) && u != null) {
                    createNotification(u.getName());
                    mUsers.add(u);
                    addRoom = new RoomCreator(name, u.getName());
                    root.updateChildren(addRoom.getMapRoom());
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mChatRoomEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int nbOccur = 0;
                String aSup = "";
                if (dataSnapshot.getValue() != null) {
                    {
                        Map<String, String> map = (Map) dataSnapshot.getValue();
                        for (String s : map.keySet()) {
                            if(mUsers.size()>0){
                                if (s.contains(name) && s.contains(mUsers.get(mUsers.size()-1).getName())) {
                                    nbOccur++;
                                    aSup = s;
                                }
                            }
                            else if(s.contains(name)){
                                aSup = s;
                            }

                        }

                        if (nbOccur > 1) {
                            dataSnapshot.child(aSup).getRef().removeValue();
                        }

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        setTitle(R.string.menu_chat_text);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_deconnexion:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.btn_log_out)
                        .setMessage(R.string.confirm_log_out)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                geoFire.removeLocation(userId);
                                auth.signOut();
                                LoginManager.getInstance().logOut();
                                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                break;
            case R.id.action_profil:
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.putParcelableArrayListExtra("list_users", (ArrayList<User>) mUsers);
                startActivity(intent);
                break;
            case R.id.action_maps:
                startActivity(new Intent(MainActivity.this, MapsActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @Nullable String permissions[], @Nullable int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    prepareGps();
                } else {
                }
                return;
            }
        }
    }

    public void launchMainActivity() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setCostAllowed(false);
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);

        provider = locationManager.getBestProvider(criteria, true);
    }

    /**
     * Ecrire dans un fichier la liste des users croisés (avec qui une conv a été engagée
     */
    @Override
    public void onStop() {
        try {
            ObjectOutputStream writer = new ObjectOutputStream(openFileOutput("users", Context.MODE_PRIVATE));
            writer.writeObject(mUsers);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    /**
     * Notification pour notifier la vue d'un nouvel utilisateur proche de soi
     * @param nameNewUser Nom du nouvel utilisateur
     */
    private void createNotification(String nameNewUser) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.notification_new_person)+" "+nameNewUser)
                .setSmallIcon(R.drawable.logo)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pIntent).build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notificationManager.notify(999999, notification);
    }
}
