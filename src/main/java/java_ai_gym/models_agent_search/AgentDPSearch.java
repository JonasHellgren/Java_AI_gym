package java_ai_gym.models_agent_search;

import java_ai_gym.helpers.CpuTimeAccumulator;
import java_ai_gym.helpers.CpuTimer;
import java_ai_gym.helpers.MathUtils;
import java_ai_gym.models_common.*;
import java_ai_gym.models_pong.SinglePong;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/***
 * This class is an abstract class used for dynamic programming based tree search.
 * Settings are defined by the constructor and search is the only public method.
 * The idea is to let the search depth increase stepwise. It increases when a specific
 * fraction of actions has been tested for the states so far visited.
 */

@Getter
@Setter
public abstract class AgentDPSearch extends AgentSearch {

    final double EF_LIMIT_DEFAULT = 0.7;
    final double DISCOUNT_FACTOR_REWARD_DEFAULT = 0.9;
    final double DISCOUNT_FACTOR_EXP_FACTOR_DEFAULT = 0.99;
    final int SEARCH_DEPTH_UPPER_DEFAULT = 100;
    final double EXPLORATION_FACTOR_LIMIT_MIN =0.2;
    final double FRACTION_LOOSE_NODES_MAX =0.1;
    final double VSB_SIZE_INCREASE_FACTOR_MIN=1.5;

    final int MAX_NOF_SELECTION_TRIES = 1000;
    double VSB_SIZE_INCREASE_FACTOR = 10.0; //determines how frequently vsb "health" measures is calculated
    final double PROB_SELECT_STATE_FROM_NEW_DEPTH_SET = 0.90;  //0.5
    final double PROB_SELECT_FROM_OPTIMAL_PATH = 0.1;
    final double PROB_SELECT_FROM_PREVIOUS_DEPTH =0.1;  //0.5

    int searchDepthUpper;  //upper limit of search depth, if large, timeBudget restricts
    int searchDepthStep;  //search depth increase step
    int searchDepthPrev;    //search depth in previous set, new states are mostly in set [searchDepthPrev,searchDepth]
    int searchDepth;        //present search depth
    StateForSearch startState;
    List<Integer> evaluatedSearchDepths;
    VisitedStatesBuffer vsb;   //all states visited in single search call
    VisitedStatesBuffer vsbForNewDepthSet;  //vsb in new "territory"
    List<StateForSearch> optimalStateSequence;
    int vsbSizeForNewDepthSetAtPreviousExplorationFactorCalculation;

    BellmanCalculator bellmanCalculator;
    DPSearchStateSelector dpSearchStateSelector;
    DPSearchServant dpSearchServant;
    protected CpuTimeAccumulator timeAccumulatorSelectState;
    protected CpuTimeAccumulator timeAccumulatorStep;
    protected CpuTimeAccumulator timeAccumulatorBellman;
    protected CpuTimeAccumulator timeAccumulatorExpFactor;

    public AgentDPSearch(SinglePong env,
                         long timeBudget,
                         int searchDepthStep) {
        super(timeBudget, env, env.parameters);
        this.searchDepthStep = searchDepthStep;
        this.searchDepth = searchDepthStep;
        this.evaluatedSearchDepths = new ArrayList<>();
        super.timeBudgetChecker = new CpuTimer(timeBudget);
        this.optimalStateSequence = new ArrayList<>();
        this.timeAccumulatorSelectState = new CpuTimeAccumulator();
        this.timeAccumulatorStep = new CpuTimeAccumulator();
        this.timeAccumulatorBellman = new CpuTimeAccumulator();
        this.timeAccumulatorExpFactor = new CpuTimeAccumulator();
        this.searchDepthUpper = SEARCH_DEPTH_UPPER_DEFAULT;
        this.dpSearchStateSelector=new DPSearchStateSelector(this);
        this.dpSearchServant = new DPSearchServant(this,EF_LIMIT_DEFAULT,DISCOUNT_FACTOR_EXP_FACTOR_DEFAULT,DISCOUNT_FACTOR_REWARD_DEFAULT);
    }

    public AgentDPSearch(SinglePong env,
                         long timeBudget,
                         int searchStepUpper,
                         int searchDepthStep,
                         double explorationFactorLimit,
                         double discountFactorReward,
                         double discountFactorExpFactor) {
        this(env, timeBudget, searchDepthStep);
        this.searchDepthUpper = searchStepUpper;
        this.dpSearchServant = new DPSearchServant(this,explorationFactorLimit,discountFactorExpFactor,discountFactorReward);
    }

    @Override
    public SearchResults search(final StateForSearch startState) {

        this.dpSearchServant.initInstanceVariables(startState);
        this.dpSearchServant.resetAgent();

        while (timeAndDepthNotExceededAndNoFailure()) {
            StateForSearch selectedState = dpSearchStateSelector.selectState();  //null <=> failed
            takeStepAndSaveExperience(selectedState);

            if (dpSearchStateSelector.wasPrimarySelectStateFailing()) {
                changeSelectStateTypeToBackupAndRetrySelectStateAndStep();
            }

            if (hasVsbSizeIncreasedSignificantly()) {
                calculateSomeVsbMeasures();
            }

            if (isAnyStateAtSearchDepth() && areManyActionsTestedAndFewLooseNodesAndVsbBigEnough()) {
                increaseSearchDepthAndSomeMoreStuff();
                performDynamicProgramming();
            }
        }

        SearchResults searchResults = super.defineSearchResults(bellmanCalculator.actionsOptPath, this.startState);
        this.dpSearchServant.printResultInfo();
        return searchResults;
    }

