package thirdparty.withings;

import bean.HeaderBb;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import static constants.Common.*;
import static constants.Const_oauth.*;
import static constants.Const_withings.*;
import db.WithingsDb;
import entity.Token_withings;
import entity.WithingsEnti;
import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import oauth.Owithings;
import oauth.SuperOauth;
import util.UtilDate;
import view.chart.BarChart;
import view.chart.LineChart;

@RequestScoped
public class Withings extends SuperOauth {

    HttpServletRequest request = getRequest();
    HttpSession session = this.request.getSession(true);
    @Inject
    HeaderBb headerBb;
    @Inject
    WithingsEnti wiEnti;
    @Inject
    Owithings owi;
    @Inject
    Token_withings tokenObj;
    @Inject
    UtilDate utiDate;
    @Inject
    BarChart barChart;
    @Inject
    LineChart lineChart;
    @EJB
    WithingsDb db;

    private String getRawDataForSteps(String from, String to) throws IOException {
        String userId = this.session.getAttribute(USER_ID).toString();
        this.tokenObj = this.db.findObj(userId);
        String access_token = this.tokenObj.getAccess_token();
        String access_token_secret = this.tokenObj.getAccess_token_secret();
        String USER_ID = this.tokenObj.getWi_userId();

        SortedMap<String, String> paramsMap = new TreeMap();
        try {
            paramsMap.put(WI_ACTION, WI_GET_ACTIVITY);
            paramsMap.put(WI_STARTDATE_YMD, from);
            paramsMap.put(WI_ENDDATE_YMD, to);
            paramsMap.put(OAUTH_CONSUMER_KEY, CONSUMER_KEY);
            paramsMap.put(OAUTH_NONCE, super.getRandomChar());
            paramsMap.put(OAUTH_TIMESTAMP, String.valueOf(super.getUnixTime()));
            paramsMap.put(OAUTH_TOKEN, access_token);
            paramsMap.put(OAUTH_SIGNATURE_METHOD, HMAC_SHA1);
            paramsMap.put(OAUTH_VERSION, "1.0");
            paramsMap.put(OAUTH_USERID, USER_ID);

            String sigData = super.makeSigData(CONSUMER_KEY, BODY_MEASURES_URL, paramsMap, HTTP_GET);
            String sigKey = super.makeSigKey(CONSUMER_SECRET, access_token_secret);

            URL url = new URL(
                    BODY_MEASURES_URL + "?"
                    + WI_ACTION + "=" + WI_GET_ACTIVITY + "&"
                    + OAUTH_USERID + "=" + URLEncode(USER_ID) + "&"
                    + WI_STARTDATE_YMD + "=" + URLEncode(paramsMap.get(WI_STARTDATE_YMD)) + "&"
                    + WI_ENDDATE_YMD + "=" + URLEncode(paramsMap.get(WI_ENDDATE_YMD)) + "&"
                    + OAUTH_CONSUMER_KEY + "=" + URLEncode(paramsMap.get(OAUTH_CONSUMER_KEY)) + "&"
                    + OAUTH_NONCE + "=" + URLEncode(paramsMap.get(OAUTH_NONCE)) + "&"
                    + OAUTH_SIGNATURE + "=" + URLEncode(super.makeSignature(sigKey, sigData)) + "&"
                    + OAUTH_SIGNATURE_METHOD + "=" + URLEncode(paramsMap.get(OAUTH_SIGNATURE_METHOD)) + "&"
                    + OAUTH_TIMESTAMP + "=" + URLEncode(paramsMap.get(OAUTH_TIMESTAMP)) + "&"
                    + OAUTH_TOKEN + "=" + URLEncode(access_token) + "&"
                    + OAUTH_VERSION + "=" + URLEncode(paramsMap.get(OAUTH_VERSION))
            );
            return super.httpResponse(url, HTTP_GET);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return "";
    }

    public void setWeightMeasures()
            throws IOException {
        String yesterday = this.utiDate.getYesterDayYyyyMmDd();
        String today = this.utiDate.getTodayYyyyMmDd();

        String jsonText = getWeightJsonData(yesterday, today);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = (JsonNode) mapper.readValue(jsonText, JsonNode.class);
        JsonNode measuregrps = node.get(WI_BODY).get(WI_MEASURE_GRPS);

        ArrayList<Double> weightList = new ArrayList();
        for (JsonNode measures : measuregrps) {
            Double value = measures.get(WI_MEASURES).get(0).get(WI_VALUE).asDouble();
            for (int unit = measures.get(WI_MEASURES).get(0).get(WI_UNIT).asInt(); unit < 0; unit++) {
                value = value / 10.0D;
            }
            weightList.add(value);
        }
        if (weightList.size() < 0) {
            System.out.println("体重計に乗りましょう");
        } else if (weightList.size() == 1) {
            Double current = weightList.get(0);
            this.wiEnti.setCurrentWeight(String.valueOf(current));
            setWeightIconPass(0.0D);
        } else if (weightList.size() >= 2) {
            Double current = weightList.get(0);
            Double past = weightList.get(1);
            BigDecimal difference = new BigDecimal(current - past);
            difference = difference.setScale(1, 1);
            this.wiEnti.setPastWeight(String.valueOf(past));
            this.wiEnti.setCurrentWeight(String.valueOf(current));
            setWeightIconPass(difference.doubleValue());
        }
    }

    private ArrayList<WithingsObject> getRangeWeightMeasures(String start, String end, ArrayList<String> dayList, int dayCount) {
        String jsonText = getWeightJsonData(start, end);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = null;
        try {
            node = (JsonNode) mapper.readValue(jsonText, JsonNode.class);
        } catch (IOException ex) {
            Logger.getLogger(Withings.class.getName()).log(Level.SEVERE, null, ex);
        }
        JsonNode measuregrps = node.get(WI_BODY).get(WI_MEASURE_GRPS);

        ArrayList<WithingsObject> weightList = new ArrayList();
        ArrayList<String> dupliDateList = new ArrayList();
        for (JsonNode measures : measuregrps) {
            String date = this.utiDate.convertUtcToYyyyMmDd(measures.get(WI_DATE).toString());
            date = date.substring(5);
            if (!existsDuplicate(dupliDateList, date)) {
                dupliDateList.add(date);

                WithingsObject wiObj = new WithingsObject();
                wiObj.utcDate = measures.get(WI_DATE).longValue();
                wiObj.dateStr = date;
                Double value = measures.get(WI_MEASURES).get(0).get(WI_VALUE).asDouble();
                for (int unit = measures.get(WI_MEASURES).get(0).get(WI_UNIT).asInt(); unit < 0; unit++) {
                    value = value / 10.0D;
                }
                wiObj.weight = value;
                weightList.add(wiObj);
            }
        }
        injectZeroDayData(dayList, weightList);
        Collections.sort(weightList, new WithingsComparator());
        if (dayCount > 60) {
            weightList = summarizeMonthWeight(weightList);
        } else if (dayCount > 31) {
            weightList = summarizeWeekWeight(weightList);
        }
        return weightList;
    }

    private Map<String, String> adjustSteps(String rawDataForSteps) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = (JsonNode) mapper.readValue(rawDataForSteps, JsonNode.class);
        JsonNode activities = node.get(WI_BODY).get(WI_ACTIVITIES);

        ArrayList<Integer> stepList = new ArrayList();
        for (JsonNode activity : activities) {
            stepList.add(activity.get(WI_STEPS).asInt());
        }
        Object stepsMap = setStepsMap(stepList);
        return (Map<String, String>) stepsMap;
    }

