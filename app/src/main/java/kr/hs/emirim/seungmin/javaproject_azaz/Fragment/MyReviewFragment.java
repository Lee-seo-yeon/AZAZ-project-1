package kr.hs.emirim.seungmin.javaproject_azaz.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import kr.hs.emirim.seungmin.javaproject_azaz.Adapter.ReviewRecyclerAdapter;
import kr.hs.emirim.seungmin.javaproject_azaz.Model.Review;
import kr.hs.emirim.seungmin.javaproject_azaz.Model.User;
import kr.hs.emirim.seungmin.javaproject_azaz.R;


public class MyReviewFragment extends Fragment {

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private RecyclerView my_reivew_list_view;
    private List<Review> review_list;
    private List<User> user_list;
    private ReviewRecyclerAdapter reviewRecyclerAdapter;
    private ImageView empty_image;
    private TextView empty_txt;
    private ImageView back_main;

    private Fragment SettingPageFragment;

    public MyReviewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        View mView = inflater.inflate(R.layout.fragment_my_review, container, false);

        my_reivew_list_view = mView.findViewById(R.id.my_review_list_view);
        empty_image = mView.findViewById(R.id.empty_image_my);
        empty_txt = mView.findViewById(R.id.my_empty_txt);
        back_main = mView.findViewById(R.id.back_main);

        SettingPageFragment = new SettingPageFragment();

        review_list = new ArrayList<>();
        user_list = new ArrayList<>();

        reviewRecyclerAdapter = new ReviewRecyclerAdapter(review_list, user_list);
        my_reivew_list_view.setLayoutManager(new GridLayoutManager(getActivity(),2));
        my_reivew_list_view.setAdapter(reviewRecyclerAdapter);

        back_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction().replace(R.id.set_page_fragment_container, SettingPageFragment).commit();
            }
        });

        final String currentUserId = firebaseAuth.getCurrentUser().getUid();


        firebaseFirestore.collection("Users/"+currentUserId+"/reviews").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                if(error == null) {
                    if(!value.isEmpty()) {
                        my_reivew_list_view.setVisibility(View.VISIBLE);
                        empty_image.setVisibility(View.GONE);
                        empty_txt.setVisibility(View.GONE);
                    }

                    if(!value.isEmpty()) {
                        review_list.clear();
                        user_list.clear();
                    }
                    if(error!=null) {
                        System.err.println(error);
                    }

                    for(DocumentChange doc : value.getDocumentChanges()) {
                        if(doc.getType() == DocumentChange.Type.ADDED) {

                            String reviewId = doc.getDocument().getId();
                            final Review review = doc.getDocument().toObject(Review.class).withId(reviewId);

                            String reviewUserId = doc.getDocument().getString("user_id");
                            firebaseFirestore.collection("Users").document(reviewUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                    if(task.isSuccessful()) {
                                        User user = task.getResult().toObject(User.class);

                                        user_list.add(user);
                                        review_list.add(review);
                                    }
                                    reviewRecyclerAdapter.notifyDataSetChanged();
                                }
                            });

                        }
                    }
                } else {
                    System.err.println(error);
                }

            }
        });

        return mView;
    }
}