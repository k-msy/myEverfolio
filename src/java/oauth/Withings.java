/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import entity.WithingsEnti;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import util.UtilDate;

/**
 *
 * @author bpg0129
 */
@SessionScoped
public class Withings extends SuperOauth {

    // OAuthにおいて利用する変数宣言
    private static final String CONSUMER_KEY = "f1e9bebd38c1bf97b7c58bf2f5844c9bf7c38ec50254124d4f43b8582f0f";
    private static final String CONSUMER_SECRET = "c17484ff357d801897828f674bb6175b4f94340516ce5bb4922def7e035";
    private static final String REQUEST_TOKEN_URL = "https://oauth.withings.com/account/request_token";
    private static final String AUTHORIZE_URL = "https://oauth.withings.com/account/authorize";
    private static final String ACCESS_TOKEN_URL = "https://oauth.withings.com/account/access_token";
    private static final String OAUTH_CALLBACK = "http://127.0.0.1:8080/myEverfolio/faces/main/callback/top.xhtml?faces-redirect=true";
    private static final String method = "GET";
    
    HttpServletRequest request = getRequest();
    HttpSession session = request.getSession(true);
    
    private String sigKey;
    private String sigData;
    
    @Inject
    WithingsEnti wiEnti;
    
    @Inject
    UtilDate utiDate;
    
    SortedMap<String, String> paramsMap;
    
    public Withings() {
        this.paramsMap = super.makeParam(CONSUMER_KEY);
        paramsMap.put("oauth_callback", URLEncode(OAUTH_CALLBACK));
        this.sigData = super.makeSigData(CONSUMER_KEY, REQUEST_TOKEN_URL, paramsMap, method);
        this.sigKey = super.makeSigKey(CONSUMER_SECRET, "");
    }
    
