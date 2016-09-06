package view.chart;

import java.io.Serializable;
import java.util.ArrayList;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.BarChartSeries;
import thirdparty.toggl.Toggl;
import thirdparty.toggl.TogglObject;
import thirdparty.withings.WithingsObject;
import thirdparty.zaim.ZaimObject;
import util.UtilChart;

@Named
@SessionScoped
public class BarChart implements Serializable {

    private BarChartModel wiModel;
    private BarChartModel togglModel;
    private BarChartModel zaimModel;
    @Inject
    UtilChart utiChart;
    @Inject
    Toggl toggl;

    public BarChart() {
        this.wiModel = new BarChartModel();
        this.togglModel = new BarChartModel();
        this.zaimModel = new BarChartModel();
    }

    public void setBarModelWithings(ArrayList<WithingsObject> stepList, ArrayList<String> dayList) {
        this.wiModel = new BarChartModel();
        BarChartSeries steps = new BarChartSeries();
        steps.setLabel("歩数");
        for (WithingsObject step : stepList) {
            steps.set(step.getDateStr(), step.getSteps());
        }
        this.wiModel.addSeries(steps);
        this.wiModel.setTitle("歩数の遷移");
        this.wiModel.setLegendPosition("ne");
        this.wiModel.setShowPointLabels(true);

        Axis xAxis = this.wiModel.getAxis(AxisType.X);
        xAxis.setTickAngle(-70);

        Axis yAxis = this.wiModel.getAxis(AxisType.Y);
        yAxis.setMin(0);
        yAxis.setMax(this.utiChart.findMaxValForStep(stepList, "1.2"));
    }

    public void setSumBarModelWithings(ArrayList<WithingsObject> stepList) {
        this.wiModel = new BarChartModel();
        BarChartSeries steps = new BarChartSeries();
        steps.setLabel("歩数");
        for (WithingsObject step : stepList) {
            steps.set(step.getDateStr(), step.getSteps());
        }
        this.wiModel.addSeries(steps);
        this.wiModel.setTitle("歩数の遷移");
        this.wiModel.setLegendPosition("ne");
        this.wiModel.setShowPointLabels(true);

        Axis xAxis = this.wiModel.getAxis(AxisType.X);
        xAxis.setTickAngle(-70);

        Axis yAxis = this.wiModel.getAxis(AxisType.Y);
        yAxis.setMin(0);
        yAxis.setMax(this.utiChart.findMaxValForStep(stepList, "1.2"));
    }

    public void setBarModelToggl(ArrayList<TogglObject> dayDurationsList) {
        this.togglModel = new BarChartModel();
        BarChartSeries series = new BarChartSeries();
        series.setLabel("自己投資時間");
        for (TogglObject obj : dayDurationsList) {
            series.set(obj.getDateStr(), obj.getDuration());
        }
        this.togglModel.addSeries(series);
        this.togglModel.setTitle("自己投資時間の遷移");
        this.togglModel.setLegendPosition("ne");
        this.togglModel.setShowPointLabels(true);
        this.togglModel.setSeriesColors("EA3F30");
        this.togglModel.setExtender("durationsFormatter");

        Axis xAxis = this.togglModel.getAxis(AxisType.X);
        xAxis.setTickAngle(-70);

        Axis yAxis = this.togglModel.getAxis(AxisType.Y);
        yAxis.setMin(0);
        yAxis.setMax(this.utiChart.findMaxValForDuration(dayDurationsList, "1.1"));
    }

    public void setBarModelZaim(ArrayList<ZaimObject> paymentList, ArrayList<ZaimObject> incomeList, ArrayList<String> dayList) {
        this.zaimModel = new BarChartModel();
        BarChartSeries pay = new BarChartSeries();
        pay.setLabel("支出");
        for (ZaimObject payment : paymentList) {
            pay.set(payment.getDateStr(), payment.getPayment());
        }
        this.zaimModel.addSeries(pay);
        this.zaimModel.setTitle("支出の遷移");
        this.zaimModel.setLegendPosition("ne");
        this.zaimModel.setShowPointLabels(true);
        this.zaimModel.setSeriesColors("4EA134");

        Axis xAxis = this.zaimModel.getAxis(AxisType.X);
        xAxis.setTickAngle(-70);

        Axis yAxis = this.zaimModel.getAxis(AxisType.Y);
        yAxis.setMin(0);
        yAxis.setMax(this.utiChart.findMaxValForZaim(paymentList, "1.1"));
    }

    public BarChartModel getWiModel() {
        return this.wiModel;
    }

    public BarChartModel getTogglModel() {
        return this.togglModel;
    }

    public BarChartModel getZaimModel() {
        return this.zaimModel;
    }
}
