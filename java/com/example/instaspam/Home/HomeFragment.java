package com.example.instaspam.Home;

import android.os.Bundle;
//import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.instaspam.Models.Comment;
import com.example.instaspam.Models.Like;
import com.example.instaspam.Models.Photo;
import com.example.instaspam.Models.User;
import com.example.instaspam.Models.UserAccountSettings;
import com.example.instaspam.R;
import com.example.instaspam.Utils.MainfeedListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.assist.deque.LIFOLinkedBlockingDeque;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    //vars
    private ArrayList<Photo> mPhotos;
    private ArrayList<Photo> mPaginatedPhotos;
    private ArrayList<String> mFollowing;
    private ListView mListView;
    private MainfeedListAdapter mAdapter;
    private int mResults;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mListView = (ListView) view.findViewById(R.id.listView);
        mFollowing = new ArrayList<>();
        mPhotos = new ArrayList<>();

        getFollowing();

        return view;
    }

    private void getFollowing() {
        Log.d(TAG, "getFollowing: searching for following");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot singleSnapShot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found user: " + singleSnapShot.child(getString(R.string.field_user_id)).getValue());

                    mFollowing.add(singleSnapShot.child(getString(R.string.field_user_id)).getValue().toString());
                }
                mFollowing.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                //get the photos
                getPhotos();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }

    private void getPhotos() {
        Log.d(TAG, "getPhotos: getting photos");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        for (int i = 0; i < mFollowing.size(); i++) {
            final int count = i;
            Query query = reference
                    .child(getActivity().getString(R.string.dbname_user_photos))
                    .child(mFollowing.get(i))
                    .orderByChild(getString(R.string.field_user_id))
                    .equalTo(mFollowing.get(i));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapShot : dataSnapshot.getChildren()) {
                        Log.d(TAG, "onDataChange: did you get anything?");
                        //photos.add(singleSnapShot.getValue(Photo.class));      //thinks retrieving hashmap, but need to get a list

                        Photo photo = new Photo();
                        Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapShot.getValue();

                        photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                        photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                        photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                        photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                        photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                        photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                        Log.d(TAG, "getPhotos: photo: " + photo.getPhoto_id());
                        ArrayList<Comment> comments = new ArrayList<Comment>();
                        for (DataSnapshot dSnapshot : singleSnapShot
                                .child(getString(R.string.field_comments)).getChildren()) {
                            Map<String, Object> object_map = (HashMap<String, Object>) dSnapshot.getValue();
                            Comment comment = new Comment();
                            comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                            comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                            comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                            comments.add(comment);
                        }
                        photo.setComments(comments);
                        mPhotos.add(photo);
                        Log.d(TAG, "mPhotos : " + mPhotos);
                    }

                        if (count <= mFollowing.size() - 1) {
                            //display our photos
                            Log.d(TAG, "onDataChange: count: " + count);
                            displayPhoto();


                        }
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError){
                        Log.d(TAG, "onCancelled: query cancelled.");
                    }

            });
        }
    }

    private void displayPhoto(){
        mPaginatedPhotos = new ArrayList<>();
        if(mPhotos != null) {
            try{

            Collections.sort(mPhotos, new Comparator<Photo>() {
                @Override
                public int compare(Photo o1, Photo o2) {
                    return o2.getDate_created().compareTo(o1.getDate_created());
                }

            });
//we want to load 10 at a time. So if there is more than 10, just load 10 to start
            int iterations = mPhotos.size();
                Log.d(TAG, "displayPhoto: iteration " + iterations);

            if(iterations > 10) {
                iterations = 10;
            }

            mResults = 10;
            for(int i = 0; i<iterations; i++){
                Log.d(TAG, "displayPhoto: hi");
                mPaginatedPhotos.add(mPhotos.get(i));
                Log.d(TAG, "displayPhoto: " + mPaginatedPhotos);
                mResults++;
                Log.d(TAG, "displayPhoto: adding a photo to paginated list: " + mPhotos.get(i).getPhoto_id());
            }
            mAdapter = new MainfeedListAdapter(getActivity(), R.layout.layout_mainfeed_listitem, mPhotos);
            mListView.setAdapter(mAdapter);

            //mListView.notifyUpdated();

            } catch(NullPointerException e){
                Log.e(TAG, "displayPhoto: NullPointerException: " + e.getMessage());
            } catch (IndexOutOfBoundsException e){
                Log.e(TAG, "displayPhoto: IndexOutOfBoundsException: " + e.getMessage());
            }
        }
    }

    public void displayMorePhotos(){
        Log.d(TAG, "displayMorePhotos: displaying more photos");

        try{
            if(mPhotos.size() > mResults && mPhotos.size() > 0) {

                int iterations;
                if(mPhotos.size() > (mResults + 10)) {
                    Log.d(TAG, "displayMorePhotos: there are greater than 10 more photos");
                    iterations = 10;
                } else {
                    Log.d(TAG, "displayMorePhotos: there are less than 10 more photos\");\n");
                    iterations = mPhotos.size() - mResults;
                }

                //add the new photos to the paginated results
                for(int i = mResults; i < mResults + iterations; i++) {
                    mPaginatedPhotos.add(mPhotos.get(i));
                }
                mResults = mResults + iterations;
                mAdapter.notifyDataSetChanged();
            }


        }catch(NullPointerException e) {
            Log.d(TAG, "displayMorePhotos: NullPointerException" + e.getMessage());

        }catch(IndexOutOfBoundsException e){
            Log.d(TAG, "displayMorePhotos: IndexOutOfBoundsException" + e.getMessage());
        }

    }

}
