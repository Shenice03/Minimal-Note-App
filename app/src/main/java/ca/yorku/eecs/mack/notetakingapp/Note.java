package ca.yorku.eecs.mack.notetakingapp;

import java.util.List;

public class Note {

    public enum Type {
        NOTE,
        TASK_LIST
    }

    private String title;
    private String content;
    private List<String> tasks;
    private List<Boolean> checked;
    private Type type;
    private String description;
    private List<String> taskList;

    private String drawingImageBase64;


    // constructor for regular note
    public Note(String title, String content) {
        this.title = title;
        this.content = content;
        this.type = Type.NOTE;
    }

    // constructor for task list
    public Note(String description, List<String> tasks, List<Boolean> checked) {
        this.description = description;
        this.tasks = tasks;
        this.checked = checked;
        this.type = Type.TASK_LIST;
    }

    public void setDrawingImageBase64(String drawingImageBase64) {
        this.drawingImageBase64 = drawingImageBase64;
    }
    public String getDrawingImageBase64() {
        return drawingImageBase64;
    }

    public String getDescription() {
        return description;
    }


    public String getTitle() {
        return title;
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

    public Type getType() {
        return type;
    }

    public void clearTasks() {
        if (tasks != null) tasks.clear();
        if (checked != null) checked.clear();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getTaskList() {
        return taskList;
    }


}