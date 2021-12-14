package java_ai_gym.models_agent_search;

import java_ai_gym.models_common.StateForSearch;
import lombok.Getter;

import java.util.logging.Logger;

/**  Class included in AgentDPSearch, keeps tracks of some variable and performs simpler functionality.
 * For example increasing the search depth.
**/

@Getter
public class DPSearchServant {

    protected final static Logger logger = Logger.getLogger(DPSearchServant.class.getName());

    AgentDPSearch agent;
    int vsbForNewDepthSetSizePrev;
    double explorationFactorLimitStart;
    double discountFactorExpFactor;
    double explorationFactorLimit;
    double discountFactorReward;

    public DPSearchServant(AgentDPSearch agentDPSearch,
                           double explorationFactorLimitStart,
                           double discountFactorExpFactor,
                           double discountFactorReward) {
        this.agent=agentDPSearch;
        this.vsbForNewDepthSetSizePrev=1;
        this.explorationFactorLimitStart=explorationFactorLimitStart;
        this.explorationFactorLimit=explorationFactorLimitStart;
        this.discountFactorExpFactor=discountFactorExpFactor;
        this.discountFactorReward=discountFactorReward;

    }

    public void resetAgent() {
        agent.vsbForNewDepthSet.clear();
        agent.evaluatedSearchDepths.clear();
        agent.optimalStateSequence.clear();
        agent.getTimeBudgetChecker().reset();
        agent.timeAccumulatorSelectState.reset();
        agent.timeAccumulatorStep.reset();
        agent.timeAccumulatorBellman.reset();
        agent.timeAccumulatorExpFactor.reset();
        vsbForNewDepthSetSizePrev= 0;
        this.vsbForNewDepthSetSizePrev=1;
    }

    public void doResets() {
        if (agent.vsb.getDepthMax() > agent.searchDepth) {
            logger.warning("vsb.getMaxDepth() > searchDept");
        }
        logger.info("increaseSearchDepthDoResets" + ", getDepthMax =" + agent.vsb.getDepthMax() + ", searchDepth =" + agent.searchDepth);

        agent.vsbForNewDepthSet.clear();
        agent.vsbSizeForNewDepthSetAtPreviousExplorationFactorCalculation = 1;
        agent.dpSearchStateSelector.setWasSelectStateFailing(false);
        logger.fine("searchDept increased to = " + agent.searchDepth + ". VSB size = " + agent.vsb.size());
    }

    protected void increaseSearchDepth() {
        agent.searchDepthPrev = agent.searchDepth;
        agent.searchDepth = agent.searchDepth + agent.searchDepthStep;
    }

    protected void updateExplorationFactorLimit() {
        this.explorationFactorLimit = Math.max(agent.EXP_FACTOR_LIMIT_MIN,
                this.explorationFactorLimitStart * Math.pow(this.discountFactorExpFactor, agent.searchDepthPrev));
    }

    public void addEvaluatedSearchDepth(int searchDepth) {
        agent.evaluatedSearchDepths.add(searchDepth);
    }


    public void initInstanceVariables(StateForSearch startState) {
        agent.startState = new StateForSearch(startState);
        int nofActions = agent.getActionSet(startState).size();
        agent.startState.setIdDepthNofActions(agent.startState.START_STATE_ID, 0, nofActions);
        agent.vsb = new VisitedStatesBuffer(agent.startState);
        agent.vsbForNewDepthSet = new VisitedStatesBuffer();
        agent.vsbSizeForNewDepthSetAtPreviousExplorationFactorCalculation = 1;
        agent.searchDepth = agent.searchDepthStep;
        agent.searchDepthPrev = 0;
        agent.vsbForNewDepthSet.clear();
      //  agent.explorationFactorLimit = this.explorationFactorLimitStart;
        agent.bellmanCalculator = new BellmanCalculator(
                agent.vsb,
                new FindMax(),
                this.discountFactorReward,
                agent.getTimeBudgetChecker());
    }




    protected void printResultInfo() {
        logger.info("search finished, vsb size = " + agent.vsb.size());
        System.out.println("statesAtDepth vsb = " + agent.vsb.calcStatesAtDepth(agent.searchDepth));
        System.out.println("statesAtDepth vsbForSpecificDepthStep= " + agent.vsbForNewDepthSet.calcStatesAtDepth(agent.searchDepth));
        System.out.println("searchDepth = " + agent.searchDepth + ", searchDepthPrev = " + agent.searchDepthPrev+ ", explorationFactorLimit = " + this.explorationFactorLimit);
        System.out.println("evaluatedSearchDepths = " + agent.evaluatedSearchDepths);
        System.out.println("maxDepth  = " + agent.vsb.getDepthMax());
        System.out.println("isAnyStateAtSearchDepth() = " + agent.isAnyStateAtSearchDepth() + ", areManyActionsTested() = " + agent.areManyActionsTestedAndFewLooseNodesAndVsbBigEnough() + ", wasSelectStateFailing = " + agent.dpSearchStateSelector.wasSelectStateFailing());
        if (agent.wasSearchFailing()) {
            logger.warning("Failed search, despite many steps there is no state at search depth, i.e end of search horizon");
        }

        System.out.println("time total = " + agent.getTimeBudgetChecker().getTimeSinceStartInMillis() +
                ", timeStep accum = " + agent.getTimeAccumulatorStep().getAccumulatedTimeMillis() +
                ", timeBellman accum = " + agent.getTimeAccumulatorBellman().getAccumulatedTimeMillis() +
                ", timeExpFactor accum = " + agent.getTimeAccumulatorExpFactor().getAccumulatedTimeMillis() +
                ", timeSelect accum = " + agent.getTimeAccumulatorSelectState().getAccumulatedTimeMillis());

    }

    protected void logProgress1() {



        logger.info("searchDepth =" + agent.searchDepth +
                ", explorationFactor =" + agent.vsbForNewDepthSet.getExplorationFactor() +
                ", explorationFactorLimit =" + this.explorationFactorLimit +
                ", fraction loose nodes =" + agent.vsbForNewDepthSet.getFractionLooseNodes() +
                ", vsbForNewDepthSet size =" + agent.vsbForNewDepthSet.size());
    }

    protected void logWarningIfMotivated() {

        if (agent.vsbForNewDepthSet.size()<vsbForNewDepthSetSizePrev) {
            logger.warning("Size of vsbForNewDepthSet decreased, consider increasing PROB_SELECT_FROM_PREVIOUS_DEPTH and/or decreasing PROB_SELECT_STATE_FROM_NEW_DEPTH_SET  ");
        }
        vsbForNewDepthSetSizePrev=agent.vsbForNewDepthSet.size();
        }

}
