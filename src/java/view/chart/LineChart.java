package view.chart;

import java.io.Serializable;
import java.util.ArrayList;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.CategoryAxis;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;
import thirdparty.todoist.TodoistObject;
import thirdparty.withings.WithingsObject;
import util.UtilChart;

@Named
@SessionScoped
public class LineChart implements Serializable {

    private LineChartModel wi_model;
    private LineChartModel todo_model;
    @Inject
    UtilChart utiChart;

    public void setWeightLineModel(ArrayList<WithingsObject> weightList, ArrayList<String> dayList) {
        this.wi_model = new LineChartModel();
        LineChartSeries weights = new LineChartSeries();
        weights.setLabel("体重");
        for (WithingsObject wiObj : weightList) {
            weights.set(wiObj.getDateStr(), wiObj.getWeight());
        }
        this.wi_model.addSeries(weights);
        this.wi_model.setTitle("体重の遷移");
        this.wi_model.setLegendPosition("ne");
        this.wi_model.setShowPointLabels(true);
        this.wi_model.getAxes().put(AxisType.X, new CategoryAxis(""));
        this.wi_model.setSeriesColors("0DB5FF");

        Axis xAxis = this.wi_model.getAxis(AxisType.X);
        xAxis.setTickAngle(-70);

        Axis yAxis = this.wi_model.getAxis(AxisType.Y);
        yAxis.setTickFormat("%.1f");
        yAxis.setMin(this.utiChart.findMinValForWeight(weightList, "0.9"));
        yAxis.setMax(this.utiChart.findMaxValForWeight(weightList, "1.1"));
    }

    public void setSumWeightLineModel(ArrayList<WithingsObject> weightList) {
        this.wi_model = new LineChartModel();
        LineChartSeries weights = new LineChartSeries();
        weights.setLabel("体重");
        for (WithingsObject wiObj : weightList) {
            weights.set(wiObj.getDateStr(), wiObj.getWeight());
        }
        this.wi_model.addSeries(weights);
        this.wi_model.setTitle("体重の遷移");
        this.wi_model.setLegendPosition("ne");
        this.wi_model.setShowPointLabels(true);
        this.wi_model.getAxes().put(AxisType.X, new CategoryAxis(""));
        this.wi_model.setSeriesColors("0DB5FF");

        Axis xAxis = this.wi_model.getAxis(AxisType.X);
        xAxis.setTickAngle(-70);

        Axis yAxis = this.wi_model.getAxis(AxisType.Y);
        yAxis.setTickFormat("%.1f");
        yAxis.setMin(this.utiChart.findMinValForWeight(weightList, "0.9"));
        yAxis.setMax(this.utiChart.findMaxValForWeight(weightList, "1.1"));
    }

    public LineChartModel getWi_model() {
        return this.wi_model;
    }

    public void setKarmaLineModel(ArrayList<TodoistObject> karmaList) {
        this.todo_model = new LineChartModel();
        LineChartSeries karmas = new LineChartSeries();
        karmas.setLabel("カルマ");
        for (TodoistObject obj : karmaList) {
            karmas.set(obj.getDateStr(), obj.getKarma());
        }
        this.todo_model.addSeries(karmas);
        this.todo_model.setTitle("カルマの遷移");
        this.todo_model.setLegendPosition("ne");
        this.todo_model.setShowPointLabels(true);
        this.todo_model.getAxes().put(AxisType.X, new CategoryAxis(""));
        this.todo_model.setSeriesColors("0DB5FF");

        Axis xAxis = this.todo_model.getAxis(AxisType.X);
        xAxis.setTickAngle(-70);

        Axis yAxis = this.todo_model.getAxis(AxisType.Y);
        yAxis.setMin(this.utiChart.findMinValForKarma(karmaList, "0.75"));
        yAxis.setMax(this.utiChart.findMaxValForMarma(karmaList, "1.1"));
    }

    public LineChartModel getTodo_model() {
        return this.todo_model;
    }
}
