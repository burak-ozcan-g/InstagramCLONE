package com.example.instagramdenemesolo.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.instagramdenemesolo.Adapter.PhotoAdapter;
import com.example.instagramdenemesolo.Adapter.PostAdapter;
import com.example.instagramdenemesolo.EditProfileActivity;
import com.example.instagramdenemesolo.FollowersActivity;
import com.example.instagramdenemesolo.Model.Post;
import com.example.instagramdenemesolo.Model.User;
import com.example.instagramdenemesolo.OptionsActivity;
import com.example.instagramdenemesolo.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private RecyclerView recyclerViewSaves;
    private PhotoAdapter saveAdapter;
    private List<Post> saveList;

    private RecyclerView recyclerViewPhoto;
    private PhotoAdapter photoAdapter;
    private List<Post> photoList;

    private CircleImageView imageProfile;
    private ImageView options;
    private TextView followers;
    private TextView following;
    private TextView posts;
    private TextView fullname;
    private TextView bio;
    private TextView username;

    private ImageView myPictures;
    private ImageView savedPictures;

    private Button editProfile;

    private FirebaseUser firebaseUser;

    String profileId;


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view   = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        String data = getContext().getSharedPreferences("PROFILE", Context.MODE_PRIVATE).getString("profileId", "none");

        if (data.equals("none")){
            profileId = firebaseUser.getUid();
        }else{
            profileId = data;
            getContext().getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit().clear().apply();
        }



        imageProfile= view.findViewById(R.id.image_profile);
        options= view.findViewById(R.id.options);
        followers= view.findViewById(R.id.followers);
        following= view.findViewById(R.id.following);
        posts= view.findViewById(R.id.posts);
        fullname= view.findViewById(R.id.fullname);
        bio= view.findViewById(R.id.bio);
        username= view.findViewById(R.id.username);
        myPictures= view.findViewById(R.id.my_pictures);
        savedPictures= view.findViewById(R.id.saved_pictures);
        editProfile= view.findViewById(R.id.edit_profile);

        recyclerViewPhoto = view.findViewById(R.id.recyler_view_pictures);
        recyclerViewPhoto.setHasFixedSize(true);
        recyclerViewPhoto.setLayoutManager(new GridLayoutManager(getContext(),3));
        photoList = new ArrayList<>();
        photoAdapter = new PhotoAdapter(getContext(), photoList);
        recyclerViewPhoto.setAdapter(photoAdapter);

        recyclerViewSaves = view.findViewById(R.id.recyler_view_saves);
        recyclerViewSaves.setHasFixedSize(true);
        recyclerViewSaves.setLayoutManager(new GridLayoutManager(getContext(),3));
        saveList = new ArrayList<>();
        saveAdapter = new PhotoAdapter(getContext(), saveList);
        recyclerViewSaves.setAdapter(saveAdapter);

        userInfo();
        getFollowersandFollowingCount();
        getPostCount();
        myPhotos();
        getSavedPosts();

        if(profileId.equals(firebaseUser.getUid())){
            editProfile.setText("Profili Düzenle");
        }else{
            checkFollowingStatus();
        }

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String btnText = editProfile.getText().toString();

                if (btnText.equals("Profili Düzenle")) {
                    startActivity(new Intent(getContext(), EditProfileActivity.class));
                }else{
                    if (btnText.equals("Takip Et")){
                        FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                                .child("following").child(profileId).setValue(true);
                        FirebaseDatabase.getInstance().getReference().child("Follow").child(profileId)
                                .child("followers").child(firebaseUser.getUid()).setValue(true);
                    }else{
                        FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                                .child("following").child(profileId).removeValue();
                        FirebaseDatabase.getInstance().getReference().child("Follow").child(profileId)
                                .child("followers").child(firebaseUser.getUid()).removeValue();
                    }
                }
            }
        });

        recyclerViewPhoto.setVisibility(View.VISIBLE);
        recyclerViewSaves.setVisibility(View.GONE);

        myPictures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerViewPhoto.setVisibility(View.VISIBLE);
                recyclerViewSaves.setVisibility(View.GONE);
            }
        });
        savedPictures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerViewPhoto.setVisibility(View.GONE);
                recyclerViewSaves.setVisibility(View.VISIBLE);
            }
        });

        followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", profileId);
                intent.putExtra("title", "Takipçi");
                startActivity(intent);
            }
        });

        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", profileId);
                intent.putExtra("title", "Takip");
                startActivity(intent);
            }
        });

        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), OptionsActivity.class));
            }
        });

        return view;
    }

    private void getSavedPosts() {

        final List<String> savedIds = new ArrayList<>();

        FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    savedIds.add(dataSnapshot.getKey());
                }
                FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot1) {

                        saveList.clear();

                        for (DataSnapshot dataSnapshot1 : snapshot1.getChildren()){

                            Post post = dataSnapshot1.getValue(Post.class);

                            for (String id: savedIds){
                                if (post.getPostid().equals(id)){
                                    saveList.add(post);
                                }
                            }
                        }
                        saveAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void myPhotos() {
        FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                photoList.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Post post =  dataSnapshot.getValue(Post.class);
                    if(post.getPublisher().equals(profileId)){
                        photoList.add(post);
                    }
                }
                Collections.reverse(photoList);
                photoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkFollowingStatus() {

        FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                .child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(profileId).exists()){
                    editProfile.setText("Takipten Çık");
                }else {
                    editProfile.setText("Takip Et");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getPostCount() {
        FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int counter = 0;
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Post post = dataSnapshot.getValue(Post.class);

                    if(post.getPublisher().equals(profileId)){
                        counter++;
                    }
                    posts.setText(String.valueOf(counter));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFollowersandFollowingCount() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Follow").child(profileId);
        ref.child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followers.setText( ""+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ref.child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                following.setText(""+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void userInfo() {

        FirebaseDatabase.getInstance().getReference().child("Users").child(profileId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                Picasso.get().load(user.getImageurl()).into(imageProfile);
                username.setText(user.getUsername());
                fullname.setText(user.getName());
                bio.setText(user.getBio());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}