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

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationSequence;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class TargetSetterAnnotator extends JCasAnnotator_ImplBase {

	int tcId = 0;

	/**
	 * Prepares the JCas for a prediction. Iterates the tokens and sets the
	 * annotations required by DKPro TC to work.
	 */
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {

		List<Sentence> sents = new ArrayList<Sentence>(JCasUtil.select(aJCas, Sentence.class));
		for (Sentence s : sents) {

			TextClassificationSequence seq = new TextClassificationSequence(aJCas, s.getBegin(), s.getEnd());
			seq.addToIndexes();

			List<Token> tokens = new ArrayList<Token>(JCasUtil.selectCovered(aJCas, Token.class, s));
			for (Token t : tokens) {
				TextClassificationTarget aTarget = new TextClassificationTarget(aJCas, t.getBegin(), t.getEnd());
				aTarget.setId(tcId++);
				aTarget.addToIndexes();

				TextClassificationOutcome outcome = new TextClassificationOutcome(aJCas, t.getBegin(), t.getEnd());
				outcome.setOutcome("UNKNOWN-LABEL");
				outcome.addToIndexes();
			}
		}
	}
}
