package java_ai_gym.models_common;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/***
 * Generic state class, can include discrete and/or continuous variables.
 * The variable naming is flexible thanks to hash maps.
 */

public class State {

    private final static Logger logger = Logger.getLogger(State.class.getName());

    public int totalNofSteps=0;
    Map<String, Integer> discreteVariables = new HashMap<>();
    Map<String, Double> continuousVariables = new HashMap<>();

    public State() {
    }

    public State(State state) {
        copyState(state);
    }

    public void copyState(State state) {
        this.discreteVariables.putAll(state.discreteVariables);
        this.continuousVariables.putAll(state.continuousVariables);
    }

    public void createDiscreteVariable(String name, Integer value) {
        discreteVariables.put(name, value);
    }

    public void setVariable(String name, Integer value) {

        if (!discreteVariables.containsKey(name))
            logger.warning("Error when setting state variable, following variable does not exist:" + name);
        else
            discreteVariables.replace(name, value);
    }

    public Integer getDiscreteVariable(String name) {

        if (!discreteVariables.containsKey(name)) {
            logger.warning("Error when getting state variable, following variable does not exist:" + name);
            return null;
        } else
            return discreteVariables.get(name);
    }

    public void createContinuousVariable(String name, Double value) {
        continuousVariables.put(name, value);
    }

    public void setVariable(String name, Double value) {

        if (!continuousVariables.containsKey(name))
            logger.warning("Error setting state variable, it does not exist:" + name);
        else
            continuousVariables.replace(name, value);
    }

    public Double getContinuousVariable(String name) {

        if (!continuousVariables.containsKey(name)) {
            logger.warning("Error getting state variable, it does not exist:" + name);
            return null;
        } else
            return continuousVariables.get(name);
    }

    public int nofDiscreteVariables() {
        return discreteVariables.size();
    }

    public int nofContinuousVariables() {
        return continuousVariables.size();
    }


    @Override
    public String toString() {
        List<String> discreteVariableKeyValuePairs = new ArrayList<>();
        List<String> continuousVariableKeyValuePairs = new ArrayList<>();

        for (String name : discreteVariables.keySet())
            discreteVariableKeyValuePairs.add(name + "=" +
                    new DecimalFormat("#.##").format(discreteVariables.get(name))
            );


        for (String name : continuousVariables.keySet())
            continuousVariableKeyValuePairs.add(name + "=" +
                    new DecimalFormat("#.##").format(continuousVariables.get(name))
            );


        return '{'+String.join(", ", discreteVariableKeyValuePairs)+
                System.getProperty("line.separator")+
                String.join(",", continuousVariableKeyValuePairs)+'}';
    }
}
