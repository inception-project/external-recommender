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
package de.unidue.ltl.recommender.core.devutil;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.apache.uima.fit.factory.JCasFactory.createJCas;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.pear.util.FileUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class CreateSimpleTestFile
{
    public static void main(String[] args) throws Exception
    {
        String json = FileUtils.readFileToString(
                new File("src/test/resources/jsonTrainRequestV3small.json"), "utf-8");

        JsonElement parse = new JsonParser().parse(json);
        
        JsonElement documents = parse.getAsJsonObject().get("documents");
        JsonArray asJsonArray = documents.getAsJsonArray();

        for (int i = 0; i < asJsonArray.size(); i++) {
            String aCas = asJsonArray.get(i).toString();
            aCas = aCas.substring(1, aCas.length() - 1);
            File toDiscTemp = FileUtil.createTempFile("toDiskRaw", ".txt");
            FileUtils.writeStringToFile(toDiscTemp, aCas, UTF_8);
            JCas createJCas = createJCas();
            try (FileInputStream fis = new FileInputStream(toDiscTemp)) {
                XmiCasDeserializer.deserialize(fis, createJCas.getCas());
            }
            
            List<Sentence> sentences = new ArrayList<Sentence>(JCasUtil.select(createJCas, Sentence.class));
            for(int j = 20; j < sentences.size(); j++) {
                List<Token> tokens = selectCovered(createJCas,Token.class, sentences.get(j));
                tokens.forEach(x -> x.removeFromIndexes());
                sentences.get(j).removeFromIndexes();
            }

            File toDiscClean = FileUtil.createTempFile("toDiskClean", ".txt");
            try (FileOutputStream fos = new FileOutputStream(toDiscClean)) {
                XmiCasSerializer.serialize(createJCas.getCas(), fos);
            }
            
            String shrinkedCas = FileUtils.readFileToString(toDiscClean, UTF_8);
            shrinkedCas = "\"" + shrinkedCas + "\"";
            
            asJsonArray.set(i, new JsonParser().parse(shrinkedCas));
            
            toDiscTemp.delete();
            toDiscClean.delete();
        }
        
        String string = parse.getAsJsonObject().toString();
        writeStringToFile(
                new File(System.getProperty("user.home") + "/Desktop/predictJsonV2small.txt"),
                string, "utf-8");

//        j cas =  cas.toArray(new String[0]);
    }
}
