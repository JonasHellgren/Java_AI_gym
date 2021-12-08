package java_ai_gym.models_pong;

import java_ai_gym.helpers.MathUtils;
import java_ai_gym.models_common.StateForSearch;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.logging.Logger;

public class ExplorationFactorCalculator {

    protected final static Logger logger = Logger.getLogger(ExplorationFactorCalculator.class.getName());
    final double PROB_SELECTING_STATE_FOR_EXPLORATION_FACTOR_CALCULATION = 0.2;  //for speeding up
    final int MIN_LENGTH_EXP_FACTOR_LIST=100; //only regard PROB_SELECTING_STATE.. above this length
    final double EXPLORATION_FACTOR_IF_NO_STATE_FOUND=0.0;

    VisitedStatesBuffer vsb;

    public ExplorationFactorCalculator(VisitedStatesBuffer vsb) {
        this.vsb = vsb;
    }

    public double calc(int excludedDepth) {

        List<Double> explorationFactorList = new ArrayList<>();
        for (StateForSearch state : vsb.stateVisitsDAO.getAll()) {
            double probability=explorationFactorList.size()>MIN_LENGTH_EXP_FACTOR_LIST
                    ?PROB_SELECTING_STATE_FOR_EXPLORATION_FACTOR_CALCULATION
                    :1.0;
            if (state.depth != excludedDepth &&
                    MathUtils.calcRandomFromIntervall(0, 1) < probability &&
                    !vsb.setOfTerminalStatesDAO.isTerminal(state.id)) {

                if (state.nofActions == 0) {
                    logger.warning("No actions in state = " + state.id);
                } else {
                    explorationFactorList.add((vsb.nofActionsTested(state.id) / (double) state.nofActions));
                }
            }
        }

        if (explorationFactorList.size() == 0) {
            logger.warning("No state fulfills criteria for exploration factor calculation");
            return EXPLORATION_FACTOR_IF_NO_STATE_FOUND;
        }

        DoubleSummaryStatistics stats = explorationFactorList.stream().mapToDouble(a -> a).summaryStatistics();
        return stats.getAverage();
    }

}
