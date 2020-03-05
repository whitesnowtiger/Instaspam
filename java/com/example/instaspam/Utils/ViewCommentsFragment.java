package com.example.instaspam.Utils;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.instaspam.Home.HomeActivity;
import com.example.instaspam.Models.Comment;
import com.example.instaspam.Models.Like;
import com.example.instaspam.Models.Photo;
import com.example.instaspam.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ViewCommentsFragment extends Fragment {


        private static final String TAG = "ViewCommentsFragment";

        public ViewCommentsFragment(){
            super();
            setArguments(new Bundle());
        }

        //firebase
        private FirebaseAuth mAuth;
        private FirebaseAuth.AuthStateListener mAuthListener;
        private FirebaseDatabase mFirebaseDatabase;
        private DatabaseReference myRef;

        //widgets
        private ImageView mBackArrow, mCheckMark;
        private EditText mComment;
        private ListView mListView;

        //vars
        private Photo mPhoto;
        private ArrayList<Comment> mComments;
        private Context mContext;


        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_view_comments, container, false);
            mBackArrow = (ImageView) view.findViewById(R.id.backArrow);
            mCheckMark = (ImageView) view.findViewById(R.id.ivPostComment);
            mComment = (EditText) view.findViewById(R.id.comment);
            mListView = (ListView) view.findViewById(R.id.listView);
            mComments = new ArrayList<>();
            mContext = getActivity();

            try {
                mPhoto = getPhotoFromBundle();
                setupFirebaseAuth();

            } catch (NullPointerException e) {
                Log.e(TAG, "onCreateView: NullPointerException: " + e.getMessage());
            }


            return view;

        }

        private String getTimestamp(){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("US/Pacific"));
            return sdf.format(new Date());

        }

        private void setupWidgets(){

            CommentListAdapter adapter = new CommentListAdapter(mContext,
                    R.layout.layout_comment, mComments);
            mListView.setAdapter(adapter);

            mCheckMark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(!mComment.getText().toString().equals("")){
                        Log.d(TAG, "onClick: attempting to submit new comment.");
                        addNewComment(mComment.getText().toString());

                        mComment.setText("");    //reset mComment
                        closeKeyboard();
                    } else {
                        Toast.makeText(getActivity(), "you can't post a black comment", Toast.LENGTH_SHORT).show();;
                    }
                }
            });

            mBackArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: navigating back");
                    if(getCallingActivityFromBundle().equals(getString(R.string.home_activity))){
                        getActivity().getSupportFragmentManager().popBackStack();
                        ((HomeActivity)getActivity()).showLayout();
                    } else {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                }
            });

        }

        private void closeKeyboard(){
            View view = getActivity().getCurrentFocus();
            if(view != null){
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }


        private void addNewComment(String newComment){
            Log.d(TAG, "addNewComment: adding new comment: " + newComment);

            String commentID = myRef.push().getKey();

            Comment comment = new Comment();
            comment.setComment(newComment);
            comment.setDate_created(getTimestamp());
            comment.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

            //insert into photos node
            myRef.child(mContext.getString(R.string.dbname_photos))
                    .child(mPhoto.getPhoto_id())
                    .child(mContext.getString(R.string.field_comments))
                    .child(commentID)
                    .setValue(comment);

            //insert into user_photos node
            myRef.child(getString(R.string.dbname_user_photos))
                    .child(mPhoto.getUser_id()) //should be mPhoto.getUser_id()
                    .child(mPhoto.getPhoto_id())
                    .child(mContext.getString(R.string.field_comments))
                    .child(commentID)
                    .setValue(comment);

        }


        /**
         * retrieve the photo from the incoming bundle from profileActivity interface
         * @return
         */
        private String getCallingActivityFromBundle(){
            Log.d(TAG, "getPhotoFromBundle: arguments: " + getArguments());

            Bundle bundle = this.getArguments();
            if(bundle != null) {
                return bundle.getString(getString(R.string.home_activity));
            }else{
                return null;
            }
        }

        /**
         * retrieve the photo from the incoming bundle from profileActivity interface
         * @return
         */
        private Photo getPhotoFromBundle(){
            Log.d(TAG, "getPhotoFromBundle: arguments: " + getArguments());

            Bundle bundle = this.getArguments();
            if(bundle != null) {
                return bundle.getParcelable(mContext.getString(R.string.photo));
            }else{
                return null;
            }
        }


  /*
   --------------------------------------------firebase------------------------------------------
   * setup the firebase auth object
   * */

    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //check if the user is logged in

                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        if(mPhoto.getComments().size()==0){
            mComments.clear();
            Comment firstComment = new Comment();
            firstComment.setComment(mPhoto.getCaption());
            firstComment.setUser_id(mPhoto.getUser_id());
            firstComment.setDate_created(mPhoto.getDate_created());

            mComments.add(firstComment);

            mPhoto.setComments(mComments);

            setupWidgets();
        }

        //Listening to the photo node if there is any new comments
        myRef.child(mContext.getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(mContext.getString(R.string.field_comments))
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Query query  = myRef
                                .child(mContext.getString(R.string.dbname_photos))
                                .orderByChild(mContext.getString(R.string.field_photo_id))
                                .equalTo(mPhoto.getPhoto_id());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot singleSnapShot : dataSnapshot.getChildren()){
                                    Log.d(TAG, "onDataChange: did you get anything?");
                                    //photos.add(singleSnapShot.getValue(Photo.class));      //thinks retrieving hashmap, but need to get a list

                                    Photo photo = new Photo();
                                    Map<String, Object> objectMap = (HashMap<String, Object>)singleSnapShot.getValue();

                                    photo.setCaption(objectMap.get(mContext.getString(R.string.field_caption)).toString());
                                    photo.setTags(objectMap.get(mContext.getString(R.string.field_tags)).toString());
                                    photo.setPhoto_id(objectMap.get(mContext.getString(R.string.field_photo_id)).toString());
                                    photo.setUser_id(objectMap.get(mContext.getString(R.string.field_user_id)).toString());
                                    photo.setDate_created(objectMap.get(mContext.getString(R.string.field_date_created)).toString());
                                    photo.setImage_path(objectMap.get(mContext.getString(R.string.field_image_path)).toString());

                                    mComments.clear();
                                    Comment firstComment = new Comment();
                                    firstComment.setComment(mPhoto.getCaption());
                                    firstComment.setUser_id(mPhoto.getUser_id());
                                    firstComment.setDate_created(mPhoto.getDate_created());

                                    mComments.add(firstComment);


                                    for(DataSnapshot dSnapshot : singleSnapShot
                                            .child(mContext.getString(R.string.field_comments)).getChildren()) {
                                        Comment comment = new Comment();
                                        comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                                        comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                                        comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                                        mComments.add(comment);
                                    }
                                        photo.setComments(mComments);
                                        mPhoto = photo;

                                        setupWidgets();


//                    List<Like> likesList = new ArrayList<Like>();
//                    for(DataSnapshot dSnapshot : singleSnapShot
//                            .child(getString(R.string.field_likes)).getChildren()){
//                        Like like = new Like();
//                        like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
//                        likesList.add(like);
                                    }

//                    photo.setLikes(likesList);
//                    photos.add(photo);

                                }



                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.d(TAG, "onCancelled: query cancelled.");
                            }
                        });

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });




    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        //mViewPager.setCurrentItem(HOME_FRAGMENT);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
            //FirebaseAuth.getInstance().signOut();
        }
    }

    }

