package com.cookandroid.login.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cookandroid.login.DateAdapter;
import com.cookandroid.login.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
public class FragFriend extends Fragment implements DateAdapter.OnDateClickListener {

    private RecyclerView dateRecyclerView;
    private DateAdapter dateAdapter;
    private List<String> dateList;
    private TextView menuDisplayTextView;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_friend, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = mAuth.getCurrentUser();

        menuDisplayTextView = view.findViewById(R.id.menuDisplayTextView);

        dateRecyclerView = view.findViewById(R.id.dateRecyclerView);
        dateRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        dateList = generateDateList();

        // 현재 날짜의 인덱스를 구한다.
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd", Locale.getDefault());
        String currentDate = dateFormat.format(calendar.getTime());
        int currentIndex = dateList.indexOf(currentDate);

        // 현재 날짜의 인덱스가 리스트의 중앙에 오도록 조정
        dateRecyclerView.scrollToPosition(currentIndex);

        dateAdapter = new DateAdapter(dateList, this);
        dateRecyclerView.setAdapter(dateAdapter);

        displayMenu();

        return view;
    }

    private List<String> generateDateList() {
        List<String> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd", Locale.getDefault());

        // 현재 달의 날짜 추가
        calendar.set(Calendar.DAY_OF_MONTH, 1); // 현재 달의 첫 날로 설정
        int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH); // 현재 달의 마지막 날짜
        for (int i = 1; i <= maxDay; i++) {
            dates.add(dateFormat.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // 이전 달의 날짜 추가
        calendar.set(Calendar.DAY_OF_MONTH, 1); // 이전 달의 첫 날로 설정
        maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH); // 이전 달의 마지막 날짜
        for (int i = 1; i <= maxDay; i++) {
            dates.add(dateFormat.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // 다음 달의 날짜 추가
        calendar.set(Calendar.DAY_OF_MONTH, 1); // 다음 달의 첫 날로 설정
        maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH); // 다음 달의 마지막 날짜
        for (int i = 1; i <= maxDay; i++) {
            dates.add(dateFormat.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return dates;
    }

    @Override
    public void onDateClick(int position) {
        String selectedDate = dateList.get(position);
        Toast.makeText(getActivity(), "Selected date: " + selectedDate, Toast.LENGTH_SHORT).show();
    }
    private void displayMenu() {
        String userId = currentUser.getUid();
        DatabaseReference userRef = mDatabase.child("meals").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasSnapshot) {
                StringBuilder menuDisplay = new StringBuilder();
                for (DataSnapshot dataSnapshot : datasSnapshot.getChildren()) {
                    ImagePredict imagePredict = dataSnapshot.getValue(ImagePredict.class);
                    if (imagePredict != null) {
                        menuDisplay.append("Date and Time: ").append(imagePredict.getDateTime()).append("\n");
                        menuDisplay.append("Meal Time: ").append(imagePredict.getEatTime()).append("\n");
                        menuDisplay.append("Menu Name: ").append(imagePredict.getImageName()).append("\n\n");
                    }
                }
                menuDisplayTextView.setText(menuDisplay.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "메뉴를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}