package thirdparty.todoist;

import bean.HeaderBb;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import db.KarmaDb;
import db.TodoistDb;
import entity.TodoistEnti;
import entity.Todoist_karma;
import entity.Token_todoist;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import oauth.Otodoist;
import oauth.SuperOauth;
import thirdparty.json.Karma;
import util.UtilChart;
import util.UtilDate;
import util.UtilLogic;
import view.chart.LineChart;

@RequestScoped
public class Todoist extends SuperOauth {

    private static final String method = "GET";
    HttpServletRequest request = getRequest();
    HttpSession session = this.request.getSession(true);
    @Inject
    TodoistDb db;
    @Inject
    TodoistEnti toEnti;
    @Inject
    HeaderBb headerBb;
    @Inject
    Token_todoist tokenObj;
    @Inject
    Otodoist oto;
    @Inject
    KarmaDb karmaDb;
    @Inject
    UtilLogic utiLogic;
    @Inject
    UtilDate utiDate;
    @Inject
    UtilChart utiChart;
    @Inject
    LineChart lineChart;

    public boolean changeCoop(boolean todoCoopFlg) {
        if (todoCoopFlg) {
            this.db.releaseCoopTodoist(this.session);
            return false;
        }
        this.db.coopTodoist(this.session);
        return true;
    }

    public boolean cancelChangeCoop(boolean todoCoopFlg) {
        if (todoCoopFlg) {
            return false;
        }
        return true;
    }

    public boolean doesCooperate(HttpSession session) {
        boolean coop = true;
        if (isExistAccessToken(session)) {
            this.headerBb.setTodoCoopFlg(true);
            coop = true;
        } else {
            this.headerBb.setTodoCoopFlg(false);
            coop = false;
        }
        return coop;
    }

    private boolean isExistAccessToken(HttpSession session) {
        String userId = session.getAttribute("user_id").toString();
        this.tokenObj = this.db.findObj(userId);
        boolean exist = true;
        if (("".equals(this.tokenObj.getTodo_code())) || ("".equals(this.tokenObj.getTodo_state()))) {
            exist = this.oto.isCallback(session);
        }
        return exist;
    }

