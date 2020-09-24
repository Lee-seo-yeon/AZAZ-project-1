package kr.hs.emirim.seungmin.javaproject_azaz;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ReviewFragment extends Fragment {

    private RecyclerView review_list_view;
    private List<Review> review_list;
    private List<User> user_list;

    private FirebaseFirestore firebaseFirestore;
    private ReviewRecyclerAdapter reviewRecyclerAdapter;
    private FirebaseAuth firebaseAuth;


    public ReviewFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_review, container, false);

        review_list = new ArrayList<>();
        user_list = new ArrayList<>();
        review_list_view = view.findViewById(R.id.review_list_view);

        firebaseAuth = FirebaseAuth.getInstance();

        reviewRecyclerAdapter = new ReviewRecyclerAdapter(review_list, user_list);
        review_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        review_list_view.setAdapter(reviewRecyclerAdapter);
        if (firebaseAuth.getCurrentUser() != null) {

            firebaseFirestore = FirebaseFirestore.getInstance();

            review_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    loadMoreReview();
                }
            });

            Query firstQuery = firebaseFirestore.collection("Reviews")
                    .orderBy("timestamp",Query.Direction.DESCENDING).limit(3);

            firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                    if(!value.isEmpty()) {

//                        review_list.clear();
//                        user_list.clear();
                    }
                    if (error != null) {
                        System.err.println(error);
                    }

                    for(DocumentChange doc : value.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String reviewId = doc.getDocument().getId();
                            final Review review = doc.getDocument().toObject(Review.class).withId(reviewId);

                            String reviewUserId = doc.getDocument().getString("user_id");
                            firebaseFirestore.collection("Reviews").document(reviewUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                    if(task.isSuccessful()) {
                                        User user = task.getResult().toObject(User.class);

                                        user_list.add(user);
                                        review_list.add(review);

                                    }
                                }

                            });
                        }
                    }
                    reviewRecyclerAdapter.notifyDataSetChanged();
                }
            });
        }

        return view;
    }

    private void loadMoreReview() {
    }
}