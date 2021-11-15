package java_ai_gym.test_search;

import java_ai_gym.models_common.State;
import java_ai_gym.models_pong.PongAgentRandomSearch;
import java_ai_gym.models_pong.SinglePong;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TestPongAgentRandomSearch {

    SinglePong env=new SinglePong();
    State state = new State(env.getTemplateState());


    @Before
    public void setup() {

        env.setRandomStateValuesStart(state);

    }

    @SneakyThrows
    @Test
    //@Ignore("Takes time")
    public void testSetup() {
        env.render(state,0.0,0);
        //TimeUnit.MILLISECONDS.sleep(1000);


        PongAgentRandomSearch agent=new PongAgentRandomSearch(50,10,env,env.parameters);

        agent.search(state);



    }



}
