package entity;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

@Named("wiEnti")
@RequestScoped
public class WithingsEnti {

    private String date = "";
    private String timezone = "";
    private int steps = 0;
    private float distance = 0.0F;
    private float calories = 0.0F;
    private float totalcalories = 0.0F;
    private float elevation = 0.0F;
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
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTimezone() {
        return this.timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public int getSteps() {
        return this.steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public float getDistance() {
        return this.distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float getCalories() {
        return this.calories;
    }

    public void setCalories(float calories) {
        this.calories = calories;
    }

    public float getTotalcalories() {
        return this.totalcalories;
    }

    public void setTotalcalories(float totalcalories) {
        this.totalcalories = totalcalories;
    }

    public float getElevation() {
        return this.elevation;
    }

    public void setElevation(float elevation) {
        this.elevation = elevation;
    }

    public int getSoft() {
        return this.soft;
    }

    public void setSoft(int soft) {
        this.soft = soft;
    }

    public int getModerate() {
        return this.moderate;
    }

    public void setModerate(int moderate) {
        this.moderate = moderate;
    }

    public int getIntense() {
        return this.intense;
    }

    public void setIntense(int intense) {
        this.intense = intense;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBody() {
        return this.body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getYesterdaySteps() {
        return this.yesterdaySteps;
    }

    public void setYesterdaySteps(String yesterdaySteps) {
        this.yesterdaySteps = yesterdaySteps;
    }

    public String getTodaySteps() {
        return this.todaySteps;
    }

    public void setTodaySteps(String todaySteps) {
        this.todaySteps = todaySteps;
    }

    public String getDifferenceSteps() {
        return this.differenceSteps;
    }

    public void setDifferenceSteps(String differenceSteps) {
        this.differenceSteps = differenceSteps;
    }

    public String getPastWeight() {
        return this.pastWeight;
    }

    public void setPastWeight(String pastWeight) {
        this.pastWeight = pastWeight;
    }

    public String getCurrentWeight() {
        return this.currentWeight;
    }

    public void setCurrentWeight(String currentWeight) {
        this.currentWeight = currentWeight;
    }

    public String getDifferenceWeight() {
        return this.differenceWeight;
    }

    public void setDifferenceWeight(String differenceWeight) {
        this.differenceWeight = differenceWeight;
    }

    public String getStepArrowIconPass() {
        return this.stepArrowIconPass;
    }

    public void setStepArrowIconPass(String stepArrowIconPass) {
        this.stepArrowIconPass = stepArrowIconPass;
    }

    public String getStepEmoIconPass() {
        return this.stepEmoIconPass;
    }

    public void setStepEmoIconPass(String stepEmoIconPass) {
        this.stepEmoIconPass = stepEmoIconPass;
    }

    public String getWeightArrowIconPass() {
        return this.weightArrowIconPass;
    }

    public void setWeightArrowIconPass(String weightArrowIconPass) {
        this.weightArrowIconPass = weightArrowIconPass;
    }

    public String getWeightEmoIconPass() {
        return this.weightEmoIconPass;
    }

    public void setWeightEmoIconPass(String weightEmoIconPass) {
        this.weightEmoIconPass = weightEmoIconPass;
    }
}
