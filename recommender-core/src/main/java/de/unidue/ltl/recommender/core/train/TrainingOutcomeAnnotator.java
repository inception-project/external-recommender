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

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationSequence;
import org.dkpro.tc.api.type.TextClassificationTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * This annotator sets up the CAS with the required annotation for DKPro TC for
 * training a model. The reference information (i.e. the labels) are extracted
 * from the externally provided type/field information.
 */
public class TrainingOutcomeAnnotator extends JCasAnnotator_ImplBase {
	public static final String PARAM_ANNOTATION_TARGET_NAME = "inputAnnotation";
	@ConfigurationParameter(name = PARAM_ANNOTATION_TARGET_NAME, mandatory = true)
	private String annotationName;

	public static final String PARAM_ANNOTATION_TARGET_FIELD_NAME = "inputAnnotationType";
	@ConfigurationParameter(name = PARAM_ANNOTATION_TARGET_FIELD_NAME, mandatory = true)
	private String fieldName;

	int tcId = 0;
	
	protected Logger logger = LoggerFactory.getLogger(TrainingOutcomeAnnotator.class);

	public static final String OTHER_OUTCOME = "dkpro-tc-placeholder";

	Type annotationType = null;
	Feature feature = null;
	
	boolean atLeastOneAnnotated=false;

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		if (annotationType == null) {
			loadTypeInformation(aJCas);
		}

		List<Token> tokens = new ArrayList<Token>(JCasUtil.select(aJCas, Token.class));
		List<AnnotationFS> classificationTargets = new ArrayList<AnnotationFS>(
				CasUtil.select(aJCas.getCas(), annotationType));

        logger.debug("Found [" + classificationTargets.size() + "] training labels of type ["
                + annotationType.getName() + "]");
        classificationTargets.forEach(x -> logger
                .debug("[" + x.getCoveredText() + "->" + x.getFeatureValueAsString(feature) + "]"));
		
		// Annotate the targets
		for (AnnotationFS a : classificationTargets) {

			List<Token> tokensCovered = JCasUtil.selectCovered(aJCas, Token.class, a);
			
			// if two or more tokens are covered each is annotated separately
			String ov = a.getFeatureValueAsString(feature);
			if(ov == null) {
                logger.debug("The feature value [" + feature.getName() + "] of text ["
                        + a.getCoveredText() + "] is null - excluding information from training");
			    continue;
			}
			for (Token t : tokensCovered) {
				TextClassificationTarget aTarget = new TextClassificationTarget(aJCas, t.getBegin(), t.getEnd());
				aTarget.setId(tcId++);
				aTarget.addToIndexes();

				TextClassificationOutcome outcome = new TextClassificationOutcome(aJCas, t.getBegin(), t.getEnd());
				outcome.setOutcome(ov);
				outcome.addToIndexes();
				atLeastOneAnnotated = true;
			}
		}

		for (Sentence s : JCasUtil.select(aJCas, Sentence.class)) {
			TextClassificationSequence classSeq = new TextClassificationSequence(aJCas, s.getBegin(), s.getEnd());
			classSeq.addToIndexes();
		}
		
		if (!atLeastOneAnnotated && tokens.size() > 0) {
		    logger.debug("No annotations found - annotating the first token to ensure something is annotated");
		    annotatedDummy(aJCas, tokens.get(0));
		}

        // This leads to an extremely skewed distribution of data, i.e. 99% will be the dummy values;
        // better work only with what have been annotated so far
//		annotateTokensWithoutCoveringTargetAsOther(aJCas, tokens, annotationType);
	}

	private void annotatedDummy(JCas aJCas, Token t)
    {
	    TextClassificationTarget aTarget = new TextClassificationTarget(aJCas, t.getBegin(), t.getEnd());
        aTarget.setId(tcId++);
        aTarget.addToIndexes();

        TextClassificationOutcome outcome = new TextClassificationOutcome(aJCas, t.getBegin(), t.getEnd());
        outcome.setOutcome(OTHER_OUTCOME);
        outcome.addToIndexes();        
    }

    private void loadTypeInformation(JCas aJCas) {
		annotationType = CasUtil.getAnnotationType(aJCas.getCas(), annotationName);
		feature = FeaturePathUtils.getType(aJCas.getTypeSystem(), annotationName).getFeatureByBaseName(fieldName);
	}

//	private void annotateTokensWithoutCoveringTargetAsOther(JCas aJCas, List<Token> tokens, Type annotationType) {
//		for (Token t : tokens) {
//
//			List<TextClassificationTarget> targets = JCasUtil.selectCovered(aJCas, TextClassificationTarget.class, t);
//			if (!targets.isEmpty()) {
//				// some target is there i.e. has been annotated with a target - skip
//				continue;
//			}
//
//			TextClassificationTarget aTarget = new TextClassificationTarget(aJCas, t.getBegin(), t.getEnd());
//			aTarget.setId(tcId++);
//			aTarget.addToIndexes();
//
//			TextClassificationOutcome outcome = new TextClassificationOutcome(aJCas, t.getBegin(), t.getEnd());
//			outcome.setOutcome(OTHER_OUTCOME);
//			outcome.addToIndexes();
//
//		}
//	}

}
