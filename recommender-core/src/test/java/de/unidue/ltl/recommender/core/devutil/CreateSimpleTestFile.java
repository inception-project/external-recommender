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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.fit.factory.JCasFactory;
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
        String json = FileUtils
                .readFileToString(new File("src/test/resources/jsonTrainRequestV2small.txt"), "utf-8");

        JsonElement parse = new JsonParser().parse(json);
        
        JsonElement documents = parse.getAsJsonObject().get("documents");
        JsonArray asJsonArray = documents.getAsJsonArray();

        for (int i = 0; i < asJsonArray.size(); i++) {
            String aCas = asJsonArray.get(i).toString();
            aCas = aCas.substring(1, aCas.length()-1);
            aCas = new String(Base64.decodeBase64(aCas.getBytes()));
            File toDiscTemp = FileUtil.createTempFile("toDiskRaw", ".txt");
            FileUtils.writeStringToFile(toDiscTemp, aCas, "utf-8");
            FileInputStream fis = new FileInputStream(toDiscTemp);
            JCas createJCas = JCasFactory.createJCas();
            XmiCasDeserializer.deserialize(fis, createJCas.getCas());
            fis.close();
            
            List<Sentence> sentences = new ArrayList<Sentence>(JCasUtil.select(createJCas, Sentence.class));
            for(int j=20; j < sentences.size(); j++) {
                List<Token> tokens = JCasUtil.selectCovered(createJCas,Token.class, sentences.get(j));
                tokens.forEach(x->x.removeFromIndexes());
                sentences.get(j).removeFromIndexes();
            }
            
//            //for prediction - remove NEs
//            List<NamedEntity> ne = new ArrayList<NamedEntity>(JCasUtil.select(createJCas, NamedEntity.class));
//            ne.forEach(x->x.removeFromIndexes());
            
            File toDiscClean = FileUtil.createTempFile("toDiskClean", ".txt");
            FileOutputStream fos = new FileOutputStream(toDiscClean);
            XmiCasSerializer.serialize(createJCas.getCas(), fos);
            fos.close();
            
            String shrinkedCas = FileUtils.readFileToString(toDiscClean, "utf-8");
            shrinkedCas = "\""+Base64.encodeBase64String(shrinkedCas.getBytes()) + "\"";
            
            asJsonArray.set(i, new JsonParser().parse(shrinkedCas));
            
            toDiscTemp.delete();
            toDiscClean.delete();
        }
        
        String string = parse.getAsJsonObject().toString();
        FileUtils.writeStringToFile(new File(System.getProperty("user.home")+"/Desktop/predictJsonV2small.txt"), string, "utf-8");

//        jcasBase64 = casBase64.toArray(new String[0]);

    }

}
