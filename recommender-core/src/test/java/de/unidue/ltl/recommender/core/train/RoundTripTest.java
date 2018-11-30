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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dkpro.tc.core.Constants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.unidue.ltl.recommender.core.predict.PredictionWithModel;

public class RoundTripTest
{
    String[] jcas;
    String typesystem;
    String annotationName;
    String annotationFieldName;

    File resultFolder = null;
    File modelLocation = null;

    @Before
    public void setup() throws Exception
    {
        File root = FileUtils.getTempDirectory();
        resultFolder = new File(root, "resultOut/");
        resultFolder.mkdir();
        modelLocation = new File(root, "modelOut/");
        modelLocation.mkdir();

    }

    @After
    public void cleanUp() throws IOException
    {
        FileUtils.deleteQuietly(modelLocation);
        FileUtils.deleteQuietly(resultFolder);
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
        PredictionWithModel pwm = new PredictionWithModel(resultFolder);
        pwm.run(jcas, typesystem, annotationName, annotationFieldName, modelLocation);

        List<File> files = getFiles(resultFolder);
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
        String json = FileUtils.readFileToString(
                new File("src/test/resources/jsonPredictRequestV3small.json"), "utf-8");

        JsonObject parse = new JsonParser().parse(json).getAsJsonObject();
        JsonObject document = parse.get("document").getAsJsonObject();
        JsonObject metadata = parse.get("metadata").getAsJsonObject();

        List<String> cas = new ArrayList<>();
        cas.add(document.get("xmi").getAsString());

        jcas = cas.toArray(new String[0]);

        typesystem = parse.get("typeSystem").getAsString();
        annotationName = metadata.get("layer").getAsString();
        annotationFieldName = metadata.get("feature").getAsString();
    }

    private void train() throws Exception
    {
        initTrain();
        // Train Model
        TrainNewModel m = new TrainNewModel();
        m.run(jcas, typesystem, annotationName, annotationFieldName, modelLocation);
        assertTrue(modelLocation.exists());
        File theModel = new File(modelLocation, Constants.MODEL_CLASSIFIER);
        assertTrue(theModel.getAbsolutePath() + " does not exist", theModel.exists());
    }

    private void initTrain() throws IOException
    {
        String json = FileUtils.readFileToString(
                new File("src/test/resources/jsonTrainRequestV3small.json"),
                StandardCharsets.UTF_8);

        JsonObject parse = new JsonParser().parse(json).getAsJsonObject();
        JsonObject metadata = parse.get("metadata").getAsJsonObject();

        JsonArray documents = parse.getAsJsonObject().get("documents").getAsJsonArray();

        List<String> casses = new ArrayList<>();

        for (int i = 0; i < documents.size(); i++) {
            String aCas = documents.get(i).getAsJsonObject().get("xmi").getAsString();
            casses.add(aCas);
        }

        jcas = casses.toArray(new String[0]);

        typesystem = parse.get("typeSystem").getAsString();

        annotationName = metadata.get("layer").getAsString();

        annotationFieldName = metadata.get("feature").getAsString();
    }
}
