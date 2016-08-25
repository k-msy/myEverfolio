package util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import thirdparty.todoist.TodoistObject;
import thirdparty.toggl.TogglObject;
import thirdparty.withings.WithingsObject;
import thirdparty.zaim.ZaimObject;

@Named
@RequestScoped
public class UtilChart {

    public Object findMaxVal(ArrayList<String[]> valueList, String constVal) {
        ArrayList<BigDecimal> list = new ArrayList();
        for (String[] val : valueList) {
            list.add(new BigDecimal(val[1]));
        }
        Collections.sort(list);
        Collections.reverse(list);

        BigDecimal timesConst = new BigDecimal(constVal);
        if (!valueList.isEmpty()) {
            BigDecimal max = (BigDecimal) list.get(0);
            max = max.multiply(timesConst);
            max = max.setScale(0, RoundingMode.CEILING);
            return roundVal(max.intValue());
        }
        BigDecimal max = new BigDecimal("0.0");
        return max.intValue();
    }

    public Object findMinVal(ArrayList<String[]> valueList) {
        ArrayList<BigDecimal> list = new ArrayList();
        for (String[] val : valueList) {
            list.add(new BigDecimal(val[1]));
        }
        Collections.sort(list);

        BigDecimal timesConst = new BigDecimal("0.9");
        if (!valueList.isEmpty()) {
            BigDecimal min = (BigDecimal) list.get(0);
            min = min.multiply(timesConst);
            min = min.setScale(0, RoundingMode.CEILING);
            return roundVal(min.intValue());
        }
        BigDecimal min = new BigDecimal("0.0");
        return min.intValue();
    }

    public Object findMaxValForStep(ArrayList<WithingsObject> stepList, String constVal) {
        ArrayList<BigDecimal> list = new ArrayList();
        for (WithingsObject step : stepList) {
            list.add(new BigDecimal(step.getSteps()));
        }
        Collections.sort(list);
        Collections.reverse(list);

        BigDecimal timesConst = new BigDecimal(constVal);

        BigDecimal max = (BigDecimal) list.get(0);
        max = max.multiply(timesConst);
        max = max.setScale(0, RoundingMode.CEILING);
        return roundVal(max.intValue());
    }

    public Object findMinValForWeight(ArrayList<WithingsObject> weightList, String constVal) {
        ArrayList<BigDecimal> list = new ArrayList();
        for (WithingsObject weight : weightList) {
            list.add(new BigDecimal(weight.getWeight()));
        }
        Collections.sort(list);
        BigDecimal min = new BigDecimal("0.0");
        BigDecimal timesConst = new BigDecimal(constVal);
        for (BigDecimal weight : list) {
            if (weight.compareTo(BigDecimal.ZERO) != 0) {
                min = weight;
                break;
            }
        }
        min = min.multiply(timesConst);
        min = min.setScale(0, RoundingMode.CEILING);
        return roundVal(min.intValue());
    }

    public Object findMaxValForWeight(ArrayList<WithingsObject> weightList, String constVal) {
        ArrayList<BigDecimal> list = new ArrayList();
        for (WithingsObject weight : weightList) {
            list.add(new BigDecimal(weight.getWeight()));
        }
        Collections.sort(list);
        Collections.reverse(list);

        BigDecimal timesConst = new BigDecimal(constVal);

        BigDecimal max = (BigDecimal) list.get(0);
        max = max.multiply(timesConst);
        max = max.setScale(0, RoundingMode.CEILING);
        return roundVal(max.intValue());
    }

    public int roundVal(int intVal) {
        int digit = String.valueOf(intVal).length();
        int tmp = intVal;
        for (int i = 0; i < digit - 2; i++) {
            tmp /= 10;
        }
        tmp = Math.round(tmp);
        for (int i = 0; i < digit - 2; i++) {
            tmp *= 10;
        }
        return tmp;
    }

    public Object findMaxValForDuration(ArrayList<TogglObject> dayDurationsList, String constVal) {
        ArrayList<BigDecimal> list = new ArrayList();
        for (TogglObject obj : dayDurationsList) {
            list.add(new BigDecimal(obj.getDuration()));
        }
        Collections.sort(list);
        Collections.reverse(list);

        BigDecimal timesConst = new BigDecimal(constVal);

        BigDecimal max = (BigDecimal) list.get(0);
        max = max.multiply(timesConst);
        max = max.setScale(0, RoundingMode.CEILING);
        return roundVal(max.intValue());
    }

    public Object findMaxValForZaim(ArrayList<ZaimObject> paymentList, String constVal) {
        ArrayList<BigDecimal> list = new ArrayList();
        for (ZaimObject val : paymentList) {
            list.add(new BigDecimal(val.getPayment()));
        }
        Collections.sort(list);
        Collections.reverse(list);

        BigDecimal timesConst = new BigDecimal(constVal);

        BigDecimal max = (BigDecimal) list.get(0);
        max = max.multiply(timesConst);
        max = max.setScale(0, RoundingMode.CEILING);
        return roundVal(max.intValue());
    }

    public Object findMinValForKarma(ArrayList<TodoistObject> karmaList, String constVal) {
        ArrayList<BigDecimal> list = new ArrayList();
        for (TodoistObject obj : karmaList) {
            list.add(new BigDecimal(obj.getKarma()));
        }
        Collections.sort(list);
        Collections.reverse(list);

        BigDecimal timesConst = new BigDecimal(constVal);

        BigDecimal max = (BigDecimal) list.get(0);
        max = max.multiply(timesConst);
        max = max.setScale(0, RoundingMode.CEILING);
        return roundVal(max.intValue());
    }

    public Object findMaxValForMarma(ArrayList<TodoistObject> karmaList, String constVal) {
        ArrayList<BigDecimal> list = new ArrayList();
        for (TodoistObject obj : karmaList) {
            list.add(new BigDecimal(obj.getKarma()));
        }
        Collections.sort(list);
        Collections.reverse(list);

        BigDecimal timesConst = new BigDecimal(constVal);

        BigDecimal max = (BigDecimal) list.get(0);
        max = max.multiply(timesConst);
        max = max.setScale(0, RoundingMode.CEILING);
        return roundVal(max.intValue());
    }
}