    public void setStepsMeasures() throws IOException {
        String yesterday = this.utiDate.getYesterDayYyyyMmDd();
        String today = this.utiDate.getTodayYyyyMmDd();

        String rawDataForSteps = getRawDataForSteps(yesterday, today);
        Map<String, String> stepsMap = adjustSteps(rawDataForSteps);
        this.wiEnti.setYesterdaySteps(stepsMap.get(WI_YESTERDAY));
        this.wiEnti.setTodaySteps(stepsMap.get(WI_TODAY));
        this.wiEnti.setDifferenceSteps(stepsMap.get(WI_DIFFERENCE));
        this.wiEnti.setStepArrowIconPass(stepsMap.get(WI_ARROW_ICON));
        this.wiEnti.setStepEmoIconPass(stepsMap.get(WI_EMO_ICON));
    }

    public void setRangeMeasures(String from, String to, ArrayList<String> dayList, int dayCount) {
        ArrayList<WithingsObject> stepList = getRangeStepMeasures(from, to, dayList, dayCount);
        ArrayList<WithingsObject> weightList = getRangeWeightMeasures(from, to, dayList, dayCount);
        if (dayCount > 30) {
            this.barChart.setSumBarModelWithings(stepList);
            this.lineChart.setSumWeightLineModel(weightList);
        } else {
            this.barChart.setBarModelWithings(stepList, dayList);
            this.lineChart.setWeightLineModel(weightList, dayList);
        }
    }

