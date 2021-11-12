package java_ai_gym.models_common;

import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class EnvironmentNetworkAgent extends Environment {


    public PolicyTestReturn testPolicy(AgentNeuralNetwork agent,EnvironmentParametersAbstract parameters, int nofTest) {

        List<Integer> nofStepsList = new ArrayList<>();
        List<Double> maxQaverageList = new ArrayList<>();
        List<Double> bellmanErrList = new ArrayList<>();
        int nofSuccessTests = 0;
        for (int i = 0; i < nofTest; i++) {
            setRandomStateValuesStart(agent.state);
            RunPolicyReturn runPolicyReturn=runPolicy(agent, parameters);
            int nofSteps = agent.state.getDiscreteVariable("nofSteps");
            nofStepsList.add(nofSteps);
            maxQaverageList.add(runPolicyReturn.avgMaxQ);
            bellmanErrList.add(runPolicyReturn.avgBellmannErr);
            if (isPolicyTestSuccessful(agent.state))
                nofSuccessTests++;
        }

        PolicyTestReturn policyTestReturn=new PolicyTestReturn();
        IntSummaryStatistics statsNofSteps = nofStepsList.stream().mapToInt(a -> a).summaryStatistics();
        DoubleSummaryStatistics statsMaxQ = maxQaverageList.stream().mapToDouble(a -> a).summaryStatistics();
        DoubleSummaryStatistics statsBellmanErr = bellmanErrList.stream().mapToDouble(a -> a).summaryStatistics();
        policyTestReturn.minNofSteps=statsNofSteps.getMin();
        policyTestReturn.avgNofSteps=statsNofSteps.getAverage();
        policyTestReturn.maxNofSteps=statsNofSteps.getMax();
        policyTestReturn.successRatio=nofSuccessTests / (double) nofTest;
        policyTestReturn.maxQaverage=statsMaxQ.getAverage();
        policyTestReturn.bellmanErrAverage=statsBellmanErr.getAverage();

        return policyTestReturn;
    }

    protected RunPolicyReturn runPolicy(AgentNeuralNetwork agent, EnvironmentParametersAbstract parameters) {
        StepReturn stepReturn;
        List<Double> maxQList = new ArrayList<>();
        List<Double> bellmanErrList = new ArrayList<>();

        int tempTotalNofSteps=agent.state.totalNofSteps;  //evaluation shall not affect totalNofSteps
        do {
            stepReturn=step(agent.chooseBestAction(agent.state),agent.state);
            agent.state.copyState(stepReturn.state);

            INDArray inputNetwork = agent.setNetworkInput(agent.state);
            double qOld = agent.readMemory(inputNetwork, agent.chooseBestAction(agent.state));
            double bellmanErrorStep=agent.calcBellmanErrorStep(stepReturn, qOld);

            maxQList.add(agent.findMaxQTargetNetwork(agent.state));
            bellmanErrList.add(Math.abs(bellmanErrorStep));
        } while (!isTerminalStatePolicyTest(agent.state));
        agent.state.totalNofSteps=tempTotalNofSteps;

        RunPolicyReturn runPolicyReturn=new RunPolicyReturn();
        DoubleSummaryStatistics statsMaxQ = maxQList.stream().mapToDouble(a -> a).summaryStatistics();
        runPolicyReturn.avgMaxQ=statsMaxQ.getAverage();
        DoubleSummaryStatistics statsBellmanErr = bellmanErrList.stream().mapToDouble(a -> a).summaryStatistics();
        runPolicyReturn.avgBellmannErr=statsBellmanErr.getAverage();
        return runPolicyReturn;
    }


    public void printPolicyTest(int iEpisode, AgentNeuralNetwork agent, PolicyTestReturn policyTestReturn, int MAX_NOF_STEPS_POLICY_TEST) {

        System.out.printf("iEpisode: %d ",iEpisode);
        System.out.printf(", success ratio: %.2f", policyTestReturn.successRatio);
        System.out.printf(", avg of maxQ: %.2f ", policyTestReturn.maxQaverage);
        System.out.printf(", avg bellman err: %.2f", policyTestReturn.bellmanErrAverage);

        System.out.printf(",  [min, avg, max] NofSteps: [%.0f, %.0f, %.0f]",
                policyTestReturn.minNofSteps,
                policyTestReturn.avgNofSteps,
                policyTestReturn.maxNofSteps);
        System.out.printf(", learning rate: %.5f", agent.calcLearningRate(agent.calcFractionEpisodes(iEpisode)));
        System.out.printf(", prob random action: %.5f", agent.calcProbRandAction(agent.calcFractionEpisodes(iEpisode)));
        System.out.println();

    }

    public void simulateEpisode(AgentNeuralNetwork agent, int iEpisode,Environment env,EnvironmentParametersAbstract envParams) {
        // Q learning equation: Q[s][a] = Q[s][a] + alpha ( R[s][a] + gamma (max Q[sNew]) - Q[s][a] )
        // a single episode: the agent finds a path from state s to the exit state

        StepReturn stepReturn;
        State sNew = new State();
        double fEpisodes=agent.calcFractionEpisodes(iEpisode);
        do {
            int aChosen=agent.chooseAction(fEpisodes,envParams.discreteActionsSpace);
            stepReturn = env.step(aChosen, agent.state);
            Experience experience = new Experience(new State(agent.state), aChosen, stepReturn, agent.BE_ERROR_INIT);
            agent.replayBuffer.addExperience(experience);

            if (agent.replayBuffer.isFull(agent) & agent.isItTimeToFit() ) {
                List<Experience> miniBatch =
                        agent.replayBuffer.getMiniBatchPrioritizedExperienceReplay(
                                agent.MINI_BATCH_SIZE,
                                agent.calcFractionEpisodes(iEpisode));

                agent.fitFromMiniBatch(miniBatch,fEpisodes);

                if (agent.isItTimeToUpdateTargetNetwork())
                    agent.updateTargetNetwork();

            }

            sNew.copyState(stepReturn.state);
            agent.state.copyState(sNew);
            env.render(agent.state,agent.findMaxQTargetNetwork(agent.state),aChosen);

        } while (!stepReturn.termState);

        agent.addBellmanErrorItemForEpisodeAndClearPerStepList();
    }

    public void animatePolicy(AgentNeuralNetwork agent) throws InterruptedException {
        setRandomStateValuesStart(agent.state);
        System.out.println(agent.state);
        StepReturn stepReturn;
        do {
            int aBest=agent.chooseBestAction(agent.state);
            stepReturn=step(aBest,agent.state);
            agent.state.copyState(stepReturn.state);
            render(agent.state,agent.findMaxQTargetNetwork(agent.state),aBest);
            TimeUnit.MILLISECONDS.sleep(gfxSettings.TIME_MILLIS_FRAME);

        } while (!stepReturn.termState);

        TimeUnit.MILLISECONDS.sleep(1000);
    }

}
