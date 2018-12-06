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
import org.apache.uima.cas.Type;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * This annotator sets up the CAS with the required annotation for DKPro TC for training a model.
 * The reference information (i.e. the labels) are extracted from the externally provided type/field
 * information.
 */
public class MultipleTokenSpanLevelTrainingOutcomeAnnotator
    extends SingleTokenLevelTrainingOutcomeAnnotator
{

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {
        super.process(aJCas);
        List<Token> tokens = new ArrayList<Token>(JCasUtil.select(aJCas, Token.class));
        annotateTokensWithoutCoveringTargetAsOther(aJCas, tokens, annotationType);
    }

    private void annotateTokensWithoutCoveringTargetAsOther(JCas aJCas, List<Token> tokens,
            Type annotationType)
    {
        for (Token t : tokens) {

            List<TextClassificationTarget> targets = JCasUtil.selectCovered(aJCas,
                    TextClassificationTarget.class, t);
            if (!targets.isEmpty()) {
                // some target is there i.e. has been annotated with a target - skip
                continue;
            }

            TextClassificationTarget aTarget = new TextClassificationTarget(aJCas, t.getBegin(),
                    t.getEnd());
            aTarget.setId(tcId++);
            aTarget.addToIndexes();

            TextClassificationOutcome outcome = new TextClassificationOutcome(aJCas, t.getBegin(),
                    t.getEnd());
            outcome.setOutcome(OTHER_OUTCOME);
            outcome.addToIndexes();

        }
    }

}
