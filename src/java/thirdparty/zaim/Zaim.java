package thirdparty.zaim;

import bean.HeaderBb;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import static constants.Common.*;
import static constants.Const_oauth.*;
import static constants.Const_zaim.*;
import db.ZaimDb;
import entity.Token_zaim;
import entity.ZaimEnti;
import java.io.IOException;
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
public class Zaim extends SuperOauth {

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
        return coop;
    }

    public boolean isExistAccessToken(HttpSession session) {
        String userId = session.getAttribute(USER_ID).toString();
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

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = (JsonNode) mapper.readValue(jsonText, JsonNode.class);
            JsonNode moneyNodeList = node.get(ZA_MONEY);

            ArrayList<String[]> paymentList = new ArrayList();
            ArrayList<String[]> incomeList = new ArrayList();
            for (JsonNode moneyNode : moneyNodeList) {
                String[] mode = moneyNode.get(ZA_MODE).toString().split("\"");
                if (ZA_PAYMENT.equals(mode[1])) {
                    paymentList = addTodayPaymentList(moneyNode, paymentList);
                } else if (ZA_INCOME.equals(mode[1])) {
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

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = (JsonNode) mapper.readValue(jsonText, JsonNode.class);
            JsonNode moneyNodeList = node.get(ZA_MONEY);

            ArrayList<ZaimObject> paymentList = new ArrayList();
            ArrayList<ZaimObject> incomeList = new ArrayList();
            for (JsonNode moneyNode : moneyNodeList) {
                String[] mode = moneyNode.get(ZA_MODE).toString().split("\"");
                if (ZA_PAYMENT.equals(mode[1])) {
                    paymentList = addRangeMoneyList(moneyNode, paymentList, ZA_PAYMENT);
                } else if (ZA_INCOME.equals(mode[1])) {
                    incomeList = addRangeMoneyList(moneyNode, incomeList, ZA_INCOME);
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
        String userId = this.session.getAttribute(USER_ID).toString();
        this.tokenObj = this.db.findObj(userId);

        String access_token = this.tokenObj.getAccess_token();
        String access_token_secret = this.tokenObj.getAccess_token_secret();

        SortedMap<String, String> paramsMap = new TreeMap();
        try {
            paramsMap.put(ZA_START_DATE, from);
            paramsMap.put(ZA_END_DATE, to);
            paramsMap.put(OAUTH_CONSUMER_KEY, CONSUMER_KEY);
            paramsMap.put(OAUTH_NONCE, super.getRandomChar());
            paramsMap.put(OAUTH_SIGNATURE_METHOD, HMAC_SHA1);
            paramsMap.put(OAUTH_TIMESTAMP, String.valueOf(super.getUnixTime()));
            paramsMap.put(OAUTH_TOKEN, access_token);
            paramsMap.put(OAUTH_VERSION, "1.0");
            String sigKey = super.makeSigKey(CONSUMER_SECRET, access_token_secret);
            String sigData = super.makeSigData(CONSUMER_KEY, MONEY_URL, paramsMap, HTTP_GET);

            URL url = new URL(
                    MONEY_URL + "?"
                    + ZA_START_DATE + "=" + URLEncode(paramsMap.get(ZA_START_DATE)) + "&"
                    + ZA_END_DATE + "=" + URLEncode(paramsMap.get(ZA_END_DATE)) + "&"
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

    private ArrayList<String[]> addTodayPaymentList(JsonNode moneyNode, ArrayList<String[]> paymentList) {
        String[] payment = new String[4];
        String uniqueId;
        if ("".equals(moneyNode.get(ZA_RECEIPT_ID).toString())) {
            uniqueId = moneyNode.get(ZA_ID).toString();
        } else {
            uniqueId = moneyNode.get(ZA_RECEIPT_ID).toString();
        }
        if (0 == paymentList.size()) {
            payment[0] = uniqueId;
            payment[1] = moneyNode.get(ZA_CATEGORY_ID).toString();
            payment[2] = moneyNode.get(ZA_GENRE_ID).toString();
            payment[3] = moneyNode.get(ZA_AMOUNT).toString();
            paymentList.add(payment);
        } else {
            int index = this.utiLogic.getSameValueIndex(paymentList, uniqueId);
            if (0 <= index) {
                String[] tmpPayment = (String[]) paymentList.get(index);
                tmpPayment[3] = String.valueOf(Long.valueOf(tmpPayment[3]) + moneyNode.get(ZA_AMOUNT).asLong());
                paymentList.set(index, tmpPayment);
            } else {
                payment[0] = uniqueId;
                payment[1] = moneyNode.get(ZA_CATEGORY_ID).toString();
                payment[2] = moneyNode.get(ZA_GENRE_ID).toString();
                payment[3] = moneyNode.get(ZA_AMOUNT).toString();
                paymentList.add(payment);
            }
        }
        return paymentList;
    }

    private ArrayList<String[]> addTodayIncomeList(JsonNode moneyNode, ArrayList<String[]> incomeList) {
        String[] income = new String[3];

        income[0] = moneyNode.get(ZA_CATEGORY_ID).toString();
        income[1] = moneyNode.get(ZA_GENRE_ID).toString();
        income[2] = moneyNode.get(ZA_AMOUNT).toString();

        incomeList.add(income);

        return incomeList;
    }

    private ArrayList<ZaimObject> addRangeMoneyList(JsonNode moneyNode, ArrayList<ZaimObject> moneyList, String type) {
        ZaimObject obj = new ZaimObject();

        String[] dateStr = moneyNode.get(ZA_DATE).toString().split("\"");
        String yyyy_mm_dd = dateStr[1];
        if (type.equals(ZA_PAYMENT)) {
            if (0 == moneyList.size()) {
                obj.dateStr = yyyy_mm_dd.substring(5);
                obj.utcDate = Long.valueOf(this.utiDate.convertStartUTC(yyyy_mm_dd));
                obj.payment = moneyNode.get(ZA_AMOUNT).asInt();
                moneyList.add(obj);
            } else {
                int index = getSameValueIndex(moneyList, yyyy_mm_dd.substring(5));
                if (0 <= index) {
                    ZaimObject tmp = (ZaimObject) moneyList.get(index);
                    tmp.payment += moneyNode.get(ZA_AMOUNT).asInt();
                    moneyList.set(index, tmp);
                } else {
                    obj.dateStr = yyyy_mm_dd.substring(5);
                    obj.utcDate = Long.valueOf(this.utiDate.convertStartUTC(yyyy_mm_dd));
                    obj.payment = moneyNode.get(ZA_AMOUNT).asInt();
                    moneyList.add(obj);
                }
            }
        } else if (type.equals(ZA_INCOME)) {
            if (0 == moneyList.size()) {
                obj.dateStr = yyyy_mm_dd.substring(5);
                obj.utcDate = Long.valueOf(this.utiDate.convertStartUTC(yyyy_mm_dd));
                obj.income = moneyNode.get(ZA_AMOUNT).asInt();
                moneyList.add(obj);
            } else {
                int index = getSameValueIndex(moneyList, yyyy_mm_dd.substring(5));
                if (0 <= index) {
                    ZaimObject tmp = (ZaimObject) moneyList.get(index);
                    tmp.payment += moneyNode.get(ZA_AMOUNT).asInt();
                    moneyList.set(index, tmp);
                } else {
                    obj.dateStr = yyyy_mm_dd.substring(5);
                    obj.utcDate = Long.valueOf(this.utiDate.convertStartUTC(yyyy_mm_dd));
                    obj.income = moneyNode.get(ZA_AMOUNT).asInt();
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
            sum += Long.valueOf(pay[3]);
        }
        return String.valueOf(nfCur.format(sum));
    }

    private String sumIncome(ArrayList<String[]> incomeList) {
        long sum = 0L;
        NumberFormat nfCur = NumberFormat.getCurrencyInstance();
        for (String[] pay : incomeList) {
            sum += Long.valueOf(pay[2]);
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
                obj.utcDate = Long.valueOf(this.utiDate.convertStartUTC(day));
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
                    obj.utcDate = Long.valueOf(this.utiDate.convertStartUTC((String) dayList.get(i)));
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
            obj.dateStr = (start + "〜" + end);
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