    public void verify() {
        
        try {
            getRequestToken();
            sendRedirect();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private void getRequestToken() throws IOException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            // 署名をパラメータに追加
            this.paramsMap.put("oauth_signature", super.makeSignature(sigKey, sigData));
            
            URL url = new URL(REQUEST_TOKEN_URL
                    + "?oauth_callback=" + URLEncode(OAUTH_CALLBACK)
                    + "&oauth_consumer_key=" + URLEncode(paramsMap.get("oauth_consumer_key"))
                    + "&oauth_nonce=" + URLEncode(paramsMap.get("oauth_nonce"))
                    + "&oauth_signature=" + URLEncode(paramsMap.get("oauth_signature"))
                    + "&oauth_signature_method=" + URLEncode(paramsMap.get("oauth_signature_method"))
                    + "&oauth_timestamp=" + URLEncode(paramsMap.get("oauth_timestamp"))
                    + "&oauth_version=" + URLEncode(paramsMap.get("oauth_version")));
            
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.connect();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String reqTokenResult;
            reqTokenResult = reader.readLine();
            if (!"".equals(reqTokenResult)) {
                String[] split = extractToken(reqTokenResult);
                session.setAttribute("request_token", split[0]);
                session.setAttribute("request_token_secret", split[1]);
            } else {
                Date date = new Date();
                System.out.println("withingsリクエストトークン取得失敗" + new Date().toString());
            }
        } catch (IOException e) {
            if (e instanceof IOException) {
                e.printStackTrace();
            } else {
                System.out.println("他のException");
            }
            
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    private void sendRedirect() {
        //
        String REQUEST_TOKEN = session.getAttribute("request_token").toString();
        String REQUEST_TOKEN_SECRET = session.getAttribute("request_token_secret").toString();
        
        SortedMap<String, String> paramsMap = new TreeMap<>();
        paramsMap.put("oauth_consumer_key", CONSUMER_KEY);
        paramsMap.put("oauth_nonce", super.getRandomChar());
        paramsMap.put("oauth_timestamp", String.valueOf(super.getUnixTime()));
        paramsMap.put("oauth_token", REQUEST_TOKEN);
        paramsMap.put("oauth_signature_method", "HMAC-SHA1");
        paramsMap.put("oauth_version", "1.0");
        
        this.sigData = super.makeSigData(CONSUMER_KEY, AUTHORIZE_URL, paramsMap, method);
        this.sigKey = super.makeSigKey(CONSUMER_SECRET, REQUEST_TOKEN_SECRET);
        
        String redirectUrl = AUTHORIZE_URL
                + "?oauth_consumer_key=" + URLEncode(paramsMap.get("oauth_consumer_key"))
                + "&oauth_nonce=" + URLEncode(paramsMap.get("oauth_nonce"))
                + "&oauth_signature=" + URLEncode(super.makeSignature(sigKey, sigData))
                + "&oauth_signature_method=" + URLEncode(paramsMap.get("oauth_signature_method"))
                + "&oauth_timestamp=" + URLEncode(paramsMap.get("oauth_timestamp"))
                + "&oauth_token=" + URLEncode(paramsMap.get("oauth_token"))
                + "&oauth_version=" + URLEncode(paramsMap.get("oauth_version"));
        
        try {
            HttpServletResponse response = getResponse();
            response.sendRedirect(redirectUrl);
        } catch (IOException ex) {
            Logger.getLogger(Withings.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void getAccessToken() throws IOException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        String OAUTH_TOKEN = session.getAttribute("oauth_token").toString();
        String REQEST_TOKEN_SECRET = session.getAttribute("request_token_secret").toString();
        try {
            this.paramsMap.clear();
            
            paramsMap.put("oauth_consumer_key", CONSUMER_KEY);
            paramsMap.put("oauth_nonce", super.getRandomChar());
            paramsMap.put("oauth_timestamp", String.valueOf(super.getUnixTime()));
            paramsMap.put("oauth_token", OAUTH_TOKEN);
            paramsMap.put("oauth_signature_method", "HMAC-SHA1");
            paramsMap.put("oauth_version", "1.0");
            
            this.sigData = super.makeSigData(CONSUMER_KEY, ACCESS_TOKEN_URL, paramsMap, method);
            this.sigKey = super.makeSigKey(CONSUMER_SECRET, REQEST_TOKEN_SECRET);

            // 署名をパラメータに追加
            this.paramsMap.put("oauth_signature", super.makeSignature(sigKey, sigData));
            
            URL url = new URL(ACCESS_TOKEN_URL
                    + "?oauth_consumer_key=" + URLEncode(paramsMap.get("oauth_consumer_key"))
                    + "&oauth_nonce=" + URLEncode(paramsMap.get("oauth_nonce"))
                    + "&oauth_signature=" + URLEncode(paramsMap.get("oauth_signature"))
                    + "&oauth_signature_method=" + URLEncode(paramsMap.get("oauth_signature_method"))
                    + "&oauth_timestamp=" + URLEncode(paramsMap.get("oauth_timestamp"))
                    + "&oauth_token=" + URLEncode(OAUTH_TOKEN)
                    + "&oauth_version=" + URLEncode(paramsMap.get("oauth_version")));
            
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.connect();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String[] split = extractToken(reader.readLine());
            System.out.println("access_token=" + split[0]);
            System.out.println("access_token_secret=" + split[1]);
            session.setAttribute("access_token", split[0]);
            session.setAttribute("access_token_secret", split[1]);
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
    }
    
    private String[] extractToken(String chars) {
        String[] result = null;
        try {
            result = chars.split("&");
            for (int i = 0; i < result.length; i++) {
                String[] a = result[i].split("=");
                result[i] = a[1];
            }
        } catch (PatternSyntaxException ex) {
            System.out.println("のっとぱたーんまっち");
        }
        if (result == null || result.length == 0) {
            result[0] = "";
        }
        return result;
    }

    /**
     * WithingsAPIより、指定期日の歩数データを取得する
     *
     * @param from
     * @param to
     * @return
     * @throws IOException
     */
    private String getRawDataForSteps(String from, String to) throws IOException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        String ACCESS_TOKEN = session.getAttribute("access_token").toString();
        String ACCESS_TOKEN_SECRET = session.getAttribute("access_token_secret").toString();
        String USER_ID = session.getAttribute("userid").toString();
        
        try {
            this.paramsMap.clear();
            
            paramsMap.put("action", "getactivity");
            paramsMap.put("startdateymd", from);
            paramsMap.put("enddateymd", to);
            paramsMap.put("oauth_consumer_key", CONSUMER_KEY);
            paramsMap.put("oauth_nonce", super.getRandomChar());
            paramsMap.put("oauth_timestamp", String.valueOf(super.getUnixTime()));
            paramsMap.put("oauth_token", ACCESS_TOKEN);
            paramsMap.put("oauth_signature_method", "HMAC-SHA1");
            paramsMap.put("oauth_version", "1.0");
            paramsMap.put("userid", USER_ID);
            
            this.sigData = super.makeSigData(CONSUMER_KEY, "https://wbsapi.withings.net/v2/measure", paramsMap, method);
            //System.out.println("this.sigData=" + this.sigData);
            this.sigKey = super.makeSigKey(CONSUMER_SECRET, ACCESS_TOKEN_SECRET);
            
            URL url = new URL("https://wbsapi.withings.net/v2/measure"
                    + "?action=" + "getactivity"
                    + "&userid=" + URLEncode(USER_ID)
                    + "&startdateymd=" + URLEncode(paramsMap.get("startdateymd"))
                    + "&enddateymd=" + URLEncode(paramsMap.get("enddateymd"))
                    + "&oauth_consumer_key=" + URLEncode(paramsMap.get("oauth_consumer_key"))
                    + "&oauth_nonce=" + URLEncode(paramsMap.get("oauth_nonce"))
                    + "&oauth_signature=" + URLEncode(super.makeSignature(sigKey, sigData))
                    + "&oauth_signature_method=" + URLEncode(paramsMap.get("oauth_signature_method"))
                    + "&oauth_timestamp=" + URLEncode(paramsMap.get("oauth_timestamp"))
                    + "&oauth_token=" + URLEncode(ACCESS_TOKEN)
                    + "&oauth_version=" + URLEncode(paramsMap.get("oauth_version")));
            
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
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
    
    public void setWeightMeasures() throws IOException {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        
        String yesterday = utiDate.getYesterDayYyyyMmDd(date, formatter);
        String today = utiDate.formatYyyyMmDd(date, formatter);
        
        String jsonText = getWeightJsonData(yesterday, today);

        //任意の期間中のJSONデータをListに詰め替える
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readValue(jsonText, JsonNode.class);
        //updateTime取得
        String updateTime = node.get("body").get("updatetime").toString();
        //measuregrps取得
        JsonNode measuregrps = node.get("body").get("measuregrps");
        
        ArrayList<Double> weightList = new ArrayList<>();
        for (JsonNode measures : measuregrps) {
            Double value = measures.get("measures").get(0).get("value").asDouble();
            System.out.println("value =" + String.valueOf(value));
            //int unit = measures.get("measures").get(0).get("unit").asInt();

            for (int unit = measures.get("measures").get(0).get("unit").asInt(); unit < 0; unit++) {
                value = value / 10;
            }
            weightList.add(value);
            System.out.println("realValue =" + String.valueOf(value));
            //System.out.println("unit =" + unit);
        }
        if (weightList.size() < 0) {
            //直近2日にかけて、体重計に乗っていない場合
            System.out.println("体重計に乗って、現実を見よう。");
        } else if (weightList.size() == 1) {
            //体重データが1つの場合
            Double current = weightList.get(0);
            wiEnti.setCurrentWeight(String.valueOf(current));
            setWeightIconPass(0.0);
        } else if (weightList.size() >= 2) {
            //体重データが2つ以上の場合
            Double current = weightList.get(0);
            Double past = weightList.get(1);
            BigDecimal difference = new BigDecimal(current - past);
            difference = difference.setScale(1, BigDecimal.ROUND_DOWN);
            System.out.println("difference =" + String.valueOf(difference.doubleValue()));
            wiEnti.setPastWeight(String.valueOf(past));
            wiEnti.setCurrentWeight(String.valueOf(current));
            setWeightIconPass(difference.doubleValue());
        }
        
    }

    /**
     * 範囲期日中の体重を取得・設定する
     *
     * @param from
     * @param to
     * @return
     */
    private ArrayList<String[]> getRangeWeightMeasures(String start, String end) {
        
        String jsonText = getWeightJsonData(start, end);

        //任意の期間中のJSONデータをListに詰め替える
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = null;
        try {
            node = mapper.readValue(jsonText, JsonNode.class);
        } catch (IOException ex) {
            Logger.getLogger(Withings.class.getName()).log(Level.SEVERE, null, ex);
        }
        //measuregrps取得
        JsonNode measuregrps = node.get("body").get("measuregrps");
        
        ArrayList<String[]> weightList = new ArrayList<String[]>();
        for (JsonNode measures : measuregrps) {
            String[] weights = new String[2];
            weights[0] = utiDate.convertUtcToYyyyMmDd(measures.get("date").toString());
            Double value = measures.get("measures").get(0).get("value").asDouble();
            System.out.println("value =" + String.valueOf(value));

            for (int unit = measures.get("measures").get(0).get("unit").asInt(); unit < 0; unit++) {
                value = value / 10;
            }
            weights[1] = String.valueOf(value);
            weightList.add(0,weights);
        }
        
        return weightList;
    }
    
    private Map<String, String> adjustSteps(String rawDataForSteps) throws IOException {
        //任意の期間中のJSONデータをListに詰め替える
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readValue(rawDataForSteps, JsonNode.class);
        JsonNode activities = node.get("body").get("activities");
        
        ArrayList<Integer> stepList = new ArrayList<Integer>();
        for (JsonNode activity : activities) {
            stepList.add(activity.get("steps").asInt());
        }
        
        Map<String, String> stepsMap = new HashMap<>();
        stepsMap = setStepsMap(stepList);
        
        return stepsMap;
    }

    /**
     * 昨日と今日の歩数を設定する
     *
     * @throws IOException
     */
    public void setStepsMeasures() throws IOException {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        
        String yesterday = utiDate.getYesterDayYyyyMmDd(date, formatter);
        String today = utiDate.formatYyyyMmDd(date, formatter);
        
        String rawDataForSteps = getRawDataForSteps(yesterday, today);
        Map<String, String> stepsMap = new HashMap<>();
        stepsMap = adjustSteps(rawDataForSteps);
        wiEnti.setYesterdaySteps(stepsMap.get("yesterday"));
        wiEnti.setTodaySteps(stepsMap.get("today"));
        wiEnti.setDifferenceSteps(stepsMap.get("difference"));
        wiEnti.setStepArrowIconPass(stepsMap.get("arrowIcon"));
        wiEnti.setStepEmoIconPass(stepsMap.get("emoIcon"));
    }

    /**
     * 範囲期日中の歩数を設定する
     *
     * @param start
     * @param end
     */
    public void setRangeMeasures(Date start, Date end) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String from = utiDate.formatYyyyMmDd(start, formatter);
        String to = utiDate.formatYyyyMmDd(end, formatter);
        
        ArrayList<String[]> stepList = getRangeStepMeasures(from, to);
        ArrayList<String[]> weightList = getRangeWeightMeasures(from, to);
        
        System.out.println("hogehoge");
    }

    /**
     * 歩数_アイコン設定
     *
     * @param stepsMap
     * @param diffrence
     * @return
     */
    private Map<String, String> setStepIconPass(Map<String, String> stepsMap, String diffrence) {
        int diff = Integer.valueOf(diffrence);
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

    /**
     * 体重_アイコン設定
     *
     * @param diff
     */
    private void setWeightIconPass(Double diff) {
        if (0.0 > diff) {
            wiEnti.setDifferenceWeight(String.valueOf(diff));
            wiEnti.setWeightArrowIconPass("../img/positive_down.png");
            wiEnti.setWeightEmoIconPass("../img/good.png");
        } else if (0.0 == diff) {
            wiEnti.setDifferenceWeight(String.valueOf(diff));
            wiEnti.setWeightArrowIconPass("../img/neutralArrow.png");
            wiEnti.setWeightEmoIconPass("../img/neutralEmo.png");
        } else if (0.0 < diff) {
            wiEnti.setDifferenceWeight("+" + String.valueOf(diff));
            wiEnti.setWeightArrowIconPass("../img/negative_up.png");
            wiEnti.setWeightEmoIconPass("../img/bad.png");
        }
    }

    /**
     * アクセストークンの存在チェック
     *
     * @param session
     * @return
     */
    public boolean isExistAccessToken(HttpSession session) {
        boolean exist = true;
        if (session.getAttribute("request_token") == null) {
            verify();
            exist = false;
        } else if (session.getAttribute("access_token") == null) {
            try {
                getAccessToken();
                exist = false;
            } catch (IOException ex) {
                System.out.println("getAccessTokenでなんらかの例外キャッチしたよ");
            }
        }
        return exist;
    }

    /**
     * 範囲期日中の歩数を取得・設定する
     *
     * @param from
     * @param to
     * @return
     */
    private ArrayList<String[]> getRangeStepMeasures(String from, String to) {
        ArrayList<String[]> stepList = new ArrayList<String[]>();
        try {
            String jsonText = getRawDataForSteps(from, to);
            //任意の期間中のJSONデータをListに詰め替える
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(jsonText, JsonNode.class);
            JsonNode activities = node.get("body").get("activities");
                        
            for (JsonNode activity : activities) {
                String[] steps = new String[2];
                String[] date = activity.get("date").toString().split("\"");
                steps[0] = date[1];
                steps[1] = activity.get("steps").toString();
                stepList.add(steps);
            }
        } catch (IOException ex) {
            Logger.getLogger(Withings.class.getName()).log(Level.SEVERE, null, ex);
        }
        return stepList;
    }

    /**
     * 昨日と今日の歩数とアイコン等を設定
     *
     * @param stepList
     * @return
     */
    private Map<String, String> setStepsMap(ArrayList<Integer> stepList) {
        int yesterday = 0;
        int today = 0;
        String diffrence;
        if (0 == stepList.size()) {
            diffrence = String.valueOf(today - yesterday);
            System.out.println("最近2日間の歩数データがありません");
        } else if (1 >= stepList.size()) {
            yesterday = stepList.get(0);
            diffrence = String.valueOf(today - yesterday);
            System.out.println("今日のデータが同期されてないっぽいです");
        } else {
            yesterday = stepList.get(0);
            today = stepList.get(1);
            diffrence = String.valueOf(today - yesterday);
        }
        Map<String, String> stepsMap = new HashMap<>();
        stepsMap.put("yesterday", String.valueOf(yesterday));
        stepsMap.put("today", String.valueOf(today));
        stepsMap = setStepIconPass(stepsMap, diffrence);
        
        return stepsMap;
    }
    
    private String getWeightJsonData(String from, String to) {
        String utcFrom = utiDate.convertStartUTC(from);
        String utcEnd = utiDate.convertEndUTC(to);
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        String USER_ID = session.getAttribute("userid").toString();
        String ACCESS_TOKEN = session.getAttribute("access_token").toString();
        String ACCESS_TOKEN_SECRET = session.getAttribute("access_token_secret").toString();
        try {
            this.paramsMap.clear();
            
            paramsMap.put("action", "getmeas");
            paramsMap.put("userid", USER_ID);
            paramsMap.put("startdate", utcFrom);
            paramsMap.put("enddate", utcEnd);
            paramsMap.put("meastype", "1");
            paramsMap.put("oauth_consumer_key", CONSUMER_KEY);
            paramsMap.put("oauth_nonce", super.getRandomChar());
            paramsMap.put("oauth_signature_method", "HMAC-SHA1");
            paramsMap.put("oauth_timestamp", String.valueOf(super.getUnixTime()));
            paramsMap.put("oauth_token", ACCESS_TOKEN);
            paramsMap.put("oauth_version", "1.0");
            
            this.sigData = super.makeSigData(CONSUMER_KEY, "https://wbsapi.withings.net/measure", paramsMap, method);
            //System.out.println("this.sigData=" + this.sigData);
            this.sigKey = super.makeSigKey(CONSUMER_SECRET, ACCESS_TOKEN_SECRET);
            
            URL url = new URL("https://wbsapi.withings.net/measure"
                    + "?action=" + "getmeas"
                    + "&userid=" + URLEncode(USER_ID)
                    + "&startdate=" + URLEncode(paramsMap.get("startdate"))
                    + "&enddate=" + URLEncode(paramsMap.get("enddate"))
                    + "&meastype=" + URLEncode(paramsMap.get("meastype"))
                    + "&oauth_consumer_key=" + URLEncode(paramsMap.get("oauth_consumer_key"))
                    + "&oauth_nonce=" + URLEncode(paramsMap.get("oauth_nonce"))
                    + "&oauth_signature=" + URLEncode(super.makeSignature(sigKey, sigData))
                    + "&oauth_signature_method=" + URLEncode(paramsMap.get("oauth_signature_method"))
                    + "&oauth_timestamp=" + URLEncode(paramsMap.get("oauth_timestamp"))
                    + "&oauth_token=" + URLEncode(ACCESS_TOKEN)
                    + "&oauth_version=" + URLEncode(paramsMap.get("oauth_version")));
            
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
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
    
}
