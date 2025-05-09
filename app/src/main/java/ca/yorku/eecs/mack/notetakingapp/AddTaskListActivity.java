package ca.yorku.eecs.mack.notetakingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AddTaskListActivity extends AppCompatActivity {

    private EditText editTextTaskTitle, editTextTaskInput;
    private Button buttonAddTask, buttonClearTasks, buttonSaveTaskList;
    private RecyclerView recyclerViewTasks;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task_list);

        editTextTaskTitle = findViewById(R.id.editTextTaskTitle);
        editTextTaskInput = findViewById(R.id.editTextTaskInput);
        buttonAddTask = findViewById(R.id.buttonAddTask);
        buttonClearTasks = findViewById(R.id.buttonClearTasks);
        buttonSaveTaskList = findViewById(R.id.buttonSaveTaskList);
        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);

        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList);
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTasks.setAdapter(taskAdapter);

        buttonAddTask.setOnClickListener(v -> {
            String taskText = editTextTaskInput.getText().toString().trim();
            // checks to see if the task is empty
            if (!taskText.isEmpty()) {
                taskList.add(new Task(taskText));
                taskAdapter.notifyItemInserted(taskList.size() - 1);
                editTextTaskInput.setText("");
            }
            else {
                Toast.makeText(AddTaskListActivity.this, "Please enter a task before adding it", Toast.LENGTH_SHORT).show();
            }
        });

        // clears all tasks in the task list
        buttonClearTasks.setOnClickListener(v -> {
            taskList.clear();
            taskAdapter.notifyDataSetChanged();
        });

        // save button for task list
        buttonSaveTaskList.setOnClickListener(v -> {
            if (!taskList.isEmpty()) {
                ArrayList<String> taskDescriptions = new ArrayList<>();
                ArrayList<Boolean> checkedStates = new ArrayList<>();

                for (Task t : taskList) {
                    taskDescriptions.add(t.getDescription());
                    checkedStates.add(t.isChecked());
                }

                String taskTitle = editTextTaskTitle.getText().toString().trim();

                // sends the results back to HomeActivity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("description", taskTitle);
                resultIntent.putStringArrayListExtra("tasks", taskDescriptions);
                resultIntent.putExtra("checked", checkedStates);
                resultIntent.putExtra("position", -1); // -1 means it's a new task list
                setResult(RESULT_OK, resultIntent);
                finish();
            }

            else{
                Toast.makeText(AddTaskListActivity.this, "Please add a task before saving it", Toast.LENGTH_SHORT).show();
            }

        });
    }
}
