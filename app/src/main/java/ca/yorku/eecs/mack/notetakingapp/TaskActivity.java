package ca.yorku.eecs.mack.notetakingapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class TaskActivity extends AppCompatActivity {

    private EditText titleInput;
    private EditText taskInput;
    private LinearLayout taskContainer;
    private ArrayList<String> tasks;
    private ArrayList<Boolean> checkedStates;
    private int position = -1; // position of task list

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        titleInput = findViewById(R.id.editTextTaskTitle);
        taskContainer = findViewById(R.id.taskListContainer);
        taskInput = findViewById(R.id.editTextTaskInput);

        Button addTaskBtn = findViewById(R.id.buttonAddTask);
        Button saveBtn = findViewById(R.id.buttonSaveTaskList);


        // get existing data
        Intent intent = getIntent();
        String description = intent.getStringExtra("description");
        tasks = intent.getStringArrayListExtra("tasks");
        checkedStates = (ArrayList<Boolean>) intent.getSerializableExtra("checked");
        position = intent.getIntExtra("position", -1);

        // set existing title
        titleInput.setText(description);

        // add existing tasks
        for (int i = 0; i < tasks.size(); i++) {
            addTaskToLayout(tasks.get(i), checkedStates.get(i));
        }

        addTaskBtn.setOnClickListener(v -> {
            String taskText = taskInput.getText().toString().trim();
            if (taskText.isEmpty()) {
                Toast.makeText(TaskActivity.this, "Please enter a task before adding it", Toast.LENGTH_SHORT).show();
            }
            else {
                addTaskToLayout(taskText, false);
                taskInput.setText("");
            }
        });

        saveBtn.setOnClickListener(v -> {
            ArrayList<String> updatedTasks = new ArrayList<>();
            ArrayList<Boolean> updatedChecked = new ArrayList<>();

            for (int i = 0; i < taskContainer.getChildCount(); i++) {
                View child = taskContainer.getChildAt(i);
                if (child instanceof CheckBox) {
                    CheckBox checkBox = (CheckBox) child;
                    String taskText = checkBox.getText().toString().trim();
                    if (!taskText.isEmpty()) {
                        updatedTasks.add(taskText);
                        updatedChecked.add(checkBox.isChecked());
                    }
                }
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra("description", titleInput.getText().toString().trim());
            resultIntent.putStringArrayListExtra("tasks", updatedTasks);
            resultIntent.putExtra("checked", updatedChecked);
            resultIntent.putExtra("position", position); // sends back position
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });


        Button clearBtn = findViewById(R.id.buttonClearTasks);
        clearBtn.setOnClickListener(v -> {
            taskContainer.removeAllViews();
            Toast.makeText(TaskActivity.this, "All tasks cleared", Toast.LENGTH_SHORT).show();
        });
    }

    private void addTaskToLayout(String taskText, boolean isChecked) {
        CheckBox checkBox = new CheckBox(this);
        checkBox.setText(taskText);
        checkBox.setChecked(isChecked);

        checkBox.setOnCheckedChangeListener((buttonView, isNowChecked) -> {
            if (isNowChecked) {
                Toast.makeText(TaskActivity.this, "Task marked as complete", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(TaskActivity.this, "Task marked as incomplete", Toast.LENGTH_SHORT).show();
            }
        });

        taskContainer.addView(checkBox);
    }


}
