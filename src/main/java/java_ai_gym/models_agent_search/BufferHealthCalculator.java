package java_ai_gym.models_agent_search;

import java_ai_gym.helpers.MathUtils;
import java_ai_gym.models_common.StateForSearch;
import lombok.Getter;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.logging.Logger;

@Getter
public class BufferHealthCalculator {

    protected final static Logger logger = Logger.getLogger(BufferHealthCalculator.class.getName());
    final double PROB_SELECTING_STATE_FOR_EXPLORATION_FACTOR_CALCULATION = 0.2;  //for speeding up
    final int MIN_LENGTH_EXP_FACTOR_LIST=100; //only regard PROB_SELECTING_STATE.. above this length
    final double EXPLORATION_FACTOR_IF_NO_STATE_FOUND=0.0;

    VisitedStatesBuffer vsb;
    int nofStatesBeforePreviousDpCalc;

    public BufferHealthCalculator(VisitedStatesBuffer vsb) {
        this.vsb = vsb;
    }

    public void setNofStatesBeforePreviousDpCalc () {
        nofStatesBeforePreviousDpCalc=vsb.size();
    }
    public double calcExplorationFactor(int excludedDepth) {

        List<Double> explorationFactorList = new ArrayList<>();
        for (StateForSearch state : vsb.getStateVisitsDAO().getAll()) {
            double probability=explorationFactorList.size()>MIN_LENGTH_EXP_FACTOR_LIST
                    ?PROB_SELECTING_STATE_FOR_EXPLORATION_FACTOR_CALCULATION
                    :1.0;
            if (state.depth != excludedDepth &&
                    MathUtils.calcRandomFromIntervall(0, 1) < probability &&
                    !vsb.getSetOfTerminalStatesDAO().isTerminal(state.id)) {

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

     //   if (excludedDepth>=20) {
     //       System.out.println("explorationFactorList = "+explorationFactorList);
    //    }

        DoubleSummaryStatistics stats = explorationFactorList.stream().mapToDouble(a -> a).summaryStatistics();
        return stats.getAverage();
    }


    public double calcFractionLooseNodes(int excludedDepth) {

        return (vsb.size()==0)
                ?0
                :nofNonTerminalStatesWithZeroTriedActions(excludedDepth)/(double) vsb.size();
    }

    public boolean isVsbBigEnough(double VSB_SIZE_INCREASE_FACTOR_MIN) {

      return   vsb.size()/(double) (nofStatesBeforePreviousDpCalc +1)>VSB_SIZE_INCREASE_FACTOR_MIN;

    }

    private int nofNonTerminalStatesWithZeroTriedActions(int searchDepth) {
        int nofStates=0;
        for (String id:vsb.getAllIds()) {
            if (vsb.isNoActionTriedInStateWithId(id) && !vsb.isExperienceOfStateTerminal(id) && vsb.getState(id).depth!=searchDepth) {
                nofStates++;
            }
        }
        return nofStates;
    }

}
