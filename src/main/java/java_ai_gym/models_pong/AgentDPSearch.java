package java_ai_gym.models_pong;

import java_ai_gym.helpers.CpuTimeAccumulator;
import java_ai_gym.helpers.CpuTimer;
import java_ai_gym.helpers.MathUtils;
import java_ai_gym.models_common.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/***
 * This class is an abstract class used for dynamic programming based search.
 * Settings are defined by the constructor and search is the only public method.
 * The idea is to let the search depth increase stepwise. In increases when a specific
 * fraction of actions has been tested for the states so far visited.
 */

@Getter
@Setter
public abstract class AgentDPSearch extends AgentSearch {

    final double EF_LIMIT_DEFAULT = 0.7;
    final double DISCOUNT_FACTOR_REWARD_DEFAULT = 0.9;
    final double DISCOUNT_FACTOR_EXP_FACTOR_DEFAULT = 0.99;
    final int SEARCH_DEPTH_UPPER_DEFAULT = 100;
    final double EXP_FACTOR_LIMIT_MIN=0.6;

    final int MAX_NOF_SELECTION_TRIES = 1000;
    double VSB_SIZE_INCREASE_FACTOR = 2.0;
    final double PROB_SELECT_STATE_FROM_NEW_DEPTH_SET = 0.9;  //0.5
    final double PROB_SELECT_FROM_OPTIMAL_PATH = 0.5;

    double explorationFactorLimitStart;
    double explorationFactorLimit;
    double discountFactorReward;
    double discountFactorExpFactor;
    int searchDepthUpper;
    int searchDepthStep;

    int searchDepthPrev;
    int searchDepth;
    double explorationFactor;
    StateForSearch startState;
    List<Integer> evaluatedSearchDepths;
    VisitedStatesBuffer vsb;
    VisitedStatesBuffer vsbForNewDepthSet;
    List<StateForSearch> optimalStateSequence;
    int nofStatesVsbForNewDepthSetPrev;
    boolean wasSelectStateFailing;

    BellmanCalculator bellmanCalculator;
    DPSearchStateSelector dpSearchStateSelector;
    DPSearchServant dpSearchServants;
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
        this.wasSelectStateFailing = false;
        this.optimalStateSequence = new ArrayList<>();
        this.timeAccumulatorSelectState = new CpuTimeAccumulator();
        this.timeAccumulatorStep = new CpuTimeAccumulator();
        this.timeAccumulatorBellman = new CpuTimeAccumulator();
        this.timeAccumulatorExpFactor = new CpuTimeAccumulator();
        this.searchDepthUpper = SEARCH_DEPTH_UPPER_DEFAULT;
        this.explorationFactorLimit = EF_LIMIT_DEFAULT;
        this.explorationFactorLimitStart = explorationFactorLimit;
        this.discountFactorReward = DISCOUNT_FACTOR_REWARD_DEFAULT;
        this.discountFactorExpFactor = DISCOUNT_FACTOR_EXP_FACTOR_DEFAULT;
        this.dpSearchStateSelector=new DPSearchStateSelector(this);
        this.dpSearchServants= new DPSearchServant(this);
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
        this.explorationFactorLimit = explorationFactorLimit;
        this.explorationFactorLimitStart = explorationFactorLimit;
        this.discountFactorReward = discountFactorReward;
        this.discountFactorExpFactor = discountFactorExpFactor;
    }

    @Override
    public SearchResults search(final StateForSearch startState) {

        this.dpSearchServants.initInstanceVariables(startState);
        this.dpSearchServants.resetAgent();

        while (timeAndDepthNotExceededAndNoFailure()) {
            StateForSearch selectedState = dpSearchStateSelector.selectState();  //can be of type NullState
            takeStepAndSaveExperience(selectedState);

            if (hasVsbSizeIncreasedSignificantly()) {
                nofStatesVsbForNewDepthSetPrev = vsbForNewDepthSet.size();
                timeAccumulatorExpFactor.play();
                explorationFactor = vsbForNewDepthSet.calcExplorationFactor(searchDepth);
                timeAccumulatorExpFactor.pause();
                this.dpSearchServants.logProgress();
            }

            if (isAnyStateAtSearchDepth() && areManyActionsTested()) {
                this.dpSearchServants.increaseSearchDepthDoResets();
                performDynamicProgramming();
            }
        }

        SearchResults searchResults = super.defineSearchResults(bellmanCalculator.actionsOptPath, this.startState);
        this.dpSearchServants.printResultInfo();
        return searchResults;
    }


    private void takeStepAndSaveExperience(StateForSearch selectedState) {

        if (wasSelectStateFailing || selectedState == null) {
            logger.warning("Cant step when failed or null state selection");
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
        return !timeBudgetChecker.isTimeExceeded() && searchDepth <= searchDepthUpper && !wasSearchFailing();
    }

    boolean areManyActionsTested() {
        return explorationFactor >= explorationFactorLimit || wasSelectStateFailing; //isSelectFailed
    }

    boolean isAnyStateAtSearchDepth() {
        return vsb.getDepthMax() >= searchDepth;
    }

    public boolean wasSearchFailing() {
        return !isAnyStateAtSearchDepth() && wasSelectStateFailing;
    }

    private boolean hasVsbSizeIncreasedSignificantly() {
        return (double) vsbForNewDepthSet.size() / (double) nofStatesVsbForNewDepthSetPrev > VSB_SIZE_INCREASE_FACTOR;
    }

}