    private Map<String, String> setStepIconPass(Map<String, String> stepsMap, String diffrence) {
        int diff = Integer.valueOf(diffrence);
        if (0 > diff) {
            stepsMap.put(WI_DIFFERENCE, diffrence);
            stepsMap.put(WI_ARROW_ICON, ICONPATH_NEGATIVE_ARROW);
            stepsMap.put(WI_EMO_ICON, ICONPATH_BAD);
        } else if (0 == diff) {
            stepsMap.put(WI_DIFFERENCE, diffrence);
            stepsMap.put(WI_ARROW_ICON, ICONPATH_NATURAL_ARROW);
            stepsMap.put(WI_EMO_ICON, ICONPATH_NATURAL);
        } else if (0 < diff) {
            stepsMap.put(WI_DIFFERENCE, "+" + diffrence);
            stepsMap.put(WI_ARROW_ICON, ICONPATH_POSITIVE_ARROW);
            stepsMap.put(WI_EMO_ICON, ICONPATH_GOOD);
        }
        return stepsMap;
    }

    private void setWeightIconPass(Double diff) {
        if (0.0D > diff) {
            this.wiEnti.setDifferenceWeight(String.valueOf(diff));
            this.wiEnti.setWeightArrowIconPass(ICONPATH_POSITIVE_DOWN_ARROW);
            this.wiEnti.setWeightEmoIconPass(ICONPATH_GOOD);
        } else if (0.0D != diff) {
            if (0.0D >= diff) {
                this.wiEnti.setDifferenceWeight(String.valueOf(diff));
                this.wiEnti.setWeightArrowIconPass(ICONPATH_NATURAL_ARROW);
                this.wiEnti.setWeightEmoIconPass(ICONPATH_NATURAL);
            } else {
                this.wiEnti.setDifferenceWeight("+" + String.valueOf(diff));
                this.wiEnti.setWeightArrowIconPass(ICONPATH_NEGATIVE_UP_ARROW);
                this.wiEnti.setWeightEmoIconPass(ICONPATH_BAD);
            }
        }
    }

    public boolean isExistAccessToken(HttpSession session) {
        String userId = session.getAttribute(USER_ID).toString();
        this.tokenObj = this.db.findObj(userId);
        boolean exist = true;
        if (("".equals(this.tokenObj.getAccess_token())) || ("".equals(this.tokenObj.getAccess_token_secret()))) {
            exist = this.owi.isCallback(session);
        }
        return exist;
    }

