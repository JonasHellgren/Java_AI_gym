package java_ai_gym.models_pong;

import java_ai_gym.models_common.StateForSearch;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.util.List;
import java.util.logging.Logger;

public class SearchTreeHistogramFromDepthStatisticsPanelCreator {

    protected final static Logger logger = Logger.getLogger(SearchTreeHistogramFromDepthStatisticsPanelCreator.class.getName());

    @ToString
    public class DepthStatistics {
        int nofFailStates=0;
        int nofNoActionTestedStates=0;
        int nofReachedDepth=0;
        int nofSomeActionTestedStates=0;

        public DepthStatistics() {
        }
    }

    JLabel label;
    VisitedStatesBuffer vsb;
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    JFreeChart chart;
    DepthStatistics depthStatistics;

    public SearchTreeHistogramFromDepthStatisticsPanelCreator() {

    }

    public ChartPanel createChart() {
        createDummyDataset();
        return createChartPanel();
    }

    @NotNull
    private ChartPanel createChartPanel() {
        chart = ChartFactory.createBarChart(
                "Bar Chart", //Chart Title
                "Depth", // Category axis
                "Number of", // Value axis
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );
        ChartPanel panel = new ChartPanel(chart);
        return panel;
    }

    private void createDummyDataset() {  //TODO remove
        // Population in 2005
        dataset.addValue(10, "USA", "2005");
        dataset.addValue(15, "India", "2005");
        dataset.addValue(20, "China", "2005");
        dataset.addValue(3, "Africa", "2005");

        // Population in 2010
        dataset.addValue(15, "USA", "2010");
        dataset.addValue(20, "India", "2010");
        dataset.addValue(25, "China", "2010");
        dataset.addValue(3, "Africa", "2010");

        // Population in 2015
        dataset.addValue(20, "USA", "2015");
        dataset.addValue(25, "India", "2015");
        dataset.addValue(30, "China", "2015");
        dataset.addValue(6, "Africa", "2015");

    }

    public  ChartPanel createHistogramFromVisitedStatesBufferDepthStatistics(VisitedStatesBuffer vsb, List<Integer> evaluatedSearchDepths) {
        this.vsb=vsb;
        createDatasetForDepthStatistics(evaluatedSearchDepths);
        return createChartPanel();
    }

    private void createDatasetForDepthStatistics(List<Integer> evaluatedSearchDepths) {
        dataset.clear();
        int prevDepth=0;
        for (int searchDepth:evaluatedSearchDepths) {
           String depthSet=prevDepth+"-"+searchDepth;
           depthStatistics=new DepthStatistics();
           for(int depth=prevDepth;depth<=searchDepth;depth++) {
               List<StateForSearch>  states=vsb.getAllStatesAtDepth(depth);
               updateDepthStatistics(states,searchDepth);
           }
            dataset.addValue(depthStatistics.nofFailStates, "fail", depthSet);
            dataset.addValue(depthStatistics.nofNoActionTestedStates, "no action tested", depthSet);
            dataset.addValue(depthStatistics.nofReachedDepth, "reached depth", depthSet);
            dataset.addValue(depthStatistics.nofSomeActionTestedStates, "action tested", depthSet);
        }
    }

    public void updateDepthStatistics(List<StateForSearch> states, int searchDepth) {

        if (vsb==null) {
            logger.warning("VisitedStatesBuffer not defined");
        }
        else {

            for (StateForSearch state : states) {
                StateExperience exp = vsb.searchExperienceOfSteppingToState(state.id);
                if (exp.termState) {
                    depthStatistics.nofFailStates++;
                } else if (vsb.nofActionsTested(state.id) == 0 && (state.depth != searchDepth)) {
                    depthStatistics.nofNoActionTestedStates++;
                } else if (state.depth == searchDepth) {
                    depthStatistics.nofReachedDepth++;
                } else if (vsb.nofActionsTested(state.id) >= 0) {
                    depthStatistics.nofSomeActionTestedStates++;
                } else {
                    logger.warning("Other type of state: " + state);
                }

            }
        }
    }



}
