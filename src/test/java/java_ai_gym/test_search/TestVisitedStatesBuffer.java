package java_ai_gym.test_search;

import java_ai_gym.models_common.StateForSearch;
import java_ai_gym.models_common.StepReturn;
import java_ai_gym.models_agent_search.StateExperience;
import java_ai_gym.models_agent_search.VisitedStatesBuffer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestVisitedStatesBuffer extends TestSearchBase {

    VisitedStatesBuffer vsb;
    StepReturn stepReturn;


    @Before
    public void setup() {
        super.setupMoves();
        env.setRandomStateValuesStart(state);
        state.setIdDepthNofActions(state.START_STATE_ID, 0, 0);
        vsb = new VisitedStatesBuffer();
        stepReturn = new StepReturn(state);

    }

    @Test
    public void EmptyBuffer() {
        vsb.clear();
        System.out.println(vsb.selectRandomStateId());
        Assert.assertEquals(vsb.ID_STATE_EMPTY_BUFFER,vsb.selectRandomStateId());
    }

    @Test
    public void setStartState() {
        vsb.addState(state.START_STATE_ID,state);
        System.out.println(vsb.selectRandomStateId());
        System.out.println(state);
        printAndAssertStateVisitsDaAOInstanceVariableSizes();
        Assert.assertEquals(state.START_STATE_ID,vsb.selectRandomStateId());
    }



    @Test
    public void addNewStateAndExperienceFromStep() {
        createVsbWithTwoStates();

        System.out.println(vsb);
        System.out.println("stateVisitsDAO="+vsb.getStateVisitsDAO());
        Assert.assertEquals(2,vsb.nofStates());
        printAndAssertStateVisitsDaAOInstanceVariableSizes();
    }


    @Test
    public void addFromTrial() {
        defineInitVSB(new StateForSearch(state));
        int maxDepth = doTrial(state);
        System.out.println(vsb);
        Assert.assertEquals(maxDepth+1,vsb.nofStates());
        printAndAssertStateVisitsDaAOInstanceVariableSizes();
    }


    @Test
    public void getAllStatesAtDepth() {
        defineInitVSB(new StateForSearch(state));
        doTrial(state);
        doTrial(state);

        List<StateForSearch> statesAtDepth1 = vsb.getAllStatesAtDepth(3);
        List<StateForSearch> statesAtDepth2 = getAllStatesAtDepthSlow(3);

        System.out.println(vsb);
        System.out.println("statesAtDepth1 = " + statesAtDepth1);
        System.out.println("statesAtDepth2 = " + statesAtDepth2);

        Assert.assertEquals(statesAtDepth1.size(),statesAtDepth2.size());
    }

    @Test
    public void removeFromVsbWithTwoStates() {
        createVsbWithTwoStates();

        System.out.println(vsb);
        vsb.remove("start.0");

        System.out.println(vsb);

        System.out.println("stateVisitsDAO="+vsb.getStateVisitsDAO());
        Assert.assertEquals(1,vsb.nofStates());
        printAndAssertStateVisitsDaAOInstanceVariableSizes();
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


    private void createVsbWithTwoStates() {
        int depth=0;
        int nofActions=0;
        state.setDepthNofActions( depth, nofActions);
        vsb = new VisitedStatesBuffer(state);
        int action=moves.get("left");
        stepReturn=env.step(moves.get("left"),state);
        vsb.addNewStateAndExperienceFromStep(state.id,action,stepReturn);
    }

    private void printAndAssertStateVisitsDaAOInstanceVariableSizes() {
        int stateBufferSize=vsb.getStateVisitsDAO().getStateBuffer().size();
        int idListSize=vsb.getStateVisitsDAO().getIdList().size();
        int idListAtDepthSize=0; //vsb.getStateVisitsDAO().getIdListAtDepth().size();

        for(int depth: vsb.getStateVisitsDAO().getIdListAtDepth().keySet()) {
            List<String> ids=vsb.getStateVisitsDAO().getIdListAtDepth().get(depth);
            idListAtDepthSize=idListAtDepthSize+ids.size();
        }

        System.out.println("stateBufferSize = " + stateBufferSize +", idListSize = " + idListSize +", idListAtDepthSize = " + idListAtDepthSize);

        Assert.assertEquals(stateBufferSize, idListSize);
        Assert.assertEquals(stateBufferSize, idListAtDepthSize);
    }

    public List<StateForSearch> getAllStatesAtDepthSlow(int depth) {
        List<StateForSearch> statesAtDepth = new ArrayList<>();
        for (StateForSearch state : vsb.getStateVisitsDAO().getAll()) {
            if (state.depth == depth) {
                statesAtDepth.add(state);
            }
        }
        return statesAtDepth;
    }




}
