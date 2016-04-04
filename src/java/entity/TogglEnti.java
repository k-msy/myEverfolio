/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.util.ArrayList;
import java.util.Map;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

/**
 *
 * @author bpg0129
 */
@Named("togEnti")
@RequestScoped
public class TogglEnti {

    private String totalDurations;
    
    ArrayList<String[]> projectList;

    public String getTotalDurations() {
        return totalDurations;
    }

    public void setTotalDurations(String totalDurations) {
        this.totalDurations = totalDurations;
    }

    public ArrayList<String[]> getProjectList() {
        return projectList;
    }

    public void setProjectList(ArrayList<String[]> projectList) {
        this.projectList = projectList;
    }
    
}
