package com.example.iitg_speech_lab;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

// import com.example.iitg_speech_lab.Classes.GradingMyData;


import com.example.iitg_speech_lab.Class.GradingMyData;
import com.example.iitg_speech_lab.Model.GradingDataModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;

public class FragmentGrading extends Fragment {
    private static RecyclerView.Adapter adapter;
    private RecyclerView recyclerView;
    private static ArrayList<GradingDataModel> data;
    static View.OnClickListener myOnClickListener;
    public TaskCompletionSource<Integer> task1;
    public Task task2;
    View V;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grading, container, false);
        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.V = view;
        String courseInfo = ViewAssignment.courseInfo;
        String assignmentID = ViewAssignment.assignmentID;

        myOnClickListener = new MyOnClickListener();

        recyclerView = view.findViewById(R.id.grading_recycler_view);
        recyclerView.setHasFixedSize(true);


        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        task1 = new TaskCompletionSource<>();
        task2 = task1.getTask();
        GradingMyData.loadGrades(courseInfo,assignmentID,task1);

        Task<Void> allTask = Tasks.whenAll(task2);

        allTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                data = new ArrayList<>();
                // Log.d("yo",Integer.toString(GradingMyData.assignmentsInfoList.size()));
                // for (int i = 0; i < GradingMyData.assignmentsInfoList.size(); i++) {
                //     data.add(new GradingDataModel(
                //             GradingMyData.assignmentsInfoList.get(i),
                //             GradingMyData.assignmentsNameList.get(i),
                //             GradingMyData.assignmentsDeadlineList.get(i)
                for (int i = 0; i < GradingMyData.StudentGradeList.size(); i++) {
                    data.add(new GradingDataModel(
                            GradingMyData.StudentNameList.get(i),
                            GradingMyData.StudentGradeList.get(i)
                    ));
                }

                adapter = new GradingCustomAdapter(data);
                recyclerView.setAdapter(adapter);
            }
        });
    }
    private class MyOnClickListener implements View.OnClickListener {

        private MyOnClickListener() {
        }

        @Override
        public void onClick(View v) {
            viewAssignment(v);
        }

        private void viewAssignment(View v) {
            int selectedItemPosition = recyclerView.getChildLayoutPosition(v);
            RecyclerView.ViewHolder viewHolder
                    = recyclerView.findViewHolderForLayoutPosition(selectedItemPosition);
            TextView textViewName
                    = null;
            if (viewHolder != null) {
                textViewName = viewHolder.itemView.findViewById(R.id.textViewCourseID);
            }
            String selectedName = null;
            if (textViewName != null) {
                selectedName = ( String ) textViewName.getText();
            }
            //int selectedItemId = -1;

//            for (int i = 0; i < GradingMyData.assignmentsIDList.size(); i++) {
//                if (selectedName.equals(GradingMyData.assignmentsIDList.get(i))) {
//                    //selectedItemId = GradingMyData.assignmentsInfoList.get(i);
//                }
//            }
//            removedItems.add(selectedItemId);
//            data.remove(selectedItemPosition);
//            adapter.notifyItemRemoved(selectedItemPosition);

            Log.d("aman",selectedName);
            String ainfo = (String) viewHolder.itemView.getTag();
            Log.d("aman",ainfo);
//            Intent intent = new Intent(AssignmentsActivity.this, ViewCourse.class);
//            intent.putExtra("courseInfo",cinfo);
//            startActivity(intent);


        }
    }
}