package java_ai_gym.models_pong;

import java_ai_gym.helpers.CpuTimeAccumulator;
import java_ai_gym.helpers.CpuTimer;
import java_ai_gym.helpers.MathUtils;
import java_ai_gym.models_common.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class AgentDPSearch extends AgentSearch {

    final double EF_LIMIT_DEFAULT = 0.4;
    final double DISCOUNT_FACTOR_DEFAULT = 0.99;

    final int MAX_NOF_SELECTION_TRIES = 1000;
    double VSB_SIZE_INCREASE_FACTOR = 5.0;
    final double PROB_SELECT_STATE_FROM_NEW_DEPTH_STEP = 0.9;  //0.5
    final double PROB_SELECT_FROM_OPTIMAL_PATH = 0.5;

    double explorationFactorLimit;
    double discountFactor;
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
    protected CpuTimeAccumulator timeSelectState;
    protected CpuTimeAccumulator timeAccumulatorStep;
    protected CpuTimeAccumulator timeAccumulatorBellman;
    protected CpuTimeAccumulator timeAccumulatorExpFactor;


    public abstract int getActionDefault();
    public abstract List<Integer> getActionSet();

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
        this.timeSelectState = new CpuTimeAccumulator();
        this.timeAccumulatorStep = new CpuTimeAccumulator();
        this.timeAccumulatorBellman = new CpuTimeAccumulator();
        this.timeAccumulatorExpFactor = new CpuTimeAccumulator();
        this.explorationFactorLimit = EF_LIMIT_DEFAULT;
        this.discountFactor = DISCOUNT_FACTOR_DEFAULT;
    }

    public AgentDPSearch(SinglePong env,
                         long timeBudget,
                         int searchDepthStep,
                         double explorationFactorLimit,
                         double discountFactor) {
        this(env, timeBudget, searchDepthStep);
        this.explorationFactorLimit = explorationFactorLimit;
        this.discountFactor = discountFactor;
    }

    @Override
    public SearchResults search(final StateForSearch startState) {

        initInstanceVariables(startState);
        reset();

        while (!timeBudgetChecker.isTimeExceeded() && searchDepth<=10 && !wasSearchFailing()) {
            StateForSearch selectedState = this.selectState();  //can be of type NullState
            int nofActions = getActionSet().size();
            takeStepAndSaveExperience(nofActions, selectedState);
            ifMotivatedShowLogs( selectedState);

            if (hasVsbSizeIncreasedSignificantly()) {
                nofStatesVsbForNewDepthSetPrev = vsbForNewDepthSet.size();
                timeAccumulatorExpFactor.play();
                explorationFactor = vsbForNewDepthSet.calcExplorationFactor(searchDepth);
                timeAccumulatorExpFactor.pause();
                logProgress();
            }

            if (isAnyStateAtSearchDepth() && areManyActionsTested()) {
                increaseSearchDepthDoResets();
                performDynamicProgramming();
            }
        }

        SearchResults searchResults = super.defineSearchResults(bellmanCalculator.actionsOptPath, this.startState);
        printResultInfo();
       // reset();
        return searchResults;
    }



    public StateForSearch selectState() {
        StateForSearch selectedState=null;  //hopefullt will change type later
        timeSelectState.play();
        for (int j = 0; j < MAX_NOF_SELECTION_TRIES; j++) {
            if (MathUtils.calcRandomFromIntervall(0, 1) < PROB_SELECT_STATE_FROM_NEW_DEPTH_STEP && vsbForNewDepthSet.size() > 0) {
                selectedState = vsbForNewDepthSet.selectRandomState();
            } else {
                if (MathUtils.calcRandomFromIntervall(0, 1) < PROB_SELECT_FROM_OPTIMAL_PATH && optimalStateSequence.size() > 0) {
                    selectedState = optimalStateSequence.get(MathUtils.randInt(0, optimalStateSequence.size() - 1));
                } else {
                    selectedState = vsb.selectRandomState();
                }
            }

            if (!isNullOrTerminalStateOrAllActionsTestedOrIsAtSearchDepth(selectedState)) {
                wasSelectStateFailing = false;
                timeSelectState.pause();
                return selectedState;
            }
        }

        timeSelectState.pause();
        wasSelectStateFailing = true;
        logsForFailedToFindState(selectedState);
        return  null; //getStateForSearchIfFailedToFind(selectedState);
    }

    private void takeStepAndSaveExperience(int nofActions, StateForSearch selectedState) {
        if (wasSelectStateFailing) {
            logger.warning("Cant step when failed state selection");
        } else {
            timeAccumulatorStep.play();
            int action = this.chooseAction(selectedState);
            StepReturn stepReturn = env.step(action, selectedState);
            StateForSearch stateNew = (StateForSearch) stepReturn.state;
            stateNew.setDepthNofActions(selectedState.depth + 1, nofActions);
            vsb.addNewStateAndExperienceFromStep(selectedState.id, action, stepReturn);
            timeAccumulatorStep.pause();

            if (stateNew.depth >= searchDepthPrev) {
                vsbForNewDepthSet.addNewStateAndExperienceFromStep(selectedState.id, action, stepReturn);
            }
        }
    }

    private void increaseSearchDepthDoResets() {
        if (vsb.getDepthMax() > searchDepth) {
            logger.warning("vsb.getMaxDepth() > searchDept");
        }
        logger.info("increaseSearchDepthDoResets"+", getDepthMax ="+vsb.getDepthMax()+ ", searchDepth =" + searchDepth);
        addEvaluatedSearchDepth(searchDepth);
        searchDepthPrev = searchDepth;
        searchDepth = searchDepth + searchDepthStep;
        vsbForNewDepthSet.clear();
        nofStatesVsbForNewDepthSetPrev=1;
        explorationFactor = 0;
        wasSelectStateFailing =false;
        logger.fine("searchDept increased to = " + searchDepth + ". VSB size = " + vsb.size());
    }

    private void performDynamicProgramming() {
        BellmanCalculator bellmanCalculator = new BellmanCalculator(vsb, new FindMax(), searchDepthPrev, discountFactor, timeBudgetChecker);
        if (!bellmanCalculator.timeExceed) {
            this.timeAccumulatorBellman.play();
            bellmanCalculator.setNodeValues();
            //logger.fine("setNodeValues (millis) = " + timeAccumulatorStep.getTimeInMillis() + ", isTimeExceeded = " + bellmanCalculator.isTimeExceeded());
            this.optimalStateSequence = bellmanCalculator.findNodesOnOptimalPath(this.startState);
            this.bellmanCalculator = bellmanCalculator;

            this.timeAccumulatorBellman.pause();
        }
    }

    //--------------- below are methods of more dummy/supporting nature ------------------

    public void reset() {
         vsbForNewDepthSet.clear();
        evaluatedSearchDepths.clear();
        optimalStateSequence.clear();
        timeBudgetChecker.reset();

    }

    private boolean areManyActionsTested() {
        return explorationFactor >= explorationFactorLimit || wasSelectStateFailing; //isSelectFailed
    }

    private boolean isAnyStateAtSearchDepth() {
        return vsb.getDepthMax() >= searchDepth;
    }

    public boolean wasSearchFailing() {
        return !isAnyStateAtSearchDepth() && wasSelectStateFailing;
    }

    private boolean hasVsbSizeIncreasedSignificantly() {
        return (double) vsbForNewDepthSet.size() / (double) nofStatesVsbForNewDepthSetPrev > VSB_SIZE_INCREASE_FACTOR;
    }

    private void ifMotivatedShowLogs(StateForSearch selectedState) {
        if (wasSelectStateFailing) {
            logger.warning("isSelectFailed, searchDepth = " + searchDepth);
        }

        if (selectedState==null) {
            logger.warning("selectedState is null");
        } else {

            if (selectedState.depth > searchDepth) {
                logger.warning("selectedState has to high search depth = " + selectedState.depth + ". searchDepth= " + searchDepth);
                System.out.println("vsb contains = " + vsb.getStateVisitsDAO().contains(selectedState.id));
                System.out.println("vsbForNewDepthSet contains = " + vsbForNewDepthSet.getStateVisitsDAO().contains(selectedState.id));
            }

            if (vsb.getDepthMax() > searchDepth) {
                logger.warning("vsb.getDepthMax()>searchDepthh = " + vsb.getDepthMax() + ". searchDepth= " + searchDepth);
            }
        }
    }

    private void printResultInfo() {
        logger.info("search finished, vsb size = " + vsb.size());
        System.out.println("statesAtDepth vsb = " + vsb.calcStatesAtDepth(searchDepth));
        System.out.println("statesAtDepth vsbForSpecificDepthStep= " + vsbForNewDepthSet.calcStatesAtDepth(searchDepth));
        System.out.println("searchDepth = " + searchDepth + ", searchDepthPrev = " + searchDepthPrev + ", explorationFactor = " + vsbForNewDepthSet.calcExplorationFactor(searchDepth));
        System.out.println("evaluatedSearchDepths = " + evaluatedSearchDepths);
        System.out.println("maxDepth  = " + vsb.getDepthMax());
        System.out.println("isAnyStateAtSearchDepth() = " + isAnyStateAtSearchDepth() + ", areManyActionsTested() = " + areManyActionsTested()+ ", wasSelectStateFailing = " + wasSelectStateFailing);
        if (wasSearchFailing()) {
            logger.warning("Failed search, despite many steps there is no state at search depth, i.e end of search horizon");
        }

        System.out.println("time total = "+timeBudgetChecker.getTimeSinceStartInMillis()+
                ", timeStep accum = "+timeAccumulatorStep.getAccumulatedTimeMillis()+
                ", timeBellman accum = "+timeAccumulatorBellman.getAccumulatedTimeMillis()+
                ", timeExpFactor accum = "+timeAccumulatorExpFactor.getAccumulatedTimeMillis()+
                ", timeSelect accum = "+timeSelectState.getAccumulatedTimeMillis());

    }

    private void logProgress() {
        logger.info("searchDepth ="+searchDepth+", explorationFactor ="+explorationFactor+", time ="+ timeBudgetChecker.getTimeSinceStartInMillis()+", vsbForNewDepthSet size ="+vsbForNewDepthSet.size());
    }

    public void initInstanceVariables(StateForSearch startState) {
        this.startState = new StateForSearch(startState);
        int nofActions = getActionSet().size();
        this.startState.setIdDepthNofActions(this.startState.START_STATE_ID, 0, nofActions);
        this.vsb = new VisitedStatesBuffer(this.startState);
        this.startTime = System.currentTimeMillis();  //starting time, long <=> minimum value of 0
        this.vsbForNewDepthSet = new VisitedStatesBuffer();
        this.nofStatesVsbForNewDepthSetPrev = 1;
        this.searchDepth = searchDepthStep;
        this.searchDepthPrev = 0;
        this.explorationFactor = 0;
        this.bellmanCalculator = new BellmanCalculator(vsb, new FindMax(), searchDepthPrev, discountFactor, timeBudgetChecker);
    }

    public void addEvaluatedSearchDepth(int searchDepth) {
        evaluatedSearchDepths.add(searchDepth);
    }

    public int chooseAction(StateForSearch selectState) {
        int action;
        if (vsb.nofActionsTested(selectState.id) == 0) {
            action = getActionDefault();
        } else {
            List<Integer> grossActions = getActionSet();
            List<Integer> testedActions = vsb.testedActions(selectState.id);
            List<Integer> nonTestedActions = MathUtils.getDifferenceBetweenLists(grossActions, testedActions);
            if (nonTestedActions.isEmpty()) {
                action = getActionDefault();
                logger.warning("nonTestedActions is empty");
            } else {
                action = chooseRandomAction(nonTestedActions);
            }
        }
        return action;
    }


    private void logsForFailedToFindState(StateForSearch selectedState) {
        logger.warning("MAX_NOF_SELECTION_TRIES exceeded !!!");
        logger.warning("id =" + selectedState.id +
                ", depth =" + selectedState.depth +
                ", null status =" + (selectedState == null) +
                ", depth status =" + (selectedState.depth == searchDepth) +
                ", nofActionsTested status =" + (vsb.nofActionsTested(selectedState.id) == selectedState.nofActions) +
                ",isExperienceOfStateTerminal =" + vsb.isExperienceOfStateTerminal(selectedState.id));
    }

    public boolean isNullOrTerminalStateOrAllActionsTestedOrIsAtSearchDepth(StateForSearch state) {

        //fail fast => speeding up
        if (state == null) {
            return true;
        }

        if (state.depth == searchDepth) {
            return true;
        }

        if (vsb.nofActionsTested(state.id) == state.nofActions) {
            return true;
        }

        if (vsb.isExperienceOfStateTerminal(state.id)) {
            return true;
        }

        return false;
    }


}
