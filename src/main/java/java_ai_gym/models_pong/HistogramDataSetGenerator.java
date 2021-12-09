package java_ai_gym.models_pong;

import java_ai_gym.models_common.StateForSearch;
import lombok.ToString;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import java.util.List;
import java.util.logging.Logger;

public class HistogramDataSetGenerator {

    protected final static Logger logger = Logger.getLogger(HistogramDataSetGenerator.class.getName());

    @ToString
    public class DepthStatistics {
        int nofFailStates=0;
        int nofNoActionTestedStates=0;
        int nofReachedDepth=0;
        int nofSomeActionTestedStates=0;

        public DepthStatistics() {
        }
    }

    VisitedStatesBuffer vsb;

    JFreeChart chart;
    DepthStatistics depthStatistics;

    public HistogramDataSetGenerator() {
    }


    public void defineDummyDataset(DefaultCategoryDataset dataset) {  //TODO remove
        // Population in 2005
        //DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.clear();
        dataset.addValue(10, "USA", "2005");
        dataset.addValue(15, "India", "2005");
        dataset.addValue(20, "China", "2005");
        dataset.addValue(3, "Africa", "2005");

        // Population in 2010
        dataset.addValue(15, "USA", "2010");
        dataset.addValue(20, "India", "2010");
        dataset.addValue(25, "China", "2010");
        dataset.addValue(3, "Africa", "2010");
    }


    public void updateDatasetForDepthStatistics(DefaultCategoryDataset dataset,VisitedStatesBuffer vsb,  List<Integer> evaluatedSearchDepths) {
        dataset.clear();
        logger.fine("updateDatasetForDepthStatistics, evaluatedSearchDepths ="+evaluatedSearchDepths);
        this.vsb=vsb;
        //DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        int prevDepth=0;
        for (int searchDepth:evaluatedSearchDepths) {
            String depthSet=prevDepth+"-"+searchDepth;
            depthStatistics=createDepthStatistics(vsb, prevDepth, searchDepth);
            logger.fine("searchDepth ="+searchDepth+", depthStatistics = "+depthStatistics);
            dataset.addValue(depthStatistics.nofFailStates, "fail", depthSet);
            dataset.addValue(depthStatistics.nofNoActionTestedStates, "no action tested", depthSet);
            dataset.addValue(depthStatistics.nofReachedDepth, "reached depth", depthSet);
            dataset.addValue(depthStatistics.nofSomeActionTestedStates, "action tested", depthSet);

            prevDepth=searchDepth;
        }
    }

    private DepthStatistics createDepthStatistics(VisitedStatesBuffer vsb, int prevDepth, int searchDepth) {
        DepthStatistics depthStatistics=new DepthStatistics();
        for(int depth = prevDepth; depth<= searchDepth; depth++) {
            List<StateForSearch> states= vsb.getAllStatesAtDepth(depth);
            updateDepthStatistics(depthStatistics,states, searchDepth);
        }
        return depthStatistics;
    }

    private void updateDepthStatistics(DepthStatistics depthStatistics, List<StateForSearch> states, int searchDepth) {

        if (vsb==null) {
            logger.warning("VisitedStatesBuffer not defined");
        }
        else {
            for (StateForSearch state : states) {
                if (vsb.setOfTerminalStatesDAO.isTerminal(state.id))    {
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

    public void updateDatasetForStatesPerDepth(DefaultCategoryDataset dataset, VisitedStatesBuffer vsb) {
        dataset.clear();

        if (vsb == null) {
            logger.warning("VisitedStatesBuffer not defined");
        } else {

            for (int depth = 0; depth <= vsb.getDepthMax(); depth++) {
                List<StateForSearch> states = vsb.getAllStatesAtDepth(depth);
                //dataset.addValue((int) states.size(), "sgsdg", depth);
                dataset.addValue(states.size(), "Nof states", String.valueOf(depth));
                //System.out.println("depth = " + depth + ", states.size() = " + states.size());

            }
            //  dataset.addValue(3, "sgsdg", "33");

        }
    }


}
