package java_ai_gym.test_search;

import java_ai_gym.models_common.AgentSearch;
import java_ai_gym.models_common.StateForSearch;
import java_ai_gym.models_common.StepReturn;
import java_ai_gym.models_pong.PongAgentDPSearch;
import java_ai_gym.models_pong.VisitedStatesBuffer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class TestVisitedStatesBuffer extends TestSearchBase {

    VisitedStatesBuffer vsb;
    StepReturn stepReturn;
    AgentSearch agent;

    @Before
    public void setup() {
        super.setupMoves();
        p.MAX_SPEED_RACKET=.01;
        env.setRandomStateValuesStart(state);
        state.setIdDepthNofActions(state.START_STATE_ID, 0, 0);
        vsb = new VisitedStatesBuffer();
        stepReturn = new StepReturn(state);
        agent= new PongAgentDPSearch(env,100,5);

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
        VisitedStatesBuffer.StateExperience se=new VisitedStatesBuffer.StateExperience(moves.get("left"),stepReturn.reward,stepReturn.termState,newId);
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
        Assert.assertEquals(2,vsb.nofStates());
    }

    @Test
    public void addFromTrial() {
        int depth=0;  int nofActions=0;
        state.setIdDepthNofActions(state.START_STATE_ID, depth, nofActions);
        System.out.println("state.id ="+state.id);
        vsb = new VisitedStatesBuffer(state);

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

        System.out.println(vsb);

        Assert.assertEquals(max_depth+1,vsb.nofStates());
    }

}
