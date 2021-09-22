package java_ai_gym.models_common;


import java_ai_gym.models_mountaincar.MountainCar;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

/***
 * Neural network based agent. Sub classes do for example define the network.
 * Following parameters are especially critical: MINI_BATCH_SIZE, NOF_NEURONS_HIDDEN, LEARNING_RATE, RB_ALP
 *  Network output y for state s defined as
 *  y=    q(s)+alpha*( r – q(s) )   (term state)
 *        q(s)+alpha*( r+gama*maxQ(s’)-q(s))   (non term state)
 *
 */

public abstract class AgentNeuralNetwork implements Learnable {

    private final static Logger logger = Logger.getLogger(AgentNeuralNetwork.class.getName());

    public State state;
    public ReplayBuffer replayBuffer;
    public MultiLayerNetwork network;   //neural network memory
    public MultiLayerNetwork networkTarget;   //neural network memory
    private final Random random = new Random();
    protected ScalerExponential learningRateScaler;
    public  ScalerExponential probRandActionScaler;

    public int nofFits=0;
    double bellmanErrorStep;
    public List<Double> bellmanErrorListItemPerEpisode =new ArrayList<>();
    public List<Double> bellmanErrorListItemPerStep =new ArrayList<>();

    protected  int SEED = 12345;  //Random number generator seed, for reproducibility

    protected  Integer NOF_OUTPUTS ;
    protected  Integer NOF_FEATURES ;
    protected  Integer  NOF_NEURONS_HIDDEN;

    public  int REPLAY_BUFFER_SIZE = 1000;
    public  int MINI_BATCH_SIZE = 30;
    public   double RB_EPS=0.1;
    public   double RB_ALP=0.5;  //0 <=> uniform distribution from bellman error for mini batch selection
    public   double BETA0=0.1;
    public  double  BE_ERROR_INIT=0;  // 0 <=> do not favor new comers
    public  double  BE_ERROR_MAX=10; //used for clipping

    public  double L2_REGULATION=1e-5;
    public  double LEARNING_RATE_START =1e-2;
    public  double LEARNING_RATE_END =1e-2;
    public  double MOMENTUM=0.8;
    public  double GRADIENT_NORMALIZATION_THRESHOLD=1;

    public double GAMMA = 1.0;  // gamma discount factor
    protected  double ALPHA=1.0;  // learning rate in Q-learning update
    protected  double PROBABILITY_RANDOM_ACTION_START = 0.9;  //probability choosing random action
    protected  double PROBABILITY_RANDOM_ACTION_END = 0.1;
    public  int NUM_OF_EPISODES = 200; // number of iterations
    public final int NUM_OF_EPOCHS = 1; //nof fits per mini batch
    public int NOF_FITS_BETWEEN_TARGET_NETWORK_UPDATE =20;
    public  int NOF_STEPS_BETWEEN_FITS=10;

    public abstract  MultiLayerNetwork createNetwork();
    public abstract  INDArray setNetworkInput(State state);
    public abstract void  createLearningRateScaler();
    public abstract void  createProbRandActionScaler();

    @Override
    public State getState() {
        return state;
    }

    @Override
    public int chooseBestAction(State state) {
        INDArray outFromNetwork = calcOutFromNetwork(state, network);
        return outFromNetwork.argMax().getInt();
    }

    @Override
    public double findMaxQ(State state) {
        INDArray inputNetwork = setNetworkInput(state);
        INDArray outFromNetwork= calcOutFromNetwork(inputNetwork, network);
        return outFromNetwork.max().getDouble();
    }

    @Override
    public int chooseRandomAction(List<Integer> actions) {
        return actions.get(random.nextInt(actions.size()));
    }

    @Override
    public int chooseAction(double fractionEpisodesFinished,List<Integer> actions) {
        return (Math.random() < calcProbRandAction(fractionEpisodesFinished)) ?
                chooseRandomAction(actions) :
                chooseBestAction(state);
    }

    @Override
    public void writeMemory(State oldState, Integer Action, Double value) {
        //not valid for NN
    }

    @Override
    public double readMemory(State state, int action) {
        INDArray inputNetwork = setNetworkInput(state);
        return readMemory(inputNetwork,  action);
    }


    public double readMemory(INDArray inputNetwork, int action) {
        INDArray outFromNetwork= calcOutFromNetwork(inputNetwork, network);
        return outFromNetwork.getDouble(action);
    }

    public double findMaxQTargetNetwork(State state) {
        INDArray inputNetwork = setNetworkInput(state);
        INDArray outFromNetwork= calcOutFromNetwork(inputNetwork, networkTarget);
        return outFromNetwork.max().getDouble();
    }

    public INDArray calcOutFromNetwork(State state,MultiLayerNetwork network) {
        INDArray inputNetwork = setNetworkInput(state);
        return network.output(inputNetwork, false);
    }

    public INDArray calcOutFromNetwork(INDArray inputNetwork,MultiLayerNetwork network) {
        return network.output(inputNetwork, false);
    }

    public void fitFromMiniBatch(List<Experience> miniBatch, double fEpisodes ) {
        network.setLearningRate(calcLearningRate(fEpisodes));
        if (miniBatch.size()== MINI_BATCH_SIZE) {
            DataSetIterator iterator = createTrainingData(miniBatch);
            network.fit(iterator,NUM_OF_EPOCHS);
            nofFits++;
        }
        else
            logger.warning("miniBatch.size() < agent.MINI_BATCH_SIZE");
    }

