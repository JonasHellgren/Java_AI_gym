package java_ai_gym.models_pong;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.logging.Logger;

public class SearchTreeHistogramPanelCreator {

    protected final static Logger logger = Logger.getLogger(SearchTreeHistogramPanelCreator.class.getName());

    JLabel label;
    VisitedStatesBuffer vsb;
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    JFreeChart chart;

    public SearchTreeHistogramPanelCreator() {

    }

    /*
    public void createLabel(int panelW, int panelH, String text) {

        this.label = new JLabel();
        this.label.setBounds(panelW / 2, 20, 100, 20);
        this.label.setForeground(Color.CYAN);
        this.label.setVisible(true);
        this.label.setText(text);
        add(this.label);
    }  */

    public ChartPanel createChart() {
        createDataset();
        chart = ChartFactory.createBarChart(
                "Bar Chart Example", //Chart Title
                "Year", // Category axis
                "Population in Million", // Value axis
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );
        ChartPanel panel = new ChartPanel(chart);
      //  panel.setBounds(margin, yPos, panelWeight - margin, panelHeight);
        return panel;
    }

    private void createDataset() {


        // Population in 2005
        dataset.addValue(10, "USA", "2005");
        dataset.addValue(15, "India", "2005");
        dataset.addValue(20, "China", "2005");

        // Population in 2010
        dataset.addValue(15, "USA", "2010");
        dataset.addValue(20, "India", "2010");
        dataset.addValue(25, "China", "2010");

        // Population in 2015
        dataset.addValue(20, "USA", "2015");
        dataset.addValue(25, "India", "2015");
        dataset.addValue(30, "China", "2015");

        dataset.addValue(28, "USA", "2020");
        dataset.addValue(35, "India", "2020");
        dataset.addValue(40, "China", "2020");

    }


}
