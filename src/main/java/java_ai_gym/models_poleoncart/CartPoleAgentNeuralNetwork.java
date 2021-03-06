package java_ai_gym.models_poleoncart;

import java_ai_gym.models_common.*;
import java_ai_gym.models_mountaincar.MountainCar;
import java_ai_gym.models_mountaincar.MountainCarAgentNeuralNetwork;
import java_ai_gym.models_poleoncart.CartPole;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.logging.Logger;

/***
 * Hyper parameters settings influenced by
 * https://arxiv.org/pdf/2006.04938.pdf
 *
 */

public class CartPoleAgentNeuralNetwork extends AgentNeuralNetwork {

    private static final Logger logger = Logger.getLogger(MountainCarAgentNeuralNetwork.class.getName());

    public final CartPole.EnvironmentParameters envParams;  //reference to environment parameters

    ScalerLinear xScaler;
    ScalerLinear xDotScaler;
    ScalerLinear thetaScaler;
    ScalerLinear thetaDotScaler;
    final double  PRECISION=1;

    public CartPoleAgentNeuralNetwork(CartPole.EnvironmentParameters envParams, State templateState) {
        this.envParams = envParams;
        state = new StateBasic(templateState);
        createReplayBuffer();
        createInputNormalizers(envParams);
        createNetworks(envParams);
        defineLearningParameters();
        createLearningRateScaler();
        createProbRandActionScaler();
        showConstructorLogMessage();
    }

    private void createInputNormalizers(CartPole.EnvironmentParameters envParams) {
        xScaler=new ScalerLinear(-envParams.X_TRESHOLD, envParams.X_TRESHOLD,-1,1);
        xDotScaler=new ScalerLinear(-envParams.X_DOT_TRESHOLD, envParams.X_DOT_TRESHOLD,-1,1);
        thetaScaler=new ScalerLinear(-envParams.THETA_THRESHOLD_RADIANS, envParams.THETA_THRESHOLD_RADIANS,-1,1);
        thetaDotScaler=new ScalerLinear(-envParams.THETA_DOT_THRESHOLD_RADIANS, envParams.THETA_DOT_THRESHOLD_RADIANS,-1,1);
    }

    private void createReplayBuffer() {
        this.REPLAY_BUFFER_SIZE = 2000;
        this.MINI_BATCH_SIZE = (int) (24*1);
        this.RB_EPS = 0.1;
        this.RB_ALP = 0.3;  //0 <=> uniform distribution from bellman error for mini batch selection
        this.BETA0 = 0.1;
        this.BE_ERROR_INIT=0;  // 0 <=> do not favor new comers
        this.BE_ERROR_MAX=1.0;
        replayBuffer = new ReplayBuffer(RB_EPS, RB_ALP, BETA0, REPLAY_BUFFER_SIZE);
    }

    private void defineLearningParameters() {
        this.GAMMA = 0.9;  // gamma discount factor
        this.ALPHA = 1.0;  // Q-learning factor
        this.PROBABILITY_RANDOM_ACTION_START = 0.99;
        this.PROBABILITY_RANDOM_ACTION_END = 0.1;
        this.NOF_STEPS_BETWEEN_FITS = 1;
        this.NUM_OF_EPISODES = (int) (500*NOF_STEPS_BETWEEN_FITS*PRECISION);
        this.NOF_FITS_BETWEEN_TARGET_NETWORK_UPDATE = (int) (100*Math.sqrt(PRECISION));
    }

    private void createNetworks(CartPole.EnvironmentParameters envParams) {
        this.NOF_OUTPUTS = envParams.NOF_ACTIONS;
        this.NOF_FEATURES = state.nofContinuousVariables();

        System.out.println("this.NOF_OUTPUTS:"+this.NOF_OUTPUTS+", NOF_FEATURES:"+this.NOF_FEATURES);

        this.NOF_NEURONS_HIDDEN = 24 * 1;
        this.L2_REGULATION = 1e-8;
        this.LEARNING_RATE_START =1e-2/Math.pow(PRECISION,2);
        this.LEARNING_RATE_END =1e-4/Math.pow(PRECISION,2);
        this.MOMENTUM = 0.8;
        this.GRADIENT_NORMALIZATION_THRESHOLD=1;

        if (isAnyNetworkSizeFieldNull())
            logger.warning("Some network size field is not set, i.e. null");
        network = createNetwork();
        networkTarget = createNetwork();
    }