    private ArrayList<WithingsObject> getRangeStepMeasures(String from, String to, ArrayList<String> dayList, int dayCount) {
        ArrayList<WithingsObject> stepList = new ArrayList();
        try {
            String jsonText = "";
            jsonText = getRawDataForSteps(from, to);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = (JsonNode) mapper.readValue(jsonText, JsonNode.class);
            JsonNode activities = node.get(WI_BODY).get(WI_ACTIVITIES);
            for (JsonNode activity : activities) {
                WithingsObject wiObj = new WithingsObject();
                String[] date = activity.get(WI_DATE).toString().split("\"");
                wiObj.dateStr = date[1].substring(5);
                wiObj.steps = activity.get(WI_STEPS).asInt();
                wiObj.utcDate = Long.valueOf(this.utiDate.convertStartUTC(date[1]));
                stepList.add(wiObj);
            }
            injectZeroDayData(dayList, stepList);
            Collections.sort(stepList, new WithingsComparator());
            if (dayCount > 60) {
                stepList = summarizeMonthStep(stepList);
            } else if (dayCount > 30) {
                stepList = summarizeWeekStep(stepList);
            }
            System.out.println("サマライズ終了");
        } catch (IOException ex) {
            Logger.getLogger(Withings.class.getName()).log(Level.SEVERE, null, ex);
        }
        return stepList;
    }

    private Map<String, String> setStepsMap(ArrayList<Integer> stepList) {
        int yesterday = 0;
        int today = 0;
        String diffrence;
        if (0 == stepList.size()) {
            diffrence = String.valueOf(today - yesterday);
            System.out.println("最近2日間の歩数データがありません");
        } else if (1 >= stepList.size()) {
            yesterday = (stepList.get(0));
            diffrence = String.valueOf(today - yesterday);
            System.out.println("今日のデータが同期されていない可能性があります");
        } else {
            yesterday = (stepList.get(0));
            today = (stepList.get(1));
            diffrence = String.valueOf(today - yesterday);
        }
        Map<String, String> stepsMap = new HashMap();
        stepsMap.put(WI_YESTERDAY, String.valueOf(yesterday));
        stepsMap.put(WI_TODAY, String.valueOf(today));
        stepsMap = setStepIconPass(stepsMap, diffrence);

        return stepsMap;
    }

