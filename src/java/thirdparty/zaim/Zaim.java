package thirdparty.zaim;

import bean.HeaderBb;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import db.ZaimDb;
import entity.Token_zaim;
import entity.ZaimEnti;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import oauth.Ozaim;
import oauth.SuperOauth;
import util.UtilDate;
import util.UtilLogic;
import view.chart.BarChart;

@Named
@RequestScoped
public class Zaim
        extends SuperOauth {

    private static final String method = "GET";
    HttpServletRequest request = getRequest();
    HttpSession session = this.request.getSession(true);
    @Inject
    UtilDate utiDate;
    @Inject
    UtilLogic utiLogic;
    @Inject
    ZaimEnti zaEnti;
    @Inject
    Ozaim ozaim;
    @Inject
    Token_zaim tokenObj;
    @Inject
    BarChart barChart;
    @Inject
    HeaderBb headerBb;
    @EJB
    ZaimDb db;

    public Boolean doesCooperate(HttpSession session) {
        boolean coop = true;
        if (isExistAccessToken(session)) {
            this.headerBb.setZaimCoopFlg(true);
            coop = true;
        } else {
            this.headerBb.setZaimCoopFlg(false);
            coop = false;
        }
        return Boolean.valueOf(coop);
    }

    public boolean isExistAccessToken(HttpSession session) {
        String userId = session.getAttribute("user_id").toString();
        this.tokenObj = this.db.findObj(userId);
        boolean exist = true;
        if (("".equals(this.tokenObj.getAccess_token())) || ("".equals(this.tokenObj.getAccess_token_secret()))) {
            exist = this.ozaim.isCallback(session);
        }
        return exist;
    }

    public void setMoneyMeasures() {
        String from = this.utiDate.getTodayYyyyMmDd();
        String to = this.utiDate.getTodayYyyyMmDd();
        try {
            String jsonText = getRawDataForMoney(from, to);
            System.out.println("moneyJsonText=" + jsonText);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = (JsonNode) mapper.readValue(jsonText, JsonNode.class);
            JsonNode moneyNodeList = node.get("money");

            ArrayList<String[]> paymentList = new ArrayList();
            ArrayList<String[]> incomeList = new ArrayList();
            for (JsonNode moneyNode : moneyNodeList) {
                String[] mode = moneyNode.get("mode").toString().split("\"");
                if ("payment".equals(mode[1])) {
                    paymentList = addTodayPaymentList(moneyNode, paymentList);
                } else if ("income".equals(mode[1])) {
                    incomeList = addTodayIncomeList(moneyNode, incomeList);
                }
            }
            this.zaEnti.setSumPayment(sumPayment(paymentList));
            this.zaEnti.setSumIncome(sumIncome(incomeList));
            this.zaEnti.setPaymentList(formatPaymentList(paymentList));
            this.zaEnti.setIncomeList(formatIncomeList(incomeList));
        } catch (IOException ex) {
            Logger.getLogger(Zaim.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setRangeMeasures(Date start, Date end, ArrayList<String> dayList, int dayCount) {
        try {
            String from = this.utiDate.formatYyyyMmDd(start);
            String to = this.utiDate.formatYyyyMmDd(end);
            String jsonText = getRawDataForMoney(from, to);
            System.out.println("moneyRangeJsonText=" + jsonText);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = (JsonNode) mapper.readValue(jsonText, JsonNode.class);
            JsonNode moneyNodeList = node.get("money");

            ArrayList<ZaimObject> paymentList = new ArrayList();
            ArrayList<ZaimObject> incomeList = new ArrayList();
            for (JsonNode moneyNode : moneyNodeList) {
                String[] mode = moneyNode.get("mode").toString().split("\"");
                if ("payment".equals(mode[1])) {
                    paymentList = addRangeMoneyList(moneyNode, paymentList, "payment");
                    System.out.println("made paymentList !!");
                } else if ("income".equals(mode[1])) {
                    incomeList = addRangeMoneyList(moneyNode, incomeList, "income");
                }
            }
            injectZeroDayData(dayList, paymentList);

            Collections.sort(paymentList, new ZaimComparator());
            if (paymentList.size() > 60) {
                paymentList = summarizeMonthDuration(paymentList);
            } else if (paymentList.size() > 31) {
                paymentList = summarizeWeekDuration(paymentList);
            }
            this.barChart.setBarModelZaim(paymentList, incomeList, dayList);
        } catch (IOException ex) {
            Logger.getLogger(Zaim.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String getRawDataForMoney(String from, String to) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        String userId = this.session.getAttribute("user_id").toString();
        this.tokenObj = this.db.findObj(userId);

        String access_token = this.tokenObj.getAccess_token();
        String access_token_secret = this.tokenObj.getAccess_token_secret();

        SortedMap<String, String> paramsMap = new TreeMap();
        try {
            paramsMap.put("start_date", from);
            paramsMap.put("end_date", to);
            paramsMap.put("oauth_consumer_key", "b12800bf82cfe709683c9b812d35fc450efb8bc4");
            paramsMap.put("oauth_nonce", super.getRandomChar());
            paramsMap.put("oauth_signature_method", "HMAC-SHA1");
            paramsMap.put("oauth_timestamp", String.valueOf(super.getUnixTime()));
            paramsMap.put("oauth_token", access_token);
            paramsMap.put("oauth_version", "1.0");
            String sigKey = super.makeSigKey("6a1b7a0ad40bdbd3d9b4d0a64fb757d10a606af4", access_token_secret);
            String sigData = super.makeSigData("b12800bf82cfe709683c9b812d35fc450efb8bc4", "https://api.zaim.net/v2/home/money", paramsMap, "GET");

            URL url = new URL("https://api.zaim.net/v2/home/money?start_date=" + URLEncode((String) paramsMap.get("start_date")) + "&end_date=" + URLEncode((String) paramsMap.get("end_date")) + "&oauth_consumer_key=" + URLEncode((String) paramsMap.get("oauth_consumer_key")) + "&oauth_nonce=" + URLEncode((String) paramsMap.get("oauth_nonce")) + "&oauth_signature=" + URLEncode(super.makeSignature(sigKey, sigData)) + "&oauth_signature_method=" + URLEncode((String) paramsMap.get("oauth_signature_method")) + "&oauth_timestamp=" + URLEncode((String) paramsMap.get("oauth_timestamp")) + "&oauth_token=" + URLEncode(access_token) + "&oauth_version=" + URLEncode((String) paramsMap.get("oauth_version")));

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
                    Logger.getLogger(Zaim.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return "";
    }

    private ArrayList<String[]> addTodayPaymentList(JsonNode moneyNode, ArrayList<String[]> paymentList) {
        String[] payment = new String[4];
        String uniqueId;
        if ("".equals(moneyNode.get("receipt_id").toString())) {
            uniqueId = moneyNode.get("id").toString();
        } else {
            uniqueId = moneyNode.get("receipt_id").toString();
        }
        if (0 == paymentList.size()) {
            payment[0] = uniqueId;
            payment[1] = moneyNode.get("category_id").toString();
            payment[2] = moneyNode.get("genre_id").toString();
            payment[3] = moneyNode.get("amount").toString();
            paymentList.add(payment);
        } else {
            int index = this.utiLogic.getSameValueIndex(paymentList, uniqueId);
            if (0 <= index) {
                String[] tmpPayment = (String[]) paymentList.get(index);
                tmpPayment[3] = String.valueOf(Long.valueOf(tmpPayment[3]).longValue() + moneyNode.get("amount").asLong());
                paymentList.set(index, tmpPayment);
            } else {
                payment[0] = uniqueId;
                payment[1] = moneyNode.get("category_id").toString();
                payment[2] = moneyNode.get("genre_id").toString();
                payment[3] = moneyNode.get("amount").toString();
                paymentList.add(payment);
            }
        }
        return paymentList;
    }

    private ArrayList<String[]> addTodayIncomeList(JsonNode moneyNode, ArrayList<String[]> incomeList) {
        String[] income = new String[3];

        income[0] = moneyNode.get("category_id").toString();
        income[1] = moneyNode.get("genre_id").toString();
        income[2] = moneyNode.get("amount").toString();

        incomeList.add(income);

        return incomeList;
    }

    private ArrayList<ZaimObject> addRangeMoneyList(JsonNode moneyNode, ArrayList<ZaimObject> moneyList, String type) {
        ZaimObject obj = new ZaimObject();

        String[] dateStr = moneyNode.get("date").toString().split("\"");
        String yyyy_mm_dd = dateStr[1];
        if (type.equals("payment")) {
            if (0 == moneyList.size()) {
                obj.dateStr = yyyy_mm_dd.substring(5);
                obj.utcDate = Long.valueOf(this.utiDate.convertStartUTC(yyyy_mm_dd)).longValue();
                obj.payment = moneyNode.get("amount").asInt();
                moneyList.add(obj);
            } else {
                int index = getSameValueIndex(moneyList, yyyy_mm_dd.substring(5));
                if (0 <= index) {
                    ZaimObject tmp = (ZaimObject) moneyList.get(index);
                    tmp.payment += moneyNode.get("amount").asInt();
                    moneyList.set(index, tmp);
                } else {
                    obj.dateStr = yyyy_mm_dd.substring(5);
                    obj.utcDate = Long.valueOf(this.utiDate.convertStartUTC(yyyy_mm_dd)).longValue();
                    obj.payment = moneyNode.get("amount").asInt();
                    moneyList.add(obj);
                }
            }
        } else if (type.equals("income")) {
            if (0 == moneyList.size()) {
                obj.dateStr = yyyy_mm_dd.substring(5);
                obj.utcDate = Long.valueOf(this.utiDate.convertStartUTC(yyyy_mm_dd)).longValue();
                obj.income = moneyNode.get("amount").asInt();
                moneyList.add(obj);
            } else {
                int index = getSameValueIndex(moneyList, yyyy_mm_dd.substring(5));
                if (0 <= index) {
                    ZaimObject tmp = (ZaimObject) moneyList.get(index);
                    tmp.payment += moneyNode.get("amount").asInt();
                    moneyList.set(index, tmp);
                } else {
                    obj.dateStr = yyyy_mm_dd.substring(5);
                    obj.utcDate = Long.valueOf(this.utiDate.convertStartUTC(yyyy_mm_dd)).longValue();
                    obj.income = moneyNode.get("amount").asInt();
                    moneyList.add(obj);
                }
            }
        }
        return moneyList;
    }

    private String sumPayment(ArrayList<String[]> paymentList) {
        long sum = 0L;
        NumberFormat nfCur = NumberFormat.getCurrencyInstance();
        for (String[] pay : paymentList) {
            sum += Long.valueOf(pay[3]).longValue();
        }
        return String.valueOf(nfCur.format(sum));
    }

    private String sumIncome(ArrayList<String[]> incomeList) {
        long sum = 0L;
        NumberFormat nfCur = NumberFormat.getCurrencyInstance();
        for (String[] pay : incomeList) {
            sum += Long.valueOf(pay[2]).longValue();
        }
        return String.valueOf(nfCur.format(sum));
    }

    private ArrayList<String> formatPaymentList(ArrayList<String[]> paymentList) {
        ArrayList<String> arrayPayment = new ArrayList();
        NumberFormat nfCur = NumberFormat.getCurrencyInstance();
        for (String[] pay : paymentList) {
            arrayPayment.add(pay[1] + "_" + pay[2] + ": " + nfCur.format(Long.valueOf(pay[3])));
        }
        return arrayPayment;
    }

    private ArrayList<String> formatIncomeList(ArrayList<String[]> incomeList) {
        ArrayList<String> arrayIncome = new ArrayList();
        NumberFormat nfCur = NumberFormat.getCurrencyInstance();
        for (String[] income : incomeList) {
            arrayIncome.add(income[0] + "_" + income[1] + ": " + nfCur.format(Long.valueOf(income[2])));
        }
        return arrayIncome;
    }

    public boolean changeCoop(boolean zaimCoopFlg) {
        if (zaimCoopFlg) {
            this.db.releaseCoopZaim(this.session);
            return false;
        }
        this.db.coopZaim(this.session);
        return true;
    }

    public boolean cancelChangeCoop(boolean zaimCoopFlg) {
        if (zaimCoopFlg) {
            return false;
        }
        return true;
    }

    private int getSameValueIndex(ArrayList<ZaimObject> list, String mm_dd) {
        for (int index = 0; index < list.size(); index++) {
            if (mm_dd.equals(((ZaimObject) list.get(index)).dateStr)) {
                return index;
            }
        }
        return -1;
    }

    private void injectZeroDayData(ArrayList<String> dayList, ArrayList<ZaimObject> list) {
        Iterator localIterator;
        String day;
        if (list.isEmpty()) {
            for (localIterator = dayList.iterator(); localIterator.hasNext();) {
                day = (String) localIterator.next();
                ZaimObject obj = new ZaimObject();
                obj.dateStr = day;
                obj.utcDate = Long.valueOf(this.utiDate.convertStartUTC(day)).longValue();
                list.add(obj);
            }
        } else {
            Object dateStrList = new ArrayList();
            for (ZaimObject obj : list) {
                ((ArrayList) dateStrList).add(obj.dateStr);
            }
            for (int i = 0; i < dayList.size(); i++) {
                String date = ((String) dayList.get(i)).substring(5);
                int index = ((ArrayList) dateStrList).indexOf(date);
                if (index < 0) {
                    ZaimObject obj = new ZaimObject();
                    obj.dateStr = ((String) dayList.get(i)).substring(5);
                    obj.utcDate = Long.valueOf(this.utiDate.convertStartUTC((String) dayList.get(i))).longValue();
                    list.add(obj);
                }
            }
        }
    }

    private ArrayList<ZaimObject> summarizeWeekDuration(ArrayList<ZaimObject> paymentList) {
        ArrayList<ZaimObject> sumList = new ArrayList();
        for (int i = 0; i < paymentList.size(); i++) {
            String start = ((ZaimObject) paymentList.get(i)).dateStr;
            long utcDate = ((ZaimObject) paymentList.get(i)).utcDate;
            int sum = 0;
            for (int j = 0; j < 7; j++) {
                if (i >= paymentList.size()) {
                    break;
                }
                sum += ((ZaimObject) paymentList.get(i)).payment;
                i += 1;
            }
            String end = findEndDate(i - 1, paymentList);
            ZaimObject obj = new ZaimObject();
            obj.dateStr = (start + "���" + end);
            obj.utcDate = utcDate;
            obj.payment = sum;
            sumList.add(obj);
        }
        return sumList;
    }

    private String findEndDate(int i, ArrayList<ZaimObject> list) {
        String end;
        if (i < list.size()) {
            end = ((ZaimObject) list.get(i)).dateStr;
        } else {
            end = ((ZaimObject) list.get(list.size() - 1)).dateStr;
        }
        return end;
    }

    private ArrayList<ZaimObject> summarizeMonthDuration(ArrayList<ZaimObject> paymentList) {
        ArrayList<ZaimObject> sumList = new ArrayList();
        for (ZaimObject obj : paymentList) {
            if (!sumList.isEmpty()) {
                boolean sumFlg = false;
                for (int i = 0; i < sumList.size(); i++) {
                    String month = obj.dateStr.substring(0, 2);
                    if (month.equals(((ZaimObject) sumList.get(i)).dateStr)) {
                        int sum = ((ZaimObject) sumList.get(i)).payment + obj.payment;
                        ((ZaimObject) sumList.get(i)).payment = sum;
                        sumFlg = true;
                    }
                }
                if (!sumFlg) {
                    obj.dateStr = obj.dateStr.substring(0, 2);
                    sumList.add(obj);
                }
            } else {
                obj.dateStr = obj.dateStr.substring(0, 2);
                sumList.add(obj);
            }
        }
        return sumList;
    }
}
