package entity;

import java.io.Serializable;
import java.util.ArrayList;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

@Named("togEnti")
@RequestScoped
public class TogglEnti implements Serializable {

    private String totalDurations;
    ArrayList<String> projectList;

    public String getTotalDurations() {
        return this.totalDurations;
    }

    public void setTotalDurations(String totalDurations) {
        this.totalDurations = totalDurations;
    }

    public ArrayList<String> getProjectList() {
        return this.projectList;
    }

    public void setProjectList(ArrayList<String> projectList) {
        this.projectList = projectList;
    }
}
