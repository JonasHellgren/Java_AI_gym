package java_ai_gym.test_search;

import java_ai_gym.models_common.StateForSearch;
import java_ai_gym.models_common.StepReturn;
import java_ai_gym.models_pong.StateExperience;
import java_ai_gym.models_pong.VisitedStatesBuffer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

public class TestVisitedStatesBuffer extends TestSearchBase {

    VisitedStatesBuffer vsb;
    StepReturn stepReturn;


    @Before
    public void setup() {
        super.setupMoves();
        p.MAX_SPEED_RACKET=.01;
        env.setRandomStateValuesStart(state);
        state.setIdDepthNofActions(state.START_STATE_ID, 0, 0);
        vsb = new VisitedStatesBuffer();
        stepReturn = new StepReturn(state);

    }

    @Test
    public void EmptyBuffer() {
        vsb.clear();
        System.out.println(vsb.selectRandomStateId());
        Assert.assertEquals("",vsb.selectRandomStateId());
    }

    @Test
    public void setStartState() {
        vsb.addState(state.START_STATE_ID,state);
        System.out.println(vsb.selectRandomStateId());
        System.out.println(state);
        Assert.assertEquals(state.START_STATE_ID,vsb.selectRandomStateId());
    }

    @Test
    @Ignore("Maybe remove")
    public void setStartStateWithOneExperience() {
        vsb.addState(state.START_STATE_ID,state);
        stepReturn=env.step(moves.get("left"),state);
        String newId=state.id+"."+moves.get("left");
        StateExperience se=new StateExperience(moves.get("left"),stepReturn.reward,stepReturn.termState,newId);
        vsb.addExperience(state.id,se);

        System.out.println(vsb);
        Assert.assertEquals(state.START_STATE_ID,vsb.selectRandomStateId());
    }

    @Test
    public void addNewStateAndExperienceFromStep() {
        int depth=0;  int nofActions=0;
        state.setDepthNofActions( depth, nofActions);
        vsb = new VisitedStatesBuffer(state);

        int action=moves.get("left");
        stepReturn=env.step(moves.get("left"),state);

        vsb.addNewStateAndExperienceFromStep(state.id,action,stepReturn);
        System.out.println(vsb);
        System.out.println("stateVisitsDAO="+vsb.getStateVisitsDAO());
        Assert.assertEquals(2,vsb.nofStates());
    }

    @Test
    public void addFromTrial() {
        defineInitVSB(new StateForSearch(state));
        int maxDepth = doTrial(state);
        System.out.println(vsb);
        Assert.assertEquals(maxDepth+1,vsb.nofStates());
    }




    @Test
    public void getAllStatesAtDepth() {
        defineInitVSB(new StateForSearch(state));
        doTrial(state);
        doTrial(state);

        List<StateForSearch> statesAtDepth= vsb.getAllStatesAtDepth(3);

        System.out.println(vsb);
        System.out.println(statesAtDepth);

    }

    private int doTrial(StateForSearch startState) {
        int depth=0;
        int nofActions=0;
        StateForSearch state=new StateForSearch(startState);

        int max_depth=5;
        for (int i = 0; i < max_depth; i++) {
            int action=agent.chooseRandomAction(env.parameters.discreteActionsSpace);
            stepReturn=env.step(action,state);
            StateForSearch stateNew= (StateForSearch) stepReturn.state;
            depth++;
            stateNew.setDepthNofActions( depth, nofActions);

            vsb.addNewStateAndExperienceFromStep(state.id,action,stepReturn);
            state.copyState(stateNew);

        }
        return max_depth;
    }

    private void defineInitVSB(StateForSearch startState) {
        state.setIdDepthNofActions(state.START_STATE_ID, 0, NOF_ACTIONS);
        vsb = new VisitedStatesBuffer(state);
    }

}
