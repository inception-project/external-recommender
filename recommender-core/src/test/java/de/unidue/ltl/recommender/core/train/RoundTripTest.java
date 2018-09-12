/*******************************************************************************
 * Copyright 2018
 * Language Technology Lab
 * University of Duisburg-Essen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.unidue.ltl.recommender.core.train;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dkpro.tc.core.Constants;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import de.unidue.ltl.recommender.core.predict.PredictionWithModel;

public class RoundTripTest
{
    String[] jcasBase64;
    String typesystemBase64;
    String annotationName;
    String annotationFieldName;

    @Rule
    public TemporaryFolder resultFolder = new TemporaryFolder();
    @Rule
    public TemporaryFolder modelLocation = new TemporaryFolder();

    @Before
    public void setup() throws Exception
    {
        modelLocation.create();
        resultFolder.create();
        
    }

    @After
    public void cleanUp() throws IOException
    {
        modelLocation.delete();
        resultFolder.delete();
    }

    @Test
    public void roundTrip() throws Exception
    {
        train();

        predict();
    }

    private void predict() throws Exception
    {
        initPredict();
        PredictionWithModel pwm = new PredictionWithModel(resultFolder.getRoot());
        pwm.run(jcasBase64, typesystemBase64, annotationName, annotationFieldName,
                modelLocation.getRoot());

        List<File> files = getFiles(resultFolder.getRoot());
        assertEquals(1, files.size());

        String content = FileUtils.readFileToString(files.get(0), "utf-8");
        assertTrue(content.startsWith(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xmi:XMI xmlns:xmi=\"http://www.omg.org/XMI\""));
    }

    private List<File> getFiles(File resultFolder)
    {
        File[] f = resultFolder.listFiles(new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                return pathname.getName().endsWith(".txt");
            }
        });

        return new ArrayList<>(Arrays.asList(f));
    }

    private void initPredict() throws IOException
    {
        String json = FileUtils
                .readFileToString(new File("src/test/resources/jsonPredictRequestV2small.txt"), "utf-8");

        JsonElement parse = new JsonParser().parse(json);

        JsonElement documents = parse.getAsJsonObject().get("documents");
        JsonArray asJsonArray = documents.getAsJsonArray();

        List<String> casBase64 = new ArrayList<>();

        for (int i = 0; i < asJsonArray.size(); i++) {
            String aCas = asJsonArray.get(i).toString();
            casBase64.add(aCas.substring(1, aCas.length() - 1));
        }

        jcasBase64 = casBase64.toArray(new String[0]);

        typesystemBase64 = parse.getAsJsonObject().get("typeSystem").toString();
        typesystemBase64 = typesystemBase64.substring(1, typesystemBase64.length() - 1);

        annotationName = parse.getAsJsonObject().get("layer").toString();
        annotationName = annotationName.substring(1, annotationName.length() - 1);

        annotationFieldName = parse.getAsJsonObject().get("feature").toString();
        annotationFieldName = annotationFieldName.substring(1, annotationFieldName.length() - 1);
    }

    private void train() throws Exception
    {
        initTrain();
        // Train Model
        TrainNewModel m = new TrainNewModel();
        m.run(jcasBase64, typesystemBase64, annotationName, annotationFieldName,
                modelLocation.getRoot());
        assertTrue(modelLocation.getRoot().exists());
        File theModel = new File(modelLocation.getRoot(), Constants.MODEL_CLASSIFIER);
        assertTrue(theModel.exists());
    }

    private void initTrain() throws IOException
    {
//        String json = FileUtils
//                .readFileToString(new File(System.getProperty("user.home")+"/Desktop/training.json"), "utf-8");
        
        String json = FileUtils
                .readFileToString(new File("src/test/resources/jsonTrainRequestV2small.txt"), "utf-8");

        JsonElement parse = new JsonParser().parse(json);

        JsonElement documents = parse.getAsJsonObject().get("documents");
        JsonArray asJsonArray = documents.getAsJsonArray();

        List<String> casBase64 = new ArrayList<>();

        for (int i = 0; i < asJsonArray.size(); i++) {
            String aCas = asJsonArray.get(i).toString();
            casBase64.add(aCas.substring(1, aCas.length() - 1));
        }

        jcasBase64 = casBase64.toArray(new String[0]);

        typesystemBase64 = parse.getAsJsonObject().get("typeSystem").toString();
        typesystemBase64 = typesystemBase64.substring(1, typesystemBase64.length() - 1);

        annotationName = parse.getAsJsonObject().get("layer").toString();
        annotationName = annotationName.substring(1, annotationName.length() - 1);

        annotationFieldName = parse.getAsJsonObject().get("feature").toString();
        annotationFieldName = annotationFieldName.substring(1, annotationFieldName.length() - 1);
    }
}
