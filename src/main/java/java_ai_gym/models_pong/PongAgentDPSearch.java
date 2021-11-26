package java_ai_gym.models_pong;

import java_ai_gym.helpers.MathUtils;
import java_ai_gym.models_common.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PongAgentDPSearch extends AgentSearch {

    final int MAX_NOF_SELECTION_TRIES = 1000;
    final int ACTION_DEFAULT = 1;
    double K=2.0;
    final double EF_LIMIT=0.5;
    final double PROB_SELECT_STATE_FROM_NEW_DEPTH_STEP=0.95;

    int searchDepthStep;

    int searchDepthPrev;
    int searchDepth;
    List<Integer> evaluatedSearchDepths;
    VisitedStatesBuffer vsb;
    VisitedStatesBuffer trimmedVSB;
    VisitedStatesBuffer vsbForNewDepthSet;
    StateForSearch startState;
    long startTimeForSpeedTesting;
    int nofStatesVsbForNewDepthSetPrev;

    public PongAgentDPSearch(SinglePong env,
                             long timeBudget,
                             int searchDepthStep) {
        super(timeBudget, env, env.parameters);
        this.searchDepthStep = searchDepthStep;
        this.searchDepth = searchDepthStep;
        this.evaluatedSearchDepths=new ArrayList<>();

        //this.state = new StateForSearch(env.getTemplateState());

    }


    @Override
    public SearchResults search(final StateForSearch startState) {

        initInstanceVariables(startState);
        int nofActions = envParams.discreteActionsSpace.size();
        int nofSteps = 0;
        double explorationFactor = 0;

        while (!timeExceeded() && nofSteps<200) {  //TODO remove nofSteps
            StateForSearch selectedState = this.selectState();
            takeStepAndSaveExperience(nofActions, selectedState);

            if ((double) vsbForNewDepthSet.size()/(double)nofStatesVsbForNewDepthSetPrev >K)  {
                nofStatesVsbForNewDepthSetPrev= vsbForNewDepthSet.size();
                explorationFactor= vsbForNewDepthSet.calcExplorationFactor(searchDepth);
                showLogs1(nofSteps, explorationFactor);
            }

            if (vsb.getDepthMax()>=searchDepth && explorationFactor>=EF_LIMIT)  {
                increaseSearchDepth();
                vsbForNewDepthSet.clear();
                explorationFactor =0;
                showLogs2(nofSteps);
                startTimerForSpeedTest();
                findBestPath();
                showElapsedTimeSpeedTest("performDynamicProgramming",true);
            }

            nofSteps++;
        }

        logger.info("search finished, vsb size = "+vsb.size());
        showLogs2(nofSteps);

        return null;
    }

    private void showLogs1(int nofSteps, double explorationFactor) {
        System.out.println("statesPerDepth vsb = "+vsb.calcStatesPerDepth(searchDepth));
        System.out.println("statesPerDepth vsbForSpecificDepthStep= "+ vsbForNewDepthSet.calcStatesPerDepth(searchDepth));
        logger.info("nofSteps = "+ nofSteps +", explorationFactor = "+ explorationFactor);
    }

    private void showLogs2(int nofSteps) {
        logger.info("searchDept increased to = "+searchDepth+". VSB size = "+vsb.size()+", nofSteps = "+ nofSteps);
        System.out.println("statesPerDepth vsb = "+vsb.calcStatesPerDepth(searchDepth));
        System.out.println("statesPerDepth vsbForSpecificDepthStep= "+ vsbForNewDepthSet.calcStatesPerDepth(searchDepth));
        System.out.println("searchDepthPrev = "+searchDepthPrev);
    }

    private void takeStepAndSaveExperience(int nofActions, StateForSearch selectedState) {
        int action = this.chooseAction(selectedState);
        StepReturn stepReturn = env.step(action, selectedState);
        StateForSearch stateNew = (StateForSearch) stepReturn.state;
        stateNew.setDepthNofActions(selectedState.depth + 1, nofActions);
        vsb.addNewStateAndExperienceFromStep(selectedState.id, action, stepReturn);

        if (stateNew.depth>=searchDepthPrev) {
        vsbForNewDepthSet.addNewStateAndExperienceFromStep(selectedState.id, action, stepReturn);
        }
    }

    public void initInstanceVariables(StateForSearch startState) {

        this.startState = new StateForSearch(startState);
        int nofActions = envParams.discreteActionsSpace.size();
        this.startState.setIdDepthNofActions(this.startState.START_STATE_ID, 0, nofActions);
        this.vsb = new VisitedStatesBuffer(this.startState);
        this.startTime = System.currentTimeMillis();  //starting time, long <=> minimum value of 0
        this.trimmedVSB=vsb;
        this.vsbForNewDepthSet =new VisitedStatesBuffer();
        this.nofStatesVsbForNewDepthSetPrev=1;
        this.searchDepth = searchDepthStep;
        this.searchDepthPrev=0;
    }


    private void startTimerForSpeedTest() {
        startTimeForSpeedTesting = System.nanoTime();  //starting time, long <=> minimum value of 0
    }

    private void showElapsedTimeSpeedTest(String methodName, boolean flag) {
        if (flag)
        System.out.println(methodName + " time (millis) = "+(System.nanoTime()- startTimeForSpeedTesting)/1000000);
    }

    private void findBestPath() {

        //todo, avoid passing in this, for timeExceeded access, bidirection dep
        trimmedVSB =  vsb.createNewVSBWithNoLooseNodesBelowDepth(searchDepthPrev,this);
        if (trimmedVSB.anyLooseNodeBelowDepth(trimmedVSB, searchDepthPrev)) {
            logger.warning("removeLooseNodesBelowDepth failed, still loose node(s).");
        }



        logger.info(". VSB trimmed size = "+trimmedVSB.size());
    }

    private void increaseSearchDepth() {
        if (vsb.getDepthMax() > searchDepth) {
             logger.warning("vsb.getMaxDepth() > searchDept");
         }
        addEvaluatedSearchDepth(searchDepth);
        searchDepthPrev =searchDepth;
        searchDepth=searchDepth+searchDepthStep;
    }

    public void addEvaluatedSearchDepth(int searchDepth) {
        evaluatedSearchDepths.add(searchDepth);
    }



    public int chooseAction(StateForSearch selectState) {
        int action;
        if (vsb.nofActionsTested(selectState.id) == 0) {
            action = ACTION_DEFAULT;
        } else {
            List<Integer> grossActions=envParams.discreteActionsSpace;
            List<Integer> testedActions= vsb.testedActions(selectState.id);
            List<Integer> nonTestedActions = MathUtils.getDifferenceBetweenLists(grossActions, testedActions);
            if (nonTestedActions.isEmpty()) {
                action = ACTION_DEFAULT;
                logger.warning("nonTestedActions is empty");
            } else {
                action = chooseRandomAction(nonTestedActions);
            }
        }
        return action;
    }

    public StateForSearch selectState() {
        StateForSearch selectedState;
        selectedState = vsb.selectRandomState();

        for (int j = 0; j < MAX_NOF_SELECTION_TRIES; j++) {
            if ( MathUtils.calcRandomFromIntervall(0,1)<PROB_SELECT_STATE_FROM_NEW_DEPTH_STEP && vsbForNewDepthSet.size()>0) {
                selectedState = vsbForNewDepthSet.selectRandomState();
            } else
            {
                selectedState = vsb.selectRandomState();
            }

            if (!isNullOrTerminalStateOrAllActionsTestedOrIsAtSearchDepth(selectedState)) {
                break;
            }
        }

      return selectedState;
    }

    public boolean isNullOrTerminalStateOrAllActionsTestedOrIsAtSearchDepth(StateForSearch state) {

        //fail fast => speeding up
        if (state == null) {
            return true;
        }

        if (state.depth== searchDepth) {
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
