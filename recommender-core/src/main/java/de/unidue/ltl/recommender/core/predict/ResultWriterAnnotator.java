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

package de.unidue.ltl.recommender.core.predict;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.type.TextClassificationOutcome;

import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.unidue.ltl.recommender.core.train.TrainingOutcomeAnnotator;
import de.unidue.ltl.recommender.core.util.CoreUtil;

public class ResultWriterAnnotator
    extends JCasAnnotator_ImplBase
{
    public static final String PARAM_ANNOTATION_TARGET_NAME = "annotationName";
    @ConfigurationParameter(name = PARAM_ANNOTATION_TARGET_NAME, mandatory = true)
    private String annotation;

    public static final String PARAM_ANNOTATION_TARGET_FIELD_NAME = "annotationFieldName";
    @ConfigurationParameter(name = PARAM_ANNOTATION_TARGET_FIELD_NAME, mandatory = true)
    private String annoValue;

    public static final String PARAM_MERGE_ADJACENT_ANNOTATIONS = "mergeAdjecent";
    @ConfigurationParameter(name = PARAM_MERGE_ADJACENT_ANNOTATIONS, mandatory = true, defaultValue = "true")
    private boolean mergeAdjacent;

    public static final String PARAM_OUTPUT_FOLDER = "outputFolder";
    @ConfigurationParameter(name = PARAM_OUTPUT_FOLDER, mandatory = true)
    private File outputFolder;

    public static final String PARAM_DEBUG_SYS_OUT = "debugSysOut";
    @ConfigurationParameter(name = PARAM_DEBUG_SYS_OUT, mandatory = false, defaultValue = "false")
    private boolean debug;

    @Override
    public void initialize(final UimaContext context) throws ResourceInitializationException
    {
        super.initialize(context);
    }

    int casCounter = 0;

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {
        List<Sentence> sentences = new ArrayList<Sentence>(JCasUtil.select(aJCas, Sentence.class));

        for (Sentence s : sentences) {
            List<TextClassificationOutcome> outcomes = JCasUtil.selectCovered(aJCas,
                    TextClassificationOutcome.class, s);

            for (int j = 0; j < outcomes.size(); j++) {

                if (outcomes.get(j).getOutcome().equals(TrainingOutcomeAnnotator.OTHER_OUTCOME)) {
                    // is class "no class"
                    continue;
                }

                int begin = outcomes.get(j).getBegin();
                int end = outcomes.get(j).getEnd();

                int adjacentLen = 0;
                if (mergeAdjacent) {
                    // Look-ahead to merge adjacent annotations of same feature value
                    adjacentLen = collectNumberOfMergeCandidates(outcomes, j);
                    end = outcomes.get(j + adjacentLen).getEnd();
                }
                
                annotateTargetAnnotation(aJCas, begin, end, outcomes, j);

                j += adjacentLen;
            }
        }

        debugSysOut(aJCas);

        try {
            serializeCas(aJCas, casCounter++);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    private void annotateTargetAnnotation(JCas aJCas, int begin, int end,  final List<TextClassificationOutcome> outcomes, final int currIdx)
    {
        String value = outcomes.get(currIdx).getOutcome();
        
        Type annotationType = CasUtil.getAnnotationType(aJCas.getCas(), annotation);
        AnnotationFS targetAnno = aJCas.getCas().createAnnotation(annotationType, begin,
                end);
        Feature featureByBaseName = FeaturePathUtils
                .getType(aJCas.getTypeSystem(), annotation).getFeatureByBaseName(annoValue);
        targetAnno.setFeatureValueFromString(featureByBaseName, value);
        ((Annotation) targetAnno).addToIndexes();

        outcomes.get(currIdx).removeFromIndexes();        
    }

    private int collectNumberOfMergeCandidates(List<TextClassificationOutcome> outcomes,
            final int currIdx)
    {
        int k = currIdx + 1;

        while (k < outcomes.size()) {
            if (outcomes.get(k - 1).getOutcome().equals(outcomes.get(k).getOutcome())) {
                k++;
            }
            else {
                break;
            }
        }

        return k - currIdx - 1;
    }

    private void debugSysOut(JCas aJCas)
    {
        if (!debug) {
            return;
        }

        Type annotationType = CasUtil.getAnnotationType(aJCas.getCas(), annotation);

        List<Token> tokens = new ArrayList<Token>(JCasUtil.select(aJCas, Token.class));
        List<AnnotationFS> select = new ArrayList<AnnotationFS>(
                CasUtil.select(aJCas.getCas(), annotationType));

        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);

            AnnotationFS a = null;
            for (AnnotationFS afs : select) {
                if (tokens.get(i).getBegin() == afs.getBegin()) {
                    a = afs;
                    break;
                }
                else if (tokens.get(i).getBegin() > afs.getBegin()
                        && tokens.get(i).getEnd() < afs.getEnd()) {
                    a = afs;
                    break;
                }
                else if (tokens.get(i).getEnd() == afs.getEnd()) {
                    a = afs;
                    break;
                }
            }
            String predictionEntry = "";
            if (a != null) {
                Feature featureByBaseName = FeaturePathUtils
                        .getType(aJCas.getTypeSystem(), annotation).getFeatureByBaseName(annoValue);
                predictionEntry = " " + a.getFeatureValueAsString(featureByBaseName);
            }
            else {
                predictionEntry = "--";
            }

            System.out.println(String.format("%5d %5d %25s %15s", t.getBegin(), t.getEnd(),
                    t.getCoveredText(), predictionEntry));
        }

        // for (AnnotationFS s : select) {
        // Feature featureByBaseName = FeaturePathUtils.getType(aJCas.getTypeSystem(), annotation)
        // .getFeatureByBaseName(annoValue);
        // System.out.println(
        // s.getCoveredText() + " " + s.getFeatureValueAsString(featureByBaseName));
        // }
    }

    private void serializeCas(JCas aJCas, int c) throws Exception
    {
        CoreUtil.serialize(aJCas, new File(outputFolder, "cas_" + c + ".txt"));
    }

    @Override
    public void collectionProcessComplete()
    {
    }

}
