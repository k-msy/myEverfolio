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
@Named("wiEnti")
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

    private String yesterdaySteps = "";
    private String todaySteps = "";
    private String differenceSteps = "";

    private String pastWeight = "";
    private String currentWeight = "";
    private String differenceWeight = "";

    private String stepArrowIconPass = "";
    private String stepEmoIconPass = "";

    private String weightArrowIconPass = "";
    private String weightEmoIconPass = "";

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

    public String getYesterdaySteps() {
        return yesterdaySteps;
    }

    public void setYesterdaySteps(String yesterdaySteps) {
        this.yesterdaySteps = yesterdaySteps;
    }

    public String getTodaySteps() {
        return todaySteps;
    }

    public void setTodaySteps(String todaySteps) {
        this.todaySteps = todaySteps;
    }

    public String getDifferenceSteps() {
        return differenceSteps;
    }

    public void setDifferenceSteps(String differenceSteps) {
        this.differenceSteps = differenceSteps;
    }

    public String getPastWeight() {
        return pastWeight;
    }

    public void setPastWeight(String pastWeight) {
        this.pastWeight = pastWeight;
    }

    public String getCurrentWeight() {
        return currentWeight;
    }

    public void setCurrentWeight(String currentWeight) {
        this.currentWeight = currentWeight;
    }

    public String getDifferenceWeight() {
        return differenceWeight;
    }

    public void setDifferenceWeight(String differenceWeight) {
        this.differenceWeight = differenceWeight;
    }

    public String getStepArrowIconPass() {
        return stepArrowIconPass;
    }

    public void setStepArrowIconPass(String stepArrowIconPass) {
        this.stepArrowIconPass = stepArrowIconPass;
    }

    public String getStepEmoIconPass() {
        return stepEmoIconPass;
    }

    public void setStepEmoIconPass(String stepEmoIconPass) {
        this.stepEmoIconPass = stepEmoIconPass;
    }

    public String getWeightArrowIconPass() {
        return weightArrowIconPass;
    }

    public void setWeightArrowIconPass(String weightArrowIconPass) {
        this.weightArrowIconPass = weightArrowIconPass;
    }

    public String getWeightEmoIconPass() {
        return weightEmoIconPass;
    }

    public void setWeightEmoIconPass(String weightEmoIconPass) {
        this.weightEmoIconPass = weightEmoIconPass;
    }

}
