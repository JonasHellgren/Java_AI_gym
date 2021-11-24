package java_ai_gym.models_pong;

import java_ai_gym.models_common.StateForSearch;
import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.util.List;
import java.util.logging.Logger;

public class SearchTreeHistogramFromStatesPerDepthPanelCreator {

    protected final static Logger logger = Logger.getLogger(SearchTreeHistogramFromStatesPerDepthPanelCreator.class.getName());

    VisitedStatesBuffer vsb;
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    JFreeChart chart;

    public SearchTreeHistogramFromStatesPerDepthPanelCreator() {
    }

    public ChartPanel createHistogramFromStatesPerDepthPanelCreator(VisitedStatesBuffer vsb) {
        this.vsb=vsb;

        return createChartPanel();
    }

    @NotNull
    ChartPanel createChartPanel() {
        createDataset();
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

    private void createDataset() {
        dataset.clear();

        if (vsb == null) {
            logger.warning("VisitedStatesBuffer not defined");
        } else {

            for (int depth = 0; depth <= vsb.getMaxDepth(); depth++) {
                List<StateForSearch> states = vsb.getAllStatesAtDepth(depth);
                //dataset.addValue((int) states.size(), "sgsdg", depth);
                dataset.addValue(states.size(), "Nof states", String.valueOf(depth));
                //System.out.println("depth = " + depth + ", states.size() = " + states.size());

            }
          //  dataset.addValue(3, "sgsdg", "33");

        }
    }
    }


