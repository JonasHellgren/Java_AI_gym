package java_ai_gym.models_pong;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;

public class HistogramPanel extends JPanel {

    private final int MARGIN = 15;
    private final Color BACKGROUND_COLOR = Color.GRAY;
    CategoryDataset dataset;

    public HistogramPanel() {
        this.dataset = new DefaultCategoryDataset();
        addChart(this.dataset);
    }

    public void addChart(CategoryDataset dataset) {
        JFreeChart ch = createChart(dataset);
        ChartPanel cp = new ChartPanel(ch);
        cp.setBorder(BorderFactory.createEmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN));
        cp.setBackground(BACKGROUND_COLOR);
        add(cp);
    }


    public DefaultCategoryDataset getDataset() {
        return (DefaultCategoryDataset) dataset;
    }

    private JFreeChart createChart(CategoryDataset dataset) {

        JFreeChart barChart = ChartFactory.createBarChart(
                "Bar Chart", //Chart Title
                "Depth", // Category axis
                "Number of", // Value axis
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        return barChart;
    }

}