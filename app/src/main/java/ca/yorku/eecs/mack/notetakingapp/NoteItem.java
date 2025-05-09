package ca.yorku.eecs.mack.notetakingapp;

import java.util.List;

public class NoteItem {
    public enum Type {
        NOTE,
        TASK_LIST
    }

    private Type type;
    private String content; // for regular note
    private List<String> tasks; // for checklist
    private List<Boolean> checked; // status of each task

    public NoteItem(String content) {
        this.type = Type.NOTE;
        this.content = content;
    }

    public NoteItem(List<String> tasks, List<Boolean> checked) {
        this.type = Type.TASK_LIST;
        this.tasks = tasks;
        this.checked = checked;
    }

    public Type getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public List<String> getTasks() {
        return tasks;
    }

    public List<Boolean> getChecked() {
        return checked;
    }

    public void clearTasks() {
        tasks.clear();
        checked.clear();
    }
}