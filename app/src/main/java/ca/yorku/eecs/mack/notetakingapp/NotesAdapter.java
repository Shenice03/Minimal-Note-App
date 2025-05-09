package ca.yorku.eecs.mack.notetakingapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.List;
import java.util.ArrayList;

public class NotesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Note> items;
    private List<Note> allNotesList;

    private Context context;

    private boolean isDarkMode;

    public NotesAdapter(Context context, List<Note> items, boolean isDarkMode) {
        this.context = context;
        this.items = items;
        this.allNotesList = items;
        this.isDarkMode = isDarkMode;
    }

    private static final int TYPE_NOTE = 0;
    private static final int TYPE_TASK_LIST = 1;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_NOTE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item, parent, false);
            return new NoteViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
            return new TaskListViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Note item = items.get(position);

        if (holder instanceof NoteViewHolder) {
            NoteViewHolder noteHolder = (NoteViewHolder) holder;

            String title = item.getTitle();

            noteHolder.noteTitle.setText(title);
            noteHolder.noteText.setText(item.getContent());

            int textColor = isDarkMode ? R.color.backgroundDark : R.color.backgroundLight;
            noteHolder.noteTitle.setTextColor(ContextCompat.getColor(context, textColor));
            noteHolder.noteText.setTextColor(ContextCompat.getColor(context, textColor));

            noteHolder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, NoteActivity.class);
                intent.putExtra("title", item.getTitle());
                intent.putExtra("content", item.getContent());
                intent.putExtra("position", holder.getAdapterPosition());
                intent.putExtra("drawing", item.getDrawingImageBase64());

                if (context instanceof Activity) {
                    ((Activity) context).startActivityForResult(intent, 1);
                }
            });

            holder.itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onNoteLongClicked(position);
                }
                return true;
            });
        }

        else if (holder instanceof TaskListViewHolder) {
            TaskListViewHolder taskHolder = (TaskListViewHolder) holder;

            taskHolder.taskListTitle.setText(item.getDescription());

            taskHolder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, TaskActivity.class);
                intent.putExtra("description", item.getDescription());
                intent.putStringArrayListExtra("tasks", new ArrayList<>(item.getTasks()));
                intent.putExtra("checked", new ArrayList<>(item.getChecked()));
                intent.putExtra("position", holder.getAdapterPosition());
                if (context instanceof Activity) {
                    ((Activity) context).startActivityForResult(intent, 2);
                }
            });

            // clear old checkboxes if any
            taskHolder.taskListContainer.removeAllViews();

            List<String> tasks = item.getTasks();
            List<Boolean> checked = item.getChecked();

            for (int i = 0; i < tasks.size(); i++) {
                CheckBox checkBox = new CheckBox(taskHolder.taskListContainer.getContext());
                checkBox.setText(tasks.get(i));
                checkBox.setChecked(checked.get(i));

                int finalI = i;
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    checked.set(finalI, isChecked);
                });

                taskHolder.taskListContainer.addView(checkBox);
            }

            taskHolder.clearTasksButton.setOnClickListener(v -> {
                int position2 = holder.getAdapterPosition();
                items.remove(position2);
                notifyItemRemoved(position2);

                // save updated list
                SharedPreferences prefs = context.getSharedPreferences("NotesApp", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                Gson gson = new Gson();
                String json = gson.toJson(items);
                editor.putString("notes", json);
                editor.apply();
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType() == Note.Type.NOTE ? TYPE_NOTE : TYPE_TASK_LIST;
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView noteTitle;
        TextView noteText;

        public NoteViewHolder(View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.noteTitle);
            noteText = itemView.findViewById(R.id.noteText);
        }
    }

    static class TaskListViewHolder extends RecyclerView.ViewHolder {
        TextView taskListTitle;
        LinearLayout taskListContainer;
        Button clearTasksButton;

        public TaskListViewHolder(View itemView) {
            super(itemView);
            taskListTitle = itemView.findViewById(R.id.taskListTitle);
            taskListContainer = itemView.findViewById(R.id.taskListContainer);
            clearTasksButton = itemView.findViewById(R.id.clearTasksButton);
        }
    }

    public void setNotes(List<Note> notes) {
        this.items = notes;
        this.allNotesList = notes;
        notifyDataSetChanged();
    }

    public interface OnNoteLongClickListener {
        void onNoteLongClicked(int position);
    }

    private OnNoteLongClickListener longClickListener;

    public void setOnNoteLongClickListener(OnNoteLongClickListener listener) {
        this.longClickListener = listener;
    }
}