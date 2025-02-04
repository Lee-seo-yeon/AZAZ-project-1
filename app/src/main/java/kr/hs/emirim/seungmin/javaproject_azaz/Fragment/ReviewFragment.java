package kr.hs.emirim.seungmin.javaproject_azaz.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import kr.hs.emirim.seungmin.javaproject_azaz.Adapter.ReviewRecyclerAdapter;
import kr.hs.emirim.seungmin.javaproject_azaz.MainActivity;
import kr.hs.emirim.seungmin.javaproject_azaz.Model.Review;
import kr.hs.emirim.seungmin.javaproject_azaz.Model.User;
import kr.hs.emirim.seungmin.javaproject_azaz.NewPostActivity;
import kr.hs.emirim.seungmin.javaproject_azaz.R;
import kr.hs.emirim.seungmin.javaproject_azaz.category.AllFragment;
import kr.hs.emirim.seungmin.javaproject_azaz.category.ElsethingFragment;
import kr.hs.emirim.seungmin.javaproject_azaz.category.ExerciseFragment;
import kr.hs.emirim.seungmin.javaproject_azaz.category.FocusFragment;
import kr.hs.emirim.seungmin.javaproject_azaz.category.HeadFragment;
import kr.hs.emirim.seungmin.javaproject_azaz.category.HearingFragment;
import kr.hs.emirim.seungmin.javaproject_azaz.category.ImagineFragment;
import kr.hs.emirim.seungmin.javaproject_azaz.category.LanguageFragment;
import kr.hs.emirim.seungmin.javaproject_azaz.category.SightFragment;
import kr.hs.emirim.seungmin.javaproject_azaz.category.TouchFragment;

public class ReviewFragment extends Fragment implements View.OnClickListener {

    private RecyclerView review_list_view;
    private List<Review> review_list;
    private List<User> user_list;

    private FirebaseFirestore firebaseFirestore;
    private ReviewRecyclerAdapter reviewRecyclerAdapter;
    private FirebaseAuth firebaseAuth;

    private FloatingActionButton add_review;

    private Boolean isFirstPageFirstLoad = true;

    private CardView category_sight;
    private CardView category_hearing;
    private CardView category_touch;
    private CardView category_imagine;
    private CardView category_exercise;
    private CardView category_head;
    private CardView category_focus;
    private CardView category_language;
    private CardView category_elsething;

    //fragment initial
    private Fragment sightFragment;
    private Fragment touchFragment;
    private Fragment languageFragment;
    private Fragment focusFragment;
    private Fragment headFragment;
    private Fragment hearingFragment;
    private Fragment imagineFragment;
    private Fragment elsethFragment;
    private Fragment exerciseFragment;


