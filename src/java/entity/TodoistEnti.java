package entity;

import java.io.Serializable;
import java.util.ArrayList;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

@Named("toEnti")
@RequestScoped
public class TodoistEnti implements Serializable {

    private String completedIconPass = "";
    private String content = "";
    private String lastUpdate = "";
    private ArrayList<String[]> taskList;

    public String getCompletedIconPass() {
        return this.completedIconPass;
    }

    public void setCompletedIconPass(String completedIconPass) {
        this.completedIconPass = completedIconPass;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLastUpdate() {
        return this.lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public ArrayList<String[]> getTaskList() {
        return this.taskList;
    }

    public void setTaskList(ArrayList<String[]> taskList) {
        this.taskList = taskList;
    }
}