    public void setTaskMeasures() {
        String userId = this.session.getAttribute("user_id").toString();
        this.tokenObj = this.db.findObj(userId);
        String token = this.tokenObj.getAccess_token();
        try {
            String encodedParam = "token=" + URLEncode(token) + "&" + "sync_token" + "=" + URLEncode("'*'") + "&" + "resource_types" + "=" + URLEncode("[\"all\"]");

            System.out.println("encodedParam =" + encodedParam);
            URL url = new URL("https://todoist.com/API/v7/sync?" + encodedParam);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String jsonText = reader.readLine();
            System.out.println("Todoist_jsonText=" + jsonText);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = (JsonNode) mapper.readValue(jsonText, JsonNode.class);
            JsonNode itemsList = node.get("items");

            ArrayList<String[]> taskList = new ArrayList();
            for (JsonNode itemNode : itemsList) {
                String[] due_date = itemNode.get("due_date_utc").toString().split("\"");

                boolean complete = doesComplete(due_date[1]);

                String[] item = new String[3];
                item[0] = getCheckedIconPass(complete);
                item[1] = this.utiDate.convertUSformatToYyyy_mm_dd(due_date[1]);
                String[] content = itemNode.get("content").toString().split("\"");
                item[2] = content[1];
                taskList.add(item);
            }
            this.toEnti.setTaskList(taskList);
        } catch (MalformedURLException ex) {
            Logger.getLogger(Otodoist.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ProtocolException ex) {
            Logger.getLogger(Otodoist.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Otodoist.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setRangeMeasures(Date start, Date end, ArrayList<String> dayList, int dayCount) {
        Todoist_karma karma = this.karmaDb.findObj(this.session.getAttribute("user_id").toString());
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = (JsonNode) mapper.readValue(karma.getKarma(), JsonNode.class);
            JsonNode storedKarmaMapNode = node.get("karmaMap");
            Map<String, String> storedKarmaMap = (Map) mapper.convertValue(storedKarmaMapNode, Map.class);

            injectZeroDayData(dayList, storedKarmaMap);
            storedKarmaMap = filterKarmaMap(dayList, storedKarmaMap);
            ArrayList<TodoistObject> karmaList = new ArrayList();
            convertMapToList(storedKarmaMap, karmaList);
            Collections.sort(karmaList, new TodoistComparator());
            if (karmaList.size() > 60) {
                karmaList = summarizeMonthDuration(karmaList);
            } else if (karmaList.size() > 31) {
                karmaList = summarizeWeekKarma(karmaList);
            }
            this.lineChart.setKarmaLineModel(karmaList);
        } catch (IOException ex) {
            Logger.getLogger(Todoist.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean doesComplete(String due_date) {
        String formatted = this.utiDate.convertUSformatToYyyy_mm_dd(due_date);
        String today = this.utiDate.getTodayYyyyMmDd();
        Long utc_due_date = Long.valueOf(this.utiDate.convertEndUTC(formatted));
        Long utc_today = Long.valueOf(this.utiDate.convertEndUTC(today));
        boolean complete;
        if (utc_due_date.longValue() > utc_today.longValue()) {
            complete = true;
        } else {
            complete = false;
        }
        return complete;
    }

    private String getCheckedIconPass(boolean complete) {
        if (complete) {
            return "../img/finish.png";
        }
        return "../img/notYet.png";
    }

    public void syncKarmaData() {
        try {
            String userId = this.session.getAttribute("user_id").toString();
            this.tokenObj = this.db.findObj(userId);
            String token = this.tokenObj.getAccess_token();

            String encodedParam = "token=" + URLEncode(token);

            String jsonText = "";
            try {
                URL url = new URL("https://todoist.com/API/v7/get_productivity_stats?" + encodedParam);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.connect();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                jsonText = reader.readLine();
                System.out.println("Todoist_jsonText=" + jsonText);
            } catch (MalformedURLException ex) {
                Logger.getLogger(Todoist.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Todoist.class.getName()).log(Level.SEVERE, null, ex);
            }
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = (JsonNode) mapper.readValue(jsonText, JsonNode.class);
            JsonNode dateNode = node.get("days_items");
            JsonNode karmaNode = node.get("karma_update_reasons");
            //JsonNode date;
            ArrayList<String> dateList = new ArrayList();
            for (Iterator localIterator = dateNode.iterator(); localIterator.hasNext();) {
                JsonNode date = (JsonNode) localIterator.next();
                String[] dateStr = date.get("date").toString().split("\"");
                dateList.add(dateStr[1]);
            }

            ArrayList<String> karmaList = new ArrayList();
            for (JsonNode karma : karmaNode) {
                String karmaStr = karma.get("new_karma").toString();
                karmaList.add(karmaStr);
            }
            Map<String, String> karmaMap = new HashMap();
            for (int i = 0; i < ((ArrayList) karmaList).size(); i++) {
                karmaMap.put(dateList.get(i), karmaList.get(i));
            }
            String diffKarma = addDiff(userId, karmaMap);
            this.db.updateKarma(userId, diffKarma);
        } catch (IOException ex) {
            Logger.getLogger(Todoist.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String addDiff(String userId, Map<String, String> karmaMap) {
        String updatedJson = "";
        Todoist_karma karma = this.karmaDb.findObj(userId);
        ObjectMapper mapper = new ObjectMapper();
        if ("".equals(karma.getKarma())) {
            System.out.println("karma未登録");
            Karma updatedKarma = new Karma();
            updatedKarma.setKarmaMap(karmaMap);
            try {
                updatedJson = mapper.writeValueAsString(updatedKarma);
            } catch (JsonProcessingException ex1) {
                Logger.getLogger(Todoist.class.getName()).log(Level.SEVERE, null, ex1);
            }
        } else {
            try {
                JsonNode node = (JsonNode) mapper.readValue(karma.getKarma(), JsonNode.class);
                JsonNode storedKarmaMapNode = node.get("karmaMap");
                Map<String, String> storedKarmaMap = (Map) mapper.convertValue(storedKarmaMapNode, Map.class);

                Set<String> keySet = karmaMap.keySet();
                for (String key : keySet) {
                    if (!storedKarmaMap.containsKey(key)) {
                        storedKarmaMap.put(key, karmaMap.get(key));
                    }
                }
                Karma updatedKarma = new Karma();
                updatedKarma.setKarmaMap(storedKarmaMap);
                updatedJson = mapper.writeValueAsString(updatedKarma);
                System.out.println("karmaMapNode確認");
            } catch (IOException ex) {
                Logger.getLogger(Todoist.class.getName()).log(Level.SEVERE, null, ex);

                Karma updatedKarma = new Karma();
                updatedKarma.setKarmaMap(karmaMap);
                try {
                    updatedJson = mapper.writeValueAsString(updatedKarma);
                } catch (JsonProcessingException ex1) {
                    Logger.getLogger(Todoist.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        }
        return updatedJson;
    }

    private void injectZeroDayData(ArrayList<String> dayList, Map<String, String> storedKarmaMap) {
        for (String date : dayList) {
            if (!storedKarmaMap.containsKey(date)) {
                storedKarmaMap.put(date, "0");
            }
        }
    }

    private void convertMapToList(Map<String, String> storedKarmaMap, ArrayList<TodoistObject> karmaList) {
        for (String karmaDate : storedKarmaMap.keySet()) {
            TodoistObject obj = new TodoistObject();
            obj.dateStr = karmaDate.substring(5);
            obj.utcDate = Long.valueOf(this.utiDate.convertStartUTC(karmaDate));
            obj.karma = Double.valueOf((String) storedKarmaMap.get(karmaDate)).longValue();
            karmaList.add(obj);
        }
    }

    private ArrayList<TodoistObject> summarizeWeekKarma(ArrayList<TodoistObject> karmaList) {
        ArrayList<TodoistObject> sumList = new ArrayList();
        for (int i = 0; i < karmaList.size(); i++) {
            String start = ((TodoistObject) karmaList.get(i)).dateStr;
            long utcDate = ((TodoistObject) karmaList.get(i)).utcDate;
            long extKarma = 0L;
            for (int j = 0; j < 7; j++) {
                if (i >= karmaList.size()) {
                    break;
                }
                long karma = ((TodoistObject) karmaList.get(i)).karma;
                if (karma != 0L) {
                    if (extKarma == 0L) {
                        extKarma = karma;
                    } else if (extKarma < karma) {
                        extKarma = karma;
                    }
                }
                i += 1;
            }
            String end = findEndDate(i - 1, karmaList);
            TodoistObject obj = new TodoistObject();
            obj.dateStr = (start + "〜" + end);
            obj.karma = extKarma;
            obj.utcDate = utcDate;
            sumList.add(obj);
        }
        return sumList;
    }

    private ArrayList<TodoistObject> summarizeMonthDuration(ArrayList<TodoistObject> karmaList) {
        ArrayList<TodoistObject> sumList = new ArrayList();
        for (TodoistObject obj : karmaList) {
            if (!sumList.isEmpty()) {
                boolean sumFlg = false;
                for (int i = 0; i < sumList.size(); i++) {
                    String month = obj.dateStr.substring(0, 2);
                    if (month.equals(((TodoistObject) sumList.get(i)).dateStr)) {
                        sumFlg = true;
                        long exKarma = obj.karma;
                        if (exKarma != 0L) {
                            if (((TodoistObject) sumList.get(i)).karma == 0L) {
                                ((TodoistObject) sumList.get(i)).karma = exKarma;
                            } else if (((TodoistObject) sumList.get(i)).karma < exKarma) {
                                ((TodoistObject) sumList.get(i)).karma = exKarma;
                            }
                        }
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

    private String findEndDate(int i, ArrayList<TodoistObject> list) {
        String end;
        if (i < list.size()) {
            end = ((TodoistObject) list.get(i)).dateStr;
        } else {
            end = ((TodoistObject) list.get(list.size() - 1)).dateStr;
        }
        return end;
    }

    private Map<String, String> filterKarmaMap(ArrayList<String> dayList, Map<String, String> storedKarmaMap) {
        Map<String, String> filterdMap = new HashMap();
        for (String date : dayList) {
            if (storedKarmaMap.containsKey(date)) {
                filterdMap.put(date, storedKarmaMap.get(date));
            }
        }
        return filterdMap;
    }
}