    @Override
    public MultiLayerNetwork createNetwork() {
        logger.info("Creating network, initital learning rate:"+this.LEARNING_RATE_START+
                ", L2_REGULATION:"+L2_REGULATION+", NOF_NEURONS_HIDDEN:"+NOF_NEURONS_HIDDEN);

        //return createTanhNetwork();
            return createReluNetwork();
    }


    public MultiLayerNetwork createTanhNetwork() {

        MultiLayerConfiguration configuration = new NeuralNetConfiguration.Builder()
                .seed(SEED)
                .weightInit(WeightInit.XAVIER)
                .l2(L2_REGULATION)
                .gradientNormalization(GradientNormalization.RenormalizeL2PerLayer)
                .updater(new Nesterovs(LEARNING_RATE_START, MOMENTUM))
                .list()
                .layer(0, new DenseLayer.Builder().nIn(NOF_FEATURES).nOut(NOF_NEURONS_HIDDEN)
                        .activation(Activation.TANH)
                        .build())
                .layer(1, new DenseLayer.Builder().nIn(NOF_NEURONS_HIDDEN).nOut(NOF_NEURONS_HIDDEN)
                        .activation(Activation.TANH)
                        .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY)
                        .nIn(NOF_NEURONS_HIDDEN).nOut(NOF_OUTPUTS).build())
                .backpropType(BackpropType.Standard)
                .build();
        MultiLayerNetwork network = new MultiLayerNetwork(configuration);
        network.init();

        return network;
    }


    public MultiLayerNetwork createReluNetwork() {

        MultiLayerConfiguration configuration = new NeuralNetConfiguration.Builder()
                .seed(SEED)
                .weightInit(WeightInit.XAVIER)
                .l2(L2_REGULATION)
                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
                .gradientNormalizationThreshold(GRADIENT_NORMALIZATION_THRESHOLD)
                .updater(new Nesterovs(LEARNING_RATE_START, MOMENTUM))
                .list()
                .layer(0, new DenseLayer.Builder().nIn(NOF_FEATURES).nOut(NOF_NEURONS_HIDDEN)
                        .activation(Activation.LEAKYRELU)
                        .build())
                .layer(1, new DenseLayer.Builder().nIn(NOF_NEURONS_HIDDEN).nOut(NOF_NEURONS_HIDDEN)
                        .activation(Activation.LEAKYRELU)
                        .build())
                //.layer(2, new DenseLayer.Builder().nIn(NOF_NEURONS_HIDDEN).nOut(NOF_NEURONS_HIDDEN)
                 //       .activation(Activation.LEAKYRELU)
                 //       .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY)
                        .nIn(NOF_NEURONS_HIDDEN).nOut(NOF_OUTPUTS).build())
                .backpropType(BackpropType.Standard)
                .build();
        MultiLayerNetwork network = new MultiLayerNetwork(configuration);
        network.init();
        return network;
    }



    @Override
    public INDArray setNetworkInput(State state) {
        double[] varValuesAsArray = {
                xScaler.calcOutDouble(state.getContinuousVariable("x")),
                xDotScaler.calcOutDouble(state.getContinuousVariable("xDot")),
                thetaScaler.calcOutDouble(state.getContinuousVariable("theta")),
                thetaDotScaler.calcOutDouble(state.getContinuousVariable("thetaDot"))
        };

        if (varValuesAsArray.length != NOF_FEATURES)
            logger.warning("Wrong number of network inputs");

        return Nd4j.create(varValuesAsArray, 1, NOF_FEATURES);
    }


    public void printQsa() {
        for (int aChosen : envParams.discreteActionsSpace)
            System.out.print("aChosen: " + aChosen + ", Q:" + readMemory(state, aChosen) + "; ");
        System.out.println();
    }

    @Override
    public void  createLearningRateScaler() {
        //learningRateScaler=new ScalerLinear(0,1,LEARNING_RATE_START,LEARNING_RATE_END);
        learningRateScaler=new ScalerExponential(0,1,LEARNING_RATE_START,LEARNING_RATE_END);
    }

    @Override
    public void  createProbRandActionScaler() {
        probRandActionScaler=new ScalerExponential(0,1,PROBABILITY_RANDOM_ACTION_START,PROBABILITY_RANDOM_ACTION_END);
    }

}