    public DataSetIterator createTrainingData(List<Experience> miniBatch) {

        INDArray inputNDSet = Nd4j.zeros(MINI_BATCH_SIZE,NOF_FEATURES);
        INDArray  outPutNDSet = Nd4j.zeros(MINI_BATCH_SIZE,NOF_OUTPUTS);

        if (miniBatch.size() > MINI_BATCH_SIZE)
            logger.warning("To big mini batch");

        double sumBellmanError=0;
        for (int idxSample= 0; idxSample < miniBatch.size(); idxSample++) {
            Experience exp=miniBatch.get(idxSample);
            INDArray inputNetwork = setNetworkInput(exp.s);
            INDArray outFromNetwork= calcOutFromNetwork(inputNetwork, network);
            modifyNetworkOut(exp, inputNetwork, outFromNetwork);
            changeBellmanErrorVariableInBufferItem(exp);
            addTrainingExample(inputNDSet, outPutNDSet, idxSample, inputNetwork, outFromNetwork);
            sumBellmanError=sumBellmanError+Math.abs(bellmanErrorStep);
        }

        bellmanErrorListItemPerStep.add(sumBellmanError/Math.max(miniBatch.size(),1));
        DataSet dataSet = new DataSet(inputNDSet, outPutNDSet);
        List<DataSet> listDs = dataSet.asList();

        return new ListDataSetIterator<>(listDs);
    }

    public void addBellmanErrorItemForEpisodeAndClearPerStepList() {
        int nofItems=bellmanErrorListItemPerStep.size();
        if (nofItems>0) {
            double beEpis = bellmanErrorListItemPerStep.stream().mapToDouble(f -> f).sum();
            bellmanErrorListItemPerEpisode.add(beEpis/ nofItems);
        }
        bellmanErrorListItemPerStep.clear();
    }

    public boolean isItTimeToFit() {
        return  state.totalNofSteps % NOF_STEPS_BETWEEN_FITS == 0;
    }

    public boolean isItTimeToUpdateTargetNetwork() {
        return  (nofFits % NOF_FITS_BETWEEN_TARGET_NETWORK_UPDATE == 0);
    }

    //To enable prioritized experience replay items in full experience buffer are modified
    //possible because mini batch item exp is reference to item in buffer
    private void changeBellmanErrorVariableInBufferItem(Experience exp) {
        exp.pExpRep.beError=Math.abs(bellmanErrorStep);
    }

    public void updateTargetNetwork() {
        networkTarget.setParams(network.params());
    }

    public double calcLearningRate(double fractionEpisodesFinished)  {
        return learningRateScaler.calcOutDouble(fractionEpisodesFinished);
    }

    public  double calcProbRandAction(double fractionEpisodesFinished) {
        return probRandActionScaler.calcOutDouble(fractionEpisodesFinished);
    }

    public double calcFractionEpisodes(int iEpisode) {
        return (double) iEpisode / (double) NUM_OF_EPISODES;
    }

    public void savePolicy(String filePath) throws IOException {
        File polePolicy = new File(filePath+"polePolicy.nw");
        File polePolicyTarget = new File(filePath+"polePolicyTarget.nw");
        network.save(polePolicy);
        networkTarget.save(polePolicyTarget);
    }

    public void loadPolicy(String filePath) throws IOException, InterruptedException {
        File polePolicy = new File(filePath+"polePolicy.nw");
        File polePolicyTarget = new File(filePath+"polePolicyTarget.nw");
        network = ModelSerializer.restoreMultiLayerNetwork(polePolicy);
        networkTarget = ModelSerializer.restoreMultiLayerNetwork(polePolicyTarget);
    }

    public double getBellmanErrorAverage(int nofSteps) {
        if (bellmanErrorListItemPerEpisode.size()==0)
            return 1;
        DoubleSummaryStatistics beStats = bellmanErrorListItemPerEpisode.stream().mapToDouble(a -> a).summaryStatistics();
        return beStats.getAverage();
    }

    //---- private methods

    private void addTrainingExample(INDArray inputNDSet, INDArray outPutNDSet, int idxSample, INDArray inputNetwork, INDArray outFromNetwork) {
        inputNDSet.putRow(idxSample, inputNetwork);
        outPutNDSet.putRow(idxSample, outFromNetwork);
    }

    private void modifyNetworkOut(Experience exp, INDArray inputNetwork, INDArray outFromNetwork) {
        double qOld = readMemory(inputNetwork, exp.action);
        bellmanErrorStep=calcBellmanErrorStep(exp.stepReturn, qOld);
        bellmanErrorStep=MathUtils.clip(bellmanErrorStep,-BE_ERROR_MAX,BE_ERROR_MAX);
        double alpha=exp.pExpRep.w*ALPHA;
        double y=qOld*1 + alpha * bellmanErrorStep;
        outFromNetwork.putScalar(0, exp.action,y);
    }

    protected double calcBellmanErrorStep(StepReturn stepReturn, double qOld) {
        return stepReturn.termState ?
                stepReturn.reward - qOld :
                stepReturn.reward + GAMMA * findMaxQTargetNetwork(stepReturn.state) - qOld;
    }

    protected  boolean isAnyNetworkSizeFieldNull() {
        return (NOF_OUTPUTS==null | NOF_FEATURES==null | NOF_NEURONS_HIDDEN==null);
    }

    protected  boolean isAnyFieldNull() {
        return (state==null | replayBuffer==null | network==null | networkTarget==null);
    }

    protected void showConstructorLogMessage() {
        if (isAnyFieldNull())
            logger.warning("Some field in AgentNeuralNetwork is not set, i.e. null");
        else
            logger.info("Neural network based agent created. ");
    }




}