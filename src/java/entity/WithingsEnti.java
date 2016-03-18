/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import javax.enterprise.context.RequestScoped;

/**
 *
 * @author bpg0129
 */
@RequestScoped
public class WithingsEnti {
    private String date = "";
    private String timezone = "";
    private int steps = 0;
    private float distance = 0;
    private float calories = 0;
    private float totalcalories = 0;
    private float elevation = 0;
    private int soft = 0;
    private int moderate = 0;
    private int intense = 0;
    private String status = "";
    private String body = "";
    
    private int difference = 0;

    public int getDifference() {
        return difference;
    }

    public void setDifference(int difference) {
        this.difference = difference;
    }

    
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float getCalories() {
        return calories;
    }

    public void setCalories(float calories) {
        this.calories = calories;
    }

    public float getTotalcalories() {
        return totalcalories;
    }

    public void setTotalcalories(float totalcalories) {
        this.totalcalories = totalcalories;
    }

    public float getElevation() {
        return elevation;
    }

    public void setElevation(float elevation) {
        this.elevation = elevation;
    }

    public int getSoft() {
        return soft;
    }

    public void setSoft(int soft) {
        this.soft = soft;
    }

    public int getModerate() {
        return moderate;
    }

    public void setModerate(int moderate) {
        this.moderate = moderate;
    }

    public int getIntense() {
        return intense;
    }

    public void setIntense(int intense) {
        this.intense = intense;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
    
    
    
}
