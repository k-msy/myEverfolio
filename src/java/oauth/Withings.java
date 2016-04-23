/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oauth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import entity.WithingsEnti;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
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

    SortedMap<String, String> paramsMap;

    private int difference = 0;

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

    private String getRawDataForSteps() throws IOException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        String ACCESS_TOKEN = session.getAttribute("access_token").toString();
        String ACCESS_TOKEN_SECRET = session.getAttribute("access_token_secret").toString();
        String USER_ID = session.getAttribute("userid").toString();

        try {
            this.paramsMap.clear();

            paramsMap.put("action", "getactivity");

            Map<String, String> dateMap = new HashMap();
            dateMap = getTodayAndYesterday(dateMap);
            paramsMap.put("startdateymd", dateMap.get("yesterday"));
            paramsMap.put("enddateymd", dateMap.get("today"));
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

            String[] result = null;
            result = text.split("\\[");
            String[] jsonText = result[1].split("\\]");
            jsonText[0] = "[" + jsonText[0] + "]";
            System.out.println("jsonText[0]=" + jsonText[0]);

            return jsonText[0];
            //return text;
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

    public String setWeightMeasures() throws IOException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        String USER_ID = session.getAttribute("userid").toString();
        String ACCESS_TOKEN = session.getAttribute("access_token").toString();
        String ACCESS_TOKEN_SECRET = session.getAttribute("access_token_secret").toString();
        try {
            this.paramsMap.clear();

            paramsMap.put("action", "getmeas");
            paramsMap.put("userid", USER_ID);
            Map<String, String> utcMap = new HashMap();
            utcMap = getUTC_TodayAndYesterday(utcMap);
            System.out.println("yesterday=" + utcMap.get("yesterday"));
            System.out.println("today=" + utcMap.get("today"));
            paramsMap.put("startdate", utcMap.get("yesterday"));
            paramsMap.put("enddate", utcMap.get("today"));
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

            String jsonText = reader.readLine();

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

            return jsonText;
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

    private Map<String, String> getTodayAndYesterday(Map<String, String> dateMap) {
        //今日の日付を取得、yyyy-MM-ddに変換
        Date date = new Date();
        SimpleDateFormat formatted = new SimpleDateFormat("yyyy-MM-dd");
        dateMap.put("today", formatted.format(date));

        //昨日の日付を取得、yyyy-MM-ddに変換
        Calendar yesterday = Calendar.getInstance();
        yesterday.setTime(date);
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        dateMap.put("yesterday", formatted.format(yesterday.getTime()));
        return dateMap;
    }

    private Map<String, String> getUTC_TodayAndYesterday(Map<String, String> utcMap) {
        ZonedDateTime d = ZonedDateTime.now().plusDays(1).truncatedTo(ChronoUnit.DAYS);
        Long nowTime = d.toEpochSecond() - 1;
        //Long nowTime = System.currentTimeMillis() / 1000L;
        //Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        //今日の日付を取得（23時59分59秒まで）
        utcMap.put("today", (nowTime).toString());
        //昨日の日付を取得（0時0分0秒から）
        Long pastTime = nowTime - 172799;
        utcMap.put("yesterday", pastTime.toString());

        return utcMap;
    }

    private Map<String, String> adjustSteps(String rawDataForSteps) throws IOException {
        //任意の期間中のJSONデータをListに詰め替える
        ObjectMapper mapper = new ObjectMapper();
        ArrayList<WithingsEnti> wiList = new ArrayList<WithingsEnti>();
        //「,」で区切られたresultの1つを試しにパースできるかどうか確認する
        wiList = mapper.readValue(rawDataForSteps, new TypeReference<ArrayList<WithingsEnti>>() {
        });

        //Listから「歩数」のみ抽出して、Listに詰め替える
        ArrayList<Integer> stepList = new ArrayList<Integer>();
        for (int i = 0; i < wiList.size(); i++) {
            stepList.add(wiList.get(i).getSteps());
            System.out.println(String.valueOf(i) + "番目のステップ数 =" + wiList.get(i).getSteps());
        }
        System.out.println("ステップリスト内の要素数：" + String.valueOf(stepList.size()));

        Map<String, String> stepsMap = new HashMap<>();
        if (1 >= stepList.size()) {
            //1日分しか歩数データが取れなかった場合
            System.out.println("今日のデータが同期されてないっぽいです");
        } else {
            //2日分歩数データが取れた場合
            int yesterday = stepList.get(0);
            int today = stepList.get(1);
            String diffrence = String.valueOf(today - yesterday);
            stepsMap.put("yesterday", String.valueOf(yesterday));
            stepsMap.put("today", String.valueOf(today));
            stepsMap = setIconPass(stepsMap, diffrence);
        }
        return stepsMap;
    }

    private Map<String, String> setIconPass(Map<String, String> stepsMap, String diffrence) {
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

    public void setStepsMeasures() throws IOException {
        String rawDataForSteps = getRawDataForSteps();
        Map<String, String> stepsMap = new HashMap<>();
        stepsMap = adjustSteps(rawDataForSteps);
        wiEnti.setYesterdaySteps(stepsMap.get("yesterday"));
        wiEnti.setTodaySteps(stepsMap.get("today"));
        wiEnti.setDifferenceSteps(stepsMap.get("difference"));
        wiEnti.setStepArrowIconPass(stepsMap.get("arrowIcon"));
        wiEnti.setStepEmoIconPass(stepsMap.get("emoIcon"));
    }

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

    /*        

     }
     */



    public void setRangeStepsMeasures(Date from, Date to) {
        
    }

    public void setRangeWeightMeasures(Date from, Date to) {
        
    }
}