   public ReviewFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_review, container, false);

        review_list = new ArrayList<>();
        user_list = new ArrayList<>();
        review_list_view = view.findViewById(R.id.review_list_view);

        add_review = view.findViewById(R.id.add_review);

        firebaseAuth = FirebaseAuth.getInstance();

        //category intial
        category_sight = view.findViewById(R.id.category_sight);
        category_hearing = view.findViewById(R.id.category_hearing);
        category_touch = view.findViewById(R.id.category_touch);
        category_imagine = view.findViewById(R.id.category_imagine);
        category_exercise = view.findViewById(R.id.category_exercise);
        category_head = view.findViewById(R.id.category_head);
        category_focus = view.findViewById(R.id.category_focus);
        category_language = view.findViewById(R.id.category_language);
        category_elsething = view.findViewById(R.id.category_elseth);

        fragment_find();

        reviewRecyclerAdapter = new ReviewRecyclerAdapter(review_list, user_list);
        review_list_view.setLayoutManager(new GridLayoutManager(getActivity(),2));
        review_list_view.setAdapter(reviewRecyclerAdapter);

        add_review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent add_review_intent = new Intent(getContext(), NewPostActivity.class);
                startActivity(add_review_intent);
            }
        });


        if (firebaseAuth.getCurrentUser() != null) {

            firebaseFirestore = FirebaseFirestore.getInstance();

            review_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                    if(reachedBottom) {
                        //loadMoreReview();
                    }


                }
            });

            category_sight.setOnClickListener(this);
            category_hearing.setOnClickListener(this);
            category_touch.setOnClickListener(this);
            category_imagine.setOnClickListener(this);
            category_exercise.setOnClickListener(this);
            category_head.setOnClickListener(this);
            category_focus.setOnClickListener(this);
            category_language.setOnClickListener(this);
            category_elsething.setOnClickListener(this);
            view.findViewById(R.id.category_all).setOnClickListener(this);

            // Firestore에서 'Reviews' 라는 데이터베이스 테이블의 데이터 가져오는 쿼리문
            // - 리뷰를 작성한 시간이 담겨있는 timestamp를 이용해 최신순으로 정렬하기
            Query firstQuery = firebaseFirestore.collection("Reviews")
                    .orderBy("timestamp",Query.Direction.DESCENDING);

            firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                    if(firebaseAuth.getCurrentUser() != null) {
                        if(!value.isEmpty()) {

                            if(isFirstPageFirstLoad) {
                                review_list.clear();
                                user_list.clear();
                            }

                        }
                        if (error != null) {
                            System.err.println(error);
                        }

                        for(DocumentChange doc : value.getDocumentChanges()) {

                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String reviewId = doc.getDocument().getId();
                                final Review review = doc.getDocument().toObject(Review.class).withId(reviewId);

                                String reviewUserId = doc.getDocument().getString("user_id");
                                firebaseFirestore.collection("Users").document(reviewUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                        if(task.isSuccessful()) {
                                            Log.e("test","firebasfirestore 동작 good");
                                            User user = task.getResult().toObject(User.class);

                                            if(isFirstPageFirstLoad) {
                                                user_list.add(user);
                                                review_list.add(review);
                                            } else {
                                                user_list.add(0,user);
                                                review_list.add(0,review);
                                            }

                                            Log.e("test","firebase add good");

                                        }
                                        reviewRecyclerAdapter.notifyDataSetChanged();
                                    }

                                });
                            }
                        }

                    }
                }

            });
        }

        return view;
    }

    private void fragment_find() {
        exerciseFragment = new ExerciseFragment();
        elsethFragment = new ElsethingFragment();
        focusFragment = new FocusFragment();
        headFragment = new HeadFragment();
        hearingFragment = new HearingFragment();
        imagineFragment = new ImagineFragment();
        languageFragment = new LanguageFragment();
        sightFragment = new SightFragment();
        touchFragment = new TouchFragment();

    }


    private void loadMoreReview() {

        if(firebaseAuth.getCurrentUser() != null) {

            Query nextQuery = firebaseFirestore.collection("Reviews")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(3);

            nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                    if(!value.isEmpty()) {

                        if (error != null) {
                            System.err.println(error);
                        }

                        for (DocumentChange doc : value.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String reviewId = doc.getDocument().getId();
                                final Review review = doc.getDocument().toObject(Review.class).withId(reviewId);

                                String reviewUserId = doc.getDocument().getString("user_id");
                                firebaseFirestore.collection("Users").document(reviewUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                        if (task.isSuccessful()) {
                                            Log.e("test", "firebasfirestore 동작 good2");
                                            User user = task.getResult().toObject(User.class);

                                            user_list.add(user);
                                            review_list.add(review);

                                        }
                                        reviewRecyclerAdapter.notifyDataSetChanged();
                                    }

                                });
                            }
                        }
                    }
                }
            });
        }


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.category_all :
                getFragmentManager().beginTransaction().replace(R.id.category_review_container,new AllFragment()).commit();
                break;
            case R.id.category_exercise :
                getFragmentManager().beginTransaction().replace(R.id.category_review_container,exerciseFragment).commit();
                break;
            case R.id.category_sight :
                getFragmentManager().beginTransaction().replace(R.id.category_review_container, sightFragment).commit();
                break;
            case R.id.category_elseth :
                getFragmentManager().beginTransaction().replace(R.id.category_review_container, elsethFragment).commit();
                break;
            case R.id.category_focus :
                getFragmentManager().beginTransaction().replace(R.id.category_review_container, focusFragment).commit();
                break;
            case R.id.category_head :
                getFragmentManager().beginTransaction().replace(R.id.category_review_container, headFragment).commit();
                break;
            case R.id.category_hearing :
                getFragmentManager().beginTransaction().replace(R.id.category_review_container, hearingFragment).commit();
                break;
            case R.id.category_imagine :
                getFragmentManager().beginTransaction().replace(R.id.category_review_container, imagineFragment).commit();
                break;
            case R.id.category_language :
                getFragmentManager().beginTransaction().replace(R.id.category_review_container, languageFragment).commit();
                break;
            case R.id.category_touch :
                getFragmentManager().beginTransaction().replace(R.id.category_review_container, touchFragment).commit();
                break;
            default:
                return;

        }
    }
}