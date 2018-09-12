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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.pear.util.FileUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class PrintDeSerializedCasToConsole
{
    public static void main(String [] args) throws Exception {
        String json = FileUtils
                .readFileToString(new File("src/test/resources/jsonTrainRequestV2small.txt"), "utf-8");
//        String json = FileUtils.readFileToString(new File(System.getProperty("user.home")+"/Desktop/training.json"), "utf-8");

        JsonElement parse = new JsonParser().parse(json);
        
        String annontationType = parse.getAsJsonObject().get("layer").toString();
        annontationType = annontationType.substring(1, annontationType.length()-1);
        
        String feature = parse.getAsJsonObject().get("feature").toString();
        feature = feature.substring(1, feature.length()-1);
        
        JsonElement documents = parse.getAsJsonObject().get("documents");
        JsonArray asJsonArray = documents.getAsJsonArray();

        for (int i = 0; i < asJsonArray.size(); i++) {
            String aCas = asJsonArray.get(i).toString();
            aCas = aCas.substring(1, aCas.length()-1);
            aCas = new String(Base64.decodeBase64(aCas.getBytes()));
            File toDiscTemp = FileUtil.createTempFile("toDiskRaw", ".txt");
            FileUtils.writeStringToFile(toDiscTemp, aCas, "utf-8");
            FileInputStream fis = new FileInputStream(toDiscTemp);
            JCas aJCas = JCasFactory.createJCas();
            XmiCasDeserializer.deserialize(fis, aJCas.getCas());
            fis.close();
            toDiscTemp.delete();
            
            
            Type annotationType = CasUtil.getAnnotationType(aJCas.getCas(), annontationType);
            
            List<AnnotationFS> select = new ArrayList<AnnotationFS>(CasUtil.select(aJCas.getCas(), annotationType));
            
            for(Sentence s : JCasUtil.select(aJCas, Sentence.class)) {
                List<Token> sentToks = JCasUtil.selectCovered(aJCas, Token.class, s);
                
                for(int j=0; j < sentToks.size(); j++) {
                    Token t = sentToks.get(j);
                    AnnotationFS a = null;
                    for(AnnotationFS afs : select) {
                        if (sentToks.get(j).getBegin() == afs.getBegin()) {
                            a = afs;
                            break;
                        } else if(sentToks.get(j).getBegin() > afs.getBegin() && sentToks.get(j).getEnd() < afs.getEnd()) {
                            a = afs;
                            break;
                        }else if(sentToks.get(j).getEnd() == afs.getEnd()) {
                            a = afs;
                            break;
                        }
                    }
                    String predictionEntry="";
                    if(a!=null) {
                        Feature featureByBaseName = FeaturePathUtils.getType(aJCas.getTypeSystem(), annontationType)
                        .getFeatureByBaseName(feature);
                        predictionEntry = " " + a.getFeatureValueAsString(featureByBaseName);
                    }else {
                        predictionEntry="--";
                    }
                    System.out.println(String.format("%5d %5d %20s %15s", t.getBegin(), t.getEnd(), t.getCoveredText(), predictionEntry));
                }
            }
            
            
//            printToConsole(aCas);
        }
        
    }

    private static void printToConsole(String aCas)
    {
        aCas = aCas.replaceAll(">", ">\n");
        System.out.println(aCas);        
    }

}
