/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

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

    public String getTotalDurations() {
        return totalDurations;
    }

    public void setTotalDurations(String totalDurations) {
        this.totalDurations = totalDurations;
    }
    
    
    
}
