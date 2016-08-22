package thirdparty.withings;

import bean.HeaderBb;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import db.WithingsDb;
import entity.Token_withings;
import entity.WithingsEnti;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
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
public class Withings
        extends SuperOauth {

    private static final String method = "GET";
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

    private String getRawDataForSteps(String from, String to)
            throws IOException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        String userId = this.session.getAttribute("user_id").toString();
        this.tokenObj = this.db.findObj(userId);
        String access_token = this.tokenObj.getAccess_token();
        String access_token_secret = this.tokenObj.getAccess_token_secret();
        String USER_ID = this.tokenObj.getWi_userId();

        SortedMap<String, String> paramsMap = new TreeMap();
        try {
            paramsMap.put("action", "getactivity");
            paramsMap.put("startdateymd", from);
            paramsMap.put("enddateymd", to);
            paramsMap.put("oauth_consumer_key", "f1e9bebd38c1bf97b7c58bf2f5844c9bf7c38ec50254124d4f43b8582f0f");
            paramsMap.put("oauth_nonce", super.getRandomChar());
            paramsMap.put("oauth_timestamp", String.valueOf(super.getUnixTime()));
            paramsMap.put("oauth_token", access_token);
            paramsMap.put("oauth_signature_method", "HMAC-SHA1");
            paramsMap.put("oauth_version", "1.0");
            paramsMap.put("userid", USER_ID);

            String sigData = super.makeSigData("f1e9bebd38c1bf97b7c58bf2f5844c9bf7c38ec50254124d4f43b8582f0f", "https://wbsapi.withings.net/v2/measure", paramsMap, "GET");

            String sigKey = super.makeSigKey("c17484ff357d801897828f674bb6175b4f94340516ce5bb4922def7e035", access_token_secret);

            URL url = new URL("https://wbsapi.withings.net/v2/measure?action=getactivity&userid=" + URLEncode(USER_ID) + "&startdateymd=" + URLEncode((String) paramsMap.get("startdateymd")) + "&enddateymd=" + URLEncode((String) paramsMap.get("enddateymd")) + "&oauth_consumer_key=" + URLEncode((String) paramsMap.get("oauth_consumer_key")) + "&oauth_nonce=" + URLEncode((String) paramsMap.get("oauth_nonce")) + "&oauth_signature=" + URLEncode(super.makeSignature(sigKey, sigData)) + "&oauth_signature_method=" + URLEncode((String) paramsMap.get("oauth_signature_method")) + "&oauth_timestamp=" + URLEncode((String) paramsMap.get("oauth_timestamp")) + "&oauth_token=" + URLEncode(access_token) + "&oauth_version=" + URLEncode((String) paramsMap.get("oauth_version")));

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String text = reader.readLine();
            System.out.println("text=" + text);

            return text;
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
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

        String updateTime = node.get("body").get("updatetime").toString();

        JsonNode measuregrps = node.get("body").get("measuregrps");

        ArrayList<Double> weightList = new ArrayList();
        for (JsonNode measures : measuregrps) {
            Double value = Double.valueOf(measures.get("measures").get(0).get("value").asDouble());
            System.out.println("value =" + String.valueOf(value));
            for (int unit = measures.get("measures").get(0).get("unit").asInt(); unit < 0; unit++) {
                value = Double.valueOf(value.doubleValue() / 10.0D);
            }
            weightList.add(value);
            System.out.println("realValue =" + String.valueOf(value));
        }
        if (weightList.size() < 0) {
            System.out.println("���������������������������������������������");
        } else if (weightList.size() == 1) {
            Double current = (Double) weightList.get(0);
            this.wiEnti.setCurrentWeight(String.valueOf(current));
            setWeightIconPass(Double.valueOf(0.0D));
        } else if (weightList.size() >= 2) {
            Double current = (Double) weightList.get(0);
            Double past = (Double) weightList.get(1);
            BigDecimal difference = new BigDecimal(current.doubleValue() - past.doubleValue());
            difference = difference.setScale(1, 1);
            System.out.println("difference =" + String.valueOf(difference.doubleValue()));
            this.wiEnti.setPastWeight(String.valueOf(past));
            this.wiEnti.setCurrentWeight(String.valueOf(current));
            setWeightIconPass(Double.valueOf(difference.doubleValue()));
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
        JsonNode measuregrps = node.get("body").get("measuregrps");

        ArrayList<WithingsObject> weightList = new ArrayList();
        ArrayList<String> dupliDateList = new ArrayList();
        for (JsonNode measures : measuregrps) {
            String date = this.utiDate.convertUtcToYyyyMmDd(measures.get("date").toString());
            date = date.substring(5);
            if (!existsDuplicate(dupliDateList, date)) {
                dupliDateList.add(date);

                WithingsObject wiObj = new WithingsObject();
                wiObj.utcDate = measures.get("date").longValue();
                wiObj.dateStr = date;
                Double value = Double.valueOf(measures.get("measures").get(0).get("value").asDouble());
                for (int unit = measures.get("measures").get(0).get("unit").asInt(); unit < 0; unit++) {
                    value = Double.valueOf(value.doubleValue() / 10.0D);
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

    private Map<String, String> adjustSteps(String rawDataForSteps)
            throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = (JsonNode) mapper.readValue(rawDataForSteps, JsonNode.class);
        JsonNode activities = node.get("body").get("activities");

        ArrayList<Integer> stepList = new ArrayList();
        for (JsonNode activity : activities) {
            stepList.add(Integer.valueOf(activity.get("steps").asInt()));
        }
        Object stepsMap = new HashMap();
        stepsMap = setStepsMap(stepList);

        return (Map<String, String>) stepsMap;
    }

    public void setStepsMeasures()
            throws IOException {
        String yesterday = this.utiDate.getYesterDayYyyyMmDd();
        String today = this.utiDate.getTodayYyyyMmDd();

        String rawDataForSteps = getRawDataForSteps(yesterday, today);
        Map<String, String> stepsMap = new HashMap();
        stepsMap = adjustSteps(rawDataForSteps);
        this.wiEnti.setYesterdaySteps((String) stepsMap.get("yesterday"));
        this.wiEnti.setTodaySteps((String) stepsMap.get("today"));
        this.wiEnti.setDifferenceSteps((String) stepsMap.get("difference"));
        this.wiEnti.setStepArrowIconPass((String) stepsMap.get("arrowIcon"));
        this.wiEnti.setStepEmoIconPass((String) stepsMap.get("emoIcon"));
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
        int diff = Integer.valueOf(diffrence).intValue();
        if (0 > diff) {
            stepsMap.put("difference", diffrence);
            stepsMap.put("arrowIcon", "../img/negative_down.png");
            stepsMap.put("emoIcon", "../img/bad.png");
        } else if (0 == diff) {
            stepsMap.put("difference", diffrence);
            stepsMap.put("arrowIcon", "../img/neutralArrow.png");
            stepsMap.put("emoIcon", "../img/neutralEmo.png");
        } else if (0 < diff) {
            stepsMap.put("difference", "+" + diffrence);
            stepsMap.put("arrowIcon", "../img/positive_up.png");
            stepsMap.put("emoIcon", "../img/good.png");
        }
        return stepsMap;
    }

    private void setWeightIconPass(Double diff) {
        if (0.0D > diff.doubleValue()) {
            this.wiEnti.setDifferenceWeight(String.valueOf(diff));
            this.wiEnti.setWeightArrowIconPass("../img/positive_down.png");
            this.wiEnti.setWeightEmoIconPass("../img/good.png");
        } else if (0.0D == diff.doubleValue()) {
            this.wiEnti.setDifferenceWeight(String.valueOf(diff));
            this.wiEnti.setWeightArrowIconPass("../img/neutralArrow.png");
            this.wiEnti.setWeightEmoIconPass("../img/neutralEmo.png");
        } else if (0.0D < diff.doubleValue()) {
            this.wiEnti.setDifferenceWeight("+" + String.valueOf(diff));
            this.wiEnti.setWeightArrowIconPass("../img/negative_up.png");
            this.wiEnti.setWeightEmoIconPass("../img/bad.png");
        }
    }

    public boolean isExistAccessToken(HttpSession session) {
        String userId = session.getAttribute("user_id").toString();
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
            JsonNode activities = node.get("body").get("activities");
            for (JsonNode activity : activities) {
                WithingsObject wiObj = new WithingsObject();
                String[] date = activity.get("date").toString().split("\"");
                wiObj.dateStr = date[1].substring(5);
                wiObj.steps = activity.get("steps").asInt();
                wiObj.utcDate = Long.valueOf(this.utiDate.convertStartUTC(date[1])).longValue();
                stepList.add(wiObj);
            }
            injectZeroDayData(dayList, stepList);
            Collections.sort(stepList, new WithingsComparator());
            if (dayCount > 60) {
                stepList = summarizeMonthStep(stepList);
            } else if (dayCount > 30) {
                stepList = summarizeWeekStep(stepList);
            }
            System.out.println("������������������������");
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
            System.out.println("������2������������������������������������������");
        } else if (1 >= stepList.size()) {
            yesterday = ((Integer) stepList.get(0)).intValue();
            diffrence = String.valueOf(today - yesterday);
            System.out.println("���������������������������������������������������������");
        } else {
            yesterday = ((Integer) stepList.get(0)).intValue();
            today = ((Integer) stepList.get(1)).intValue();
            diffrence = String.valueOf(today - yesterday);
        }
        Map<String, String> stepsMap = new HashMap();
        stepsMap.put("yesterday", String.valueOf(yesterday));
        stepsMap.put("today", String.valueOf(today));
        stepsMap = setStepIconPass(stepsMap, diffrence);

        return stepsMap;
    }

    private String getWeightJsonData(String from, String to) {
        String utcFrom = this.utiDate.convertStartUTC(from);
        String utcEnd = this.utiDate.convertEndUTC(to);
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        String userId = this.session.getAttribute("user_id").toString();
        this.tokenObj = this.db.findObj(userId);
        String USER_ID = this.tokenObj.getWi_userId();
        String ACCESS_TOKEN = this.tokenObj.getAccess_token();
        String ACCESS_TOKEN_SECRET = this.tokenObj.getAccess_token_secret();

        SortedMap<String, String> paramsMap = new TreeMap();
        try {
            paramsMap.put("action", "getmeas");
            paramsMap.put("userid", USER_ID);
            paramsMap.put("startdate", utcFrom);
            paramsMap.put("enddate", utcEnd);
            paramsMap.put("meastype", "1");
            paramsMap.put("oauth_consumer_key", "f1e9bebd38c1bf97b7c58bf2f5844c9bf7c38ec50254124d4f43b8582f0f");
            paramsMap.put("oauth_nonce", super.getRandomChar());
            paramsMap.put("oauth_signature_method", "HMAC-SHA1");
            paramsMap.put("oauth_timestamp", String.valueOf(super.getUnixTime()));
            paramsMap.put("oauth_token", ACCESS_TOKEN);
            paramsMap.put("oauth_version", "1.0");

            String sigData = super.makeSigData("f1e9bebd38c1bf97b7c58bf2f5844c9bf7c38ec50254124d4f43b8582f0f", "https://wbsapi.withings.net/measure", paramsMap, "GET");

            String sigKey = super.makeSigKey("c17484ff357d801897828f674bb6175b4f94340516ce5bb4922def7e035", ACCESS_TOKEN_SECRET);

            URL url = new URL("https://wbsapi.withings.net/measure?action=getmeas&userid=" + URLEncode(USER_ID) + "&startdate=" + URLEncode((String) paramsMap.get("startdate")) + "&enddate=" + URLEncode((String) paramsMap.get("enddate")) + "&meastype=" + URLEncode((String) paramsMap.get("meastype")) + "&oauth_consumer_key=" + URLEncode((String) paramsMap.get("oauth_consumer_key")) + "&oauth_nonce=" + URLEncode((String) paramsMap.get("oauth_nonce")) + "&oauth_signature=" + URLEncode(super.makeSignature(sigKey, sigData)) + "&oauth_signature_method=" + URLEncode((String) paramsMap.get("oauth_signature_method")) + "&oauth_timestamp=" + URLEncode((String) paramsMap.get("oauth_timestamp")) + "&oauth_token=" + URLEncode(ACCESS_TOKEN) + "&oauth_version=" + URLEncode((String) paramsMap.get("oauth_version")));

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            return reader.readLine();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    Logger.getLogger(Withings.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
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
            wiObj.dateStr = (start + "���" + end);
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
                        if (!extWeight.equals(Double.valueOf(0.0D))) {
                            if (((WithingsObject) sumWeightList.get(i)).weight.equals(Double.valueOf(0.0D))) {
                                ((WithingsObject) sumWeightList.get(i)).weight = extWeight;
                            } else if (extWeight.doubleValue() < ((WithingsObject) sumWeightList.get(i)).weight.doubleValue()) {
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
            Double extWeight = Double.valueOf(0.0D);
            for (int j = 0; j < 7; j++) {
                if (i >= weightList.size()) {
                    break;
                }
                Double weight = ((WithingsObject) weightList.get(i)).weight;
                if (!weight.equals(Double.valueOf(0.0D))) {
                    if (extWeight.equals(Double.valueOf(0.0D))) {
                        extWeight = weight;
                    } else if (weight.doubleValue() < extWeight.doubleValue()) {
                        extWeight = weight;
                    }
                }
                i += 1;
            }
            String end = findEndDate(i - 1, weightList);
            WithingsObject wiObj = new WithingsObject();
            wiObj.dateStr = (start + "���" + end);
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
                wiObj.utcDate = Long.valueOf(this.utiDate.convertStartUTC(day)).longValue();
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
                    wiObj.utcDate = Long.valueOf(this.utiDate.convertStartUTC((String) dayList.get(i))).longValue();
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
