package java_ai_gym.models_pong;

import java_ai_gym.models_common.AgentSearch;
import java_ai_gym.models_common.StateForSearch;

import java.util.logging.Logger;

/**--------------- below are methods of more dummy/supporting nature ------------------
**/
public class DPSearchServants {

    protected final static Logger logger = Logger.getLogger(DPSearchServants.class.getName());

    AgentDPSearch agent;

    public DPSearchServants(AgentDPSearch agentDPSearch) {
        this.agent=agentDPSearch;
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
    }

    public void increaseSearchDepthDoResets() {
        if (agent.vsb.getDepthMax() > agent.searchDepth) {
            logger.warning("vsb.getMaxDepth() > searchDept");
        }
        logger.info("increaseSearchDepthDoResets" + ", getDepthMax =" + agent.vsb.getDepthMax() + ", searchDepth =" + agent.searchDepth);
        addEvaluatedSearchDepth(agent.searchDepth);
        agent.searchDepthPrev = agent.searchDepth;
        agent.searchDepth = agent.searchDepth + agent.searchDepthStep;
        agent.vsbForNewDepthSet.clear();
        agent.explorationFactorLimit = agent.explorationFactorLimitStart * Math.pow(agent.discountFactorExpFactor, agent.searchDepthPrev);
        agent.nofStatesVsbForNewDepthSetPrev = 1;
        agent.explorationFactor = 0;
        agent.wasSelectStateFailing = false;
        logger.fine("searchDept increased to = " + agent.searchDepth + ". VSB size = " + agent.vsb.size());
    }

    public void addEvaluatedSearchDepth(int searchDepth) {
        agent.evaluatedSearchDepths.add(searchDepth);
    }


    public void initInstanceVariables(StateForSearch startState) {
        agent.startState = new StateForSearch(startState);
        int nofActions = agent.getActionSet(startState).size();
        agent.startState.setIdDepthNofActions(agent.startState.START_STATE_ID, 0, nofActions);
        agent.vsb = new VisitedStatesBuffer(agent.startState);
       // agent.startTime = System.currentTimeMillis();  //starting time, long <=> minimum value of 0
        agent.vsbForNewDepthSet = new VisitedStatesBuffer();
        agent.nofStatesVsbForNewDepthSetPrev = 1;
        agent.searchDepth = agent.searchDepthStep;
        agent.searchDepthPrev = 0;
        agent.explorationFactor = 0;
        agent.explorationFactorLimit = agent.explorationFactorLimitStart;
        agent.bellmanCalculator = new BellmanCalculator(agent.vsb, new FindMax(), agent.searchDepthPrev, agent.discountFactorReward, agent.getTimeBudgetChecker());
    }




    protected void logsForFailedToFindState(StateForSearch selectedState) {
        logger.warning("MAX_NOF_SELECTION_TRIES exceeded !!!");
        logger.warning("id =" + selectedState.id +
                ", depth =" + selectedState.depth +
                // ", null status =" + (selectedState == null) +
                ", depth status =" + (selectedState.depth == agent.searchDepth) +
                ", nofActionsTested status =" + (agent.vsb.nofActionsTested(selectedState.id) == selectedState.nofActions) +
                ",isExperienceOfStateTerminal =" + agent.vsb.isExperienceOfStateTerminal(selectedState.id));
    }


    protected void printResultInfo() {
        logger.info("search finished, vsb size = " + agent.vsb.size());
        System.out.println("statesAtDepth vsb = " + agent.vsb.calcStatesAtDepth(agent.searchDepth));
        System.out.println("statesAtDepth vsbForSpecificDepthStep= " + agent.vsbForNewDepthSet.calcStatesAtDepth(agent.searchDepth));
        System.out.println("searchDepth = " + agent.searchDepth + ", searchDepthPrev = " + agent.searchDepthPrev); // + ", explorationFactor = " + vsbForNewDepthSet.calcExplorationFactor(searchDepth));
        System.out.println("evaluatedSearchDepths = " + agent.evaluatedSearchDepths);
        System.out.println("maxDepth  = " + agent.vsb.getDepthMax());
        System.out.println("isAnyStateAtSearchDepth() = " + agent.isAnyStateAtSearchDepth() + ", areManyActionsTested() = " + agent.areManyActionsTested() + ", wasSelectStateFailing = " + agent.wasSelectStateFailing);
        if (agent.wasSearchFailing()) {
            logger.warning("Failed search, despite many steps there is no state at search depth, i.e end of search horizon");
        }

        System.out.println("time total = " + agent.getTimeBudgetChecker().getTimeSinceStartInMillis() +
                ", timeStep accum = " + agent.getTimeAccumulatorStep().getAccumulatedTimeMillis() +
                ", timeBellman accum = " + agent.getTimeAccumulatorBellman().getAccumulatedTimeMillis() +
                ", timeExpFactor accum = " + agent.getTimeAccumulatorExpFactor().getAccumulatedTimeMillis() +
                ", timeSelect accum = " + agent.getTimeAccumulatorSelectState().getAccumulatedTimeMillis());

    }

    protected void logProgress() {
        logger.info("searchDepth =" + agent.searchDepth + ", explorationFactor =" + agent.explorationFactor + ", explorationFactorLimit =" + agent.explorationFactorLimit + ", time =" + agent.getTimeBudgetChecker().getTimeSinceStartInMillis() + ", vsbForNewDepthSet size =" + agent.vsbForNewDepthSet.size());
    }

}