    private void calculateSomeVsbMeasures() {
        vsbSizeForNewDepthSetAtPreviousExplorationFactorCalculation = vsbForNewDepthSet.size();
        timeAccumulatorExpFactor.play();
        vsbForNewDepthSet.calcExplorationFactor(searchDepth);
        vsbForNewDepthSet.calcFractionLooseNodes(searchDepth);
        timeAccumulatorExpFactor.pause();
        this.dpSearchServant.logProgress1();
    }

    private void increaseSearchDepthAndSomeMoreStuff() {
        vsbForNewDepthSet.getBufferHealthCalculator().setNofStatesBeforePreviousDpCalc();
        this.dpSearchServant.logWarningIfMotivated();
        this.dpSearchServant.addEvaluatedSearchDepth(searchDepth);
        this.dpSearchServant.increaseSearchDepth();
        this.dpSearchServant.doResets();
        this.dpSearchServant.updateExplorationFactorLimit();
    }

    private void changeSelectStateTypeToBackupAndRetrySelectStateAndStep() {
        StateForSearch selectedState;
        logger.fine("wasPrimarySelectStateFailing == true. Switching state selector to backup type.");
        vsbForNewDepthSet.calcExplorationFactor(searchDepth);
        dpSearchStateSelector.setStateSelectorAsBackupType();
        selectedState = dpSearchStateSelector.selectState();
        takeStepAndSaveExperience(selectedState);
    }


    private void takeStepAndSaveExperience(StateForSearch selectedState) {

        if (selectedState == null) {
            logger.fine("Cant step when null state selection");
        } else {
            timeAccumulatorStep.play();
            int action = this.chooseAction(selectedState,vsb);
            StepReturn stepReturn = env.step(action, selectedState);
            StateForSearch stateNew = (StateForSearch) stepReturn.state;
            int nofActions = getActionSet(stateNew).size();
            stateNew.setDepthNofActions(selectedState.depth + 1, nofActions);
            vsb.addNewStateAndExperienceFromStep(selectedState.id, action, stepReturn);

            if (stateNew.depth >= searchDepthPrev) {
                vsbForNewDepthSet.addNewStateAndExperienceFromStep(selectedState.id, action, stepReturn);
            }
            timeAccumulatorStep.pause();
        }
    }

    private void performDynamicProgramming() {

        this.timeAccumulatorBellman.play();
        int maxEvaluatedDepth = MathUtils.maxInIntegerList(evaluatedSearchDepths);
        bellmanCalculator.setNodeValues(maxEvaluatedDepth);
        if (!bellmanCalculator.isTimeExceeded()) {
            this.optimalStateSequence = bellmanCalculator.findNodesOnOptimalPath(this.startState);
        }
        this.timeAccumulatorBellman.pause();
    }


    private boolean timeAndDepthNotExceededAndNoFailure() {
        return !timeBudgetChecker.isTimeExceeded() &&
                searchDepth <= searchDepthUpper &&
                !failedStateSelectionAndAllActionsTested();
    }

    private boolean failedStateSelectionAndAllActionsTested() {
        if (dpSearchStateSelector.wasBackupSelectStateFailing()) {
            vsbForNewDepthSet.calcExplorationFactor(searchDepth);  //time consuming => only when needed
        }

        return dpSearchStateSelector.wasBackupSelectStateFailing() &&
                (vsbForNewDepthSet.explorationFactor==1 || vsbForNewDepthSet.isNoStateFulfillsCriteriaForExplorationFactorCalculation());
    }

    boolean areManyActionsTestedAndFewLooseNodesAndVsbBigEnough() {
        return  vsbForNewDepthSet.getExplorationFactor() >= dpSearchServant.explorationFactorLimit &&
                vsbForNewDepthSet.getFractionLooseNodes() <= FRACTION_LOOSE_NODES_MAX &&
                vsbForNewDepthSet.getBufferHealthCalculator().isVsbBigEnough(VSB_SIZE_INCREASE_FACTOR_MIN) ||
                dpSearchStateSelector.wasBackupSelectStateFailing();
    }

    boolean isAnyStateAtSearchDepth() {
        return vsb.getDepthMax() >= searchDepth;
    }

    public boolean wasSearchFailing() {
        return !isAnyStateAtSearchDepth() && dpSearchStateSelector.wasBackupSelectStateFailing();
    }

    private boolean hasVsbSizeIncreasedSignificantly() {
        return (double) vsbForNewDepthSet.size() /
                (double) vsbSizeForNewDepthSetAtPreviousExplorationFactorCalculation
                > VSB_SIZE_INCREASE_FACTOR;
    }

}
