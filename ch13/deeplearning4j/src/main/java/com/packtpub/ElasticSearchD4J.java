package com.packtpub;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ElasticSearchD4J {

    private static Logger log = LoggerFactory.getLogger(ElasticSearchD4J.class);

    public static void main(String[] args) throws Exception {
        ElasticsearchClient client = ClientHelper.createClient();
        String indexName = "iris";

        SearchRequest searchRequest = new SearchRequest.Builder().index(indexName).size(1000).build();
        SearchResponse<Iris> response = client.search(searchRequest, Iris.class);
        List<Hit<Iris>> hits = response.hits().hits();

//Convert the iris data into 150x4 matrix
        int row = 150;
        int col = 4;
        double[][] irisMatrix = new double[row][col];
//Now do the same for the label data
        int colLabel = 3;
        double[][] labelMatrix = new double[row][colLabel];

        for (int r = 0; r < row; r++) {
            // we populate features

            Hit<Iris> source = hits.get(r);
            Iris iris = source.source();
            irisMatrix[r][0] = (double) iris.getF1();
            irisMatrix[r][1] = (double) iris.getF2();
            irisMatrix[r][2] = (double) iris.getF3();
            irisMatrix[r][3] = (double) iris.getF4();
            // we populate labels
            int label = (Integer) iris.getLabel();
            labelMatrix[r][0] = 0.0;
            labelMatrix[r][1] = 0.0;
            labelMatrix[r][2] = 0.0;
            if (label == 0) labelMatrix[r][0] = 1.0;
            if (label == 1) labelMatrix[r][1] = 1.0;
            if (label == 2) labelMatrix[r][2] = 1.0;
        }


//Check the array by printing it in the log
//System.out.println(Arrays.deepToString(irisMatrix).replace("], ", "]\n"));

//        System.out.println(Arrays.deepToString(labelMatrix).replace("], ", "]\n"));

//Convert the data matrices into training INDArrays
        INDArray training = Nd4j.create(irisMatrix);
        INDArray labels = Nd4j.create(labelMatrix);

        DataSet allData = new DataSet(training, labels);

        allData.shuffle();
        SplitTestAndTrain testAndTrain = allData.splitTestAndTrain(0.65);  //Use 65% of data for training

        DataSet trainingData = testAndTrain.getTrain();
        DataSet testData = testAndTrain.getTest();

        //We need to normalize our data. We'll use NormalizeStandardize (which gives us mean 0, unit variance):
        DataNormalization normalizer = new NormalizerStandardize();
        normalizer.fit(trainingData);           //Collect the statistics (mean/stdev) from the training data. This does not modify the input data
        normalizer.transform(trainingData);     //Apply normalization to the training data
        normalizer.transform(testData);         //Apply normalization to the test data. This is using statistics calculated from the *training* set


        final int numInputs = 4;
        int outputNum = 3;
        long seed = 6;


        log.info("Build model....");
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .activation(Activation.TANH)
                .weightInit(WeightInit.XAVIER)
                .updater(new Sgd(0.1))
                .l2(1e-4)
                .list()
                .layer(0, new DenseLayer.Builder().nIn(numInputs).nOut(3)
                        .build())
                .layer(1, new DenseLayer.Builder().nIn(3).nOut(3)
                        .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .activation(Activation.SOFTMAX)
                        .nIn(3).nOut(outputNum).build())
                .backprop(true).pretrain(false)
                .build();

        //run the model
        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(100));

        for (int i = 0; i < 1000; i++) {
            model.fit(trainingData);
        }

        //evaluate the model on the test set
        Evaluation eval = new Evaluation(3);
        INDArray output = model.output(testData.getFeatures());
        eval.eval(testData.getLabels(), output);
        log.info(eval.stats());
    }

}