    private String getWeightJsonData(String from, String to) {
        String utcFrom = this.utiDate.convertStartUTC(from);
        String utcEnd = this.utiDate.convertEndUTC(to);
        String userId = this.session.getAttribute(USER_ID).toString();
        this.tokenObj = this.db.findObj(userId);
        String USER_ID = this.tokenObj.getWi_userId();
        String ACCESS_TOKEN = this.tokenObj.getAccess_token();
        String ACCESS_TOKEN_SECRET = this.tokenObj.getAccess_token_secret();

        SortedMap<String, String> paramsMap = new TreeMap();
        try {
            paramsMap.put(WI_ACTION, WI_GETMEAS);
            paramsMap.put(OAUTH_USERID, USER_ID);
            paramsMap.put(WI_STARTDATE, utcFrom);
            paramsMap.put(WI_ENDDATE, utcEnd);
            paramsMap.put(WI_MEAS_TYPE, "1");
            paramsMap.put(OAUTH_CONSUMER_KEY, CONSUMER_KEY);
            paramsMap.put(OAUTH_NONCE, super.getRandomChar());
            paramsMap.put(OAUTH_SIGNATURE_METHOD, HMAC_SHA1);
            paramsMap.put(OAUTH_TIMESTAMP, String.valueOf(super.getUnixTime()));
            paramsMap.put(OAUTH_TOKEN, ACCESS_TOKEN);
            paramsMap.put(OAUTH_TOKEN, "1.0");

            String sigData = super.makeSigData(CONSUMER_KEY, WEIGHT_MEASURES_URL, paramsMap, HTTP_GET);

            String sigKey = super.makeSigKey(CONSUMER_SECRET, ACCESS_TOKEN_SECRET);

            URL url = new URL(
                    WEIGHT_MEASURES_URL + "?"
                    + WI_ACTION + "=" + WI_GETMEAS + "&"
                    + OAUTH_USERID + "=" + URLEncode(USER_ID) + "&"
                    + WI_STARTDATE + "=" + URLEncode(paramsMap.get(WI_STARTDATE)) + "&"
                    + WI_ENDDATE + "=" + URLEncode(paramsMap.get(WI_ENDDATE)) + "&"
                    + WI_MEAS_TYPE + "=" + URLEncode(paramsMap.get(WI_MEAS_TYPE)) + "&"
                    + OAUTH_CONSUMER_KEY + "=" + URLEncode(paramsMap.get(OAUTH_CONSUMER_KEY)) + "&"
                    + OAUTH_NONCE + "=" + URLEncode(paramsMap.get(OAUTH_NONCE)) + "&"
                    + OAUTH_SIGNATURE + "=" + URLEncode(super.makeSignature(sigKey, sigData)) + "&"
                    + OAUTH_SIGNATURE_METHOD + "=" + URLEncode(paramsMap.get(OAUTH_SIGNATURE_METHOD)) + "&"
                    + OAUTH_TIMESTAMP + "=" + URLEncode(paramsMap.get(OAUTH_TIMESTAMP)) + "&"
                    + OAUTH_TOKEN + "=" + URLEncode(ACCESS_TOKEN) + "&"
                    + OAUTH_VERSION + "=" + URLEncode(paramsMap.get(OAUTH_TOKEN))
            );
            return super.httpResponse(url, HTTP_GET);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return "";
    }

    public boolean doesCooperate(HttpSession session) {
        boolean coop = false;
        if (isExistAccessToken(session)) {
            this.headerBb.setWiCoopFlg(true);
            coop = true;
        } else {
            this.headerBb.setWiCoopFlg(false);
            coop = false;
        }
        return coop;
    }

    public boolean changeCoop(boolean wiCoopFlg) {
        if (wiCoopFlg) {
            this.db.releaseCoopWithings(this.session);
            return false;
        }
        this.db.coopWithings(this.session);
        return true;
    }

    public boolean cancelChangeCoop(boolean wiCoopFlg) {
        if (wiCoopFlg) {
            return false;
        }
        return true;
    }

    private ArrayList<WithingsObject> summarizeWeekStep(ArrayList<WithingsObject> stepList) {
        ArrayList<WithingsObject> sumStepList = new ArrayList();
        for (int i = 0; i < stepList.size(); i++) {
            String start = ((WithingsObject) stepList.get(i)).dateStr;
            long utcDate = ((WithingsObject) stepList.get(i)).utcDate;
            int sum = 0;
            for (int j = 0; j < 7; j++) {
                if (i >= stepList.size()) {
                    break;
                }
                sum += ((WithingsObject) stepList.get(i)).steps;
                i += 1;
            }
            String end = findEndDate(i - 1, stepList);
            WithingsObject wiObj = new WithingsObject();
            wiObj.dateStr = (start + "〜" + end);
            wiObj.steps = sum;
            wiObj.utcDate = utcDate;
            sumStepList.add(wiObj);
        }
        return sumStepList;
    }

    private ArrayList<WithingsObject> summarizeMonthStep(ArrayList<WithingsObject> stepList) {
        ArrayList<WithingsObject> sumStepList = new ArrayList();
        for (WithingsObject step : stepList) {
            if (!sumStepList.isEmpty()) {
                boolean sumFlg = false;
                for (int i = 0; i < sumStepList.size(); i++) {
                    String month = step.dateStr.substring(0, 2);
                    if (month.equals(((WithingsObject) sumStepList.get(i)).dateStr)) {
                        int sum = ((WithingsObject) sumStepList.get(i)).steps + step.steps;
                        ((WithingsObject) sumStepList.get(i)).steps = sum;
                        sumFlg = true;
                    }
                }
                if (!sumFlg) {
                    step.dateStr = step.dateStr.substring(0, 2);
                    sumStepList.add(step);
                }
            } else {
                step.dateStr = step.dateStr.substring(0, 2);
                sumStepList.add(step);
            }
        }
        return sumStepList;
    }

    private String findEndDate(int i, ArrayList<WithingsObject> list) {
        String end;
        if (i < list.size()) {
            end = ((WithingsObject) list.get(i)).dateStr;
        } else {
            end = ((WithingsObject) list.get(list.size() - 1)).dateStr;
        }
        return end;
    }

    private ArrayList<WithingsObject> summarizeMonthWeight(ArrayList<WithingsObject> weightList) {
        ArrayList<WithingsObject> sumWeightList = new ArrayList();
        for (WithingsObject weight : weightList) {
            if (!sumWeightList.isEmpty()) {
                boolean sumFlg = false;
                for (int i = 0; i < sumWeightList.size(); i++) {
                    String month = weight.dateStr.substring(0, 2);
                    if (month.equals(((WithingsObject) sumWeightList.get(i)).dateStr)) {
                        sumFlg = true;
                        Double extWeight = weight.weight;
                        if (!extWeight.equals(0.0D)) {
                            if (((WithingsObject) sumWeightList.get(i)).weight.equals(0.0D)) {
                                ((WithingsObject) sumWeightList.get(i)).weight = extWeight;
                            } else if (extWeight >= ((WithingsObject) sumWeightList.get(i)).weight) {
                            } else {
                                ((WithingsObject) sumWeightList.get(i)).weight = extWeight;
                            }
                        }
                    }
                }
                if (!sumFlg) {
                    weight.dateStr = weight.dateStr.substring(0, 2);
                    sumWeightList.add(weight);
                }
            } else {
                weight.dateStr = weight.dateStr.substring(0, 2);
                sumWeightList.add(weight);
            }
        }
        return sumWeightList;
    }

    private ArrayList<WithingsObject> summarizeWeekWeight(ArrayList<WithingsObject> weightList) {
        ArrayList<WithingsObject> sumWeightList = new ArrayList();
        for (int i = 0; i < weightList.size(); i++) {
            String start = ((WithingsObject) weightList.get(i)).dateStr;
            long utcDate = ((WithingsObject) weightList.get(i)).utcDate;
            Double extWeight = 0.0D;
            for (int j = 0; j < 7; j++) {
                if (i >= weightList.size()) {
                    break;
                }
                Double weight = ((WithingsObject) weightList.get(i)).weight;
                if (!weight.equals(0.0D)) {
                    if (extWeight.equals(0.0D)) {
                        extWeight = weight;
                    } else if (weight < extWeight) {
                        extWeight = weight;
                    }
                }
                i += 1;
            }
            String end = findEndDate(i - 1, weightList);
            WithingsObject wiObj = new WithingsObject();
            wiObj.dateStr = (start + "〜" + end);
            wiObj.weight = extWeight;
            wiObj.utcDate = utcDate;
            sumWeightList.add(wiObj);
        }
        return sumWeightList;
    }

    private void injectZeroDayData(ArrayList<String> dayList, ArrayList<WithingsObject> list) {
        Iterator localIterator;
        String day;
        if (list.isEmpty()) {
            for (localIterator = dayList.iterator(); localIterator.hasNext();) {
                day = (String) localIterator.next();
                WithingsObject wiObj = new WithingsObject();
                wiObj.dateStr = day;
                wiObj.utcDate = Long.valueOf(this.utiDate.convertStartUTC(day));
                list.add(wiObj);
            }
        } else {
            Object dateStrList = new ArrayList();
            for (WithingsObject wiObj : list) {
                ((ArrayList) dateStrList).add(wiObj.dateStr);
            }
            for (int i = 0; i < dayList.size(); i++) {
                String date = ((String) dayList.get(i)).substring(5);
                int index = ((ArrayList) dateStrList).indexOf(date);
                if (index < 0) {
                    WithingsObject wiObj = new WithingsObject();
                    wiObj.dateStr = ((String) dayList.get(i)).substring(5);
                    wiObj.utcDate = Long.valueOf(this.utiDate.convertStartUTC((String) dayList.get(i)));
                    list.add(wiObj);
                }
            }
        }
    }

    private boolean existsDuplicate(ArrayList<String> dupliDateList, String date) {
        int index = dupliDateList.indexOf(date);
        if (index < 0) {
            return false;
        }
        return true;
    }
}
