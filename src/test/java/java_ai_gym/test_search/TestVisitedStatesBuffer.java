package java_ai_gym.test_search;

import java_ai_gym.models_common.StepReturn;
import java_ai_gym.models_pong.VisitedStatesBuffer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestVisitedStatesBuffer extends TestSearchBase {

    VisitedStatesBuffer vsb;

    @Before
    public void setup() {
        super.setupMoves();
        p.MAX_SPEED_RACKET=.01;
        env.setRandomStateValuesStart(state);
        vsb = new VisitedStatesBuffer();
        state.setId(vsb.START_STATE_ID);
    }

    @Test
    public void EmptyBuffer() {
        vsb.clear();
        System.out.println(vsb.selectRandomStateId());
        Assert.assertEquals("",vsb.selectRandomStateId());
    }

    @Test
    public void setStartState() {
        vsb.addState(vsb.START_STATE_ID,state);
        System.out.println(vsb.selectRandomStateId());
        Assert.assertEquals(vsb.START_STATE_ID,vsb.selectRandomStateId());
    }

    @Test
    public void setStartStateWithOneExperience() {
        vsb.addState(vsb.START_STATE_ID,state);
        StepReturn stepReturn=env.step(moves.get("left"),state);
        String newId=state.getId()+"."+moves.get("left");
        VisitedStatesBuffer.StateExperience se=new VisitedStatesBuffer.StateExperience(moves.get("left"),stepReturn.reward,stepReturn.termState,newId);
        vsb.addExperience(state.getId(),se);

        System.out.println(vsb.selectRandomStateId());
        System.out.println(vsb.getExperienceList(state.getId()));
        Assert.assertEquals(vsb.START_STATE_ID,vsb.selectRandomStateId());
    }

    @Test
    public void addExperienceToStartState() {

        int action=moves.get("left");
        StepReturn stepReturn=env.step(action,state);
        vsb = new VisitedStatesBuffer(state);
        vsb.addNewStateAndExperienceFromStep(state.getId(),action,stepReturn);
        System.out.println(vsb);
        Assert.assertEquals(2,vsb.nofStates());
    }



}
