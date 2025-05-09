package ca.yorku.eecs.mack.notetakingapp;

public class Task {
    private String description;
    private boolean isChecked;

    public Task(String description) {
        this.description = description;
        this.isChecked = false;
    }

    public String getDescription() {
        return description;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }




}
