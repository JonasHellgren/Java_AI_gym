package java_ai_gym.models_pong;

import java_ai_gym.helpers.CpuTimer;
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
    final double PROB_SELECT_STATE_FROM_NEW_DEPTH_STEP=0.5;

    int searchDepthStep;
    int searchDepthPrev;
    int searchDepth;
    List<Integer> evaluatedSearchDepths;
    VisitedStatesBuffer vsb;
    VisitedStatesBuffer trimmedVSB;
    VisitedStatesBuffer vsbForNewDepthSet;
    StateForSearch startState;
    int nofStatesVsbForNewDepthSetPrev;
    CpuTimer timeChecker;


    public PongAgentDPSearch(SinglePong env,
                             long timeBudget,
                             int searchDepthStep) {
        super(timeBudget, env, env.parameters);
        this.searchDepthStep = searchDepthStep;
        this.searchDepth = searchDepthStep;
        this.evaluatedSearchDepths=new ArrayList<>();
        super.cpuTimer=new CpuTimer(timeBudget);
        this.timeChecker=new CpuTimer(0);
        //this.state = new StateForSearch(env.getTemplateState());
    }


    @Override
    public SearchResults search(final StateForSearch startState) {

        initInstanceVariables(startState);
        int nofActions = envParams.discreteActionsSpace.size();
        int nofSteps = 0;
        double explorationFactor = 0;

        while (!cpuTimer.isTimeExceeded() && nofSteps<15000) {  //TODO remove nofSteps
            StateForSearch selectedState = this.selectState();
            takeStepAndSaveExperience(nofActions, selectedState);

            if ((double) vsbForNewDepthSet.size()/(double)nofStatesVsbForNewDepthSetPrev > K)  {
                nofStatesVsbForNewDepthSetPrev = vsbForNewDepthSet.size();
                explorationFactor= vsbForNewDepthSet.calcExplorationFactor(searchDepth);
                showLogs1(nofSteps, explorationFactor);
            }

            if (vsb.getDepthMax()>=searchDepth && explorationFactor>=EF_LIMIT)  {
                increaseSearchDepth();
                vsbForNewDepthSet.clear();
                explorationFactor =0;
                showLogs2(nofSteps);
                timeChecker.reset();
                findBestPath();
                logger.info("findBestPath (millis) = " +timeChecker.getTimeInMillis());
            }
            nofSteps++;
        }

        //findBestPath();  //TODO remove
        logger.info("search finished, vsb size = "+vsb.size());
        showLogs1(nofSteps, explorationFactor);
        showLogs2(nofSteps);
        return null;
    }

    public void setTimeBudgetMillis(long time) {
        this.timeBudget=time;
        cpuTimer.setTimeBudgetMillis(time);
    }

    private void showLogs1(int nofSteps, double explorationFactor) {
     //   System.out.println("calcStatesAtDepth vsb = "+vsb.calcStatesAtDepth(searchDepth));
     //   System.out.println("calcStatesAtDepth vsbForSpecificDepthStep= "+ vsbForNewDepthSet.calcStatesAtDepth(searchDepth));
        logger.info("nofSteps = "+ nofSteps +", explorationFactor = "+ explorationFactor);
    }

    private void showLogs2(int nofSteps) {
        logger.info("searchDept increased to = "+searchDepth+". VSB size = "+vsb.size()+", nofSteps = "+ nofSteps);
        System.out.println("statesAtDepth vsb = "+vsb.calcStatesAtDepth(searchDepth));
        System.out.println("statesAtDepth vsbForSpecificDepthStep= "+ vsbForNewDepthSet.calcStatesAtDepth(searchDepth));
        System.out.println("searchDepthPrev = "+searchDepthPrev);
        System.out.println("evaluatedSearchDepths = "+evaluatedSearchDepths);
        System.out.println("maxDepth trimmed = "+trimmedVSB.getDepthMax());
        System.out.println("maxDepth not trimmed  = "+vsb.getDepthMax());
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
        this.trimmedVSB=new VisitedStatesBuffer(this.startState);
        this.vsbForNewDepthSet =new VisitedStatesBuffer();
        this.nofStatesVsbForNewDepthSetPrev=1;
        this.searchDepth = searchDepthStep;
        this.searchDepthPrev=0;
    }





    private void findBestPath() {
        logger.info("findBestPath called, searchDepthPrev= "+searchDepthPrev+", trimmedVSB.getDepthMax= "+trimmedVSB.getDepthMax());
        VisitedStatesBuffer trimResult =   vsb.createNewVSBWithNoLooseNodesBelowDepth(searchDepthPrev,cpuTimer);
        if (!trimResult.timeExceedWhenTrimming)  {
            trimmedVSB=trimResult;

            logger.info(". VSB trimmed size = "+trimmedVSB.size());

      //      if (trimmedVSB.anyLooseNodeBelowDepth(trimmedVSB, searchDepthPrev)) {
        //        logger.warning("in findBestPath, removeLooseNodesBelowDepth failed, still loose node(s).");
        //    }

        }



    }

    private void increaseSearchDepth() {
        if (vsb.getDepthMax() > searchDepth) {
             logger.warning("vsb.getMaxDepth() > searchDept");
         }
        addEvaluatedSearchDepth(searchDepth);
        searchDepthPrev = searchDepth;
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
