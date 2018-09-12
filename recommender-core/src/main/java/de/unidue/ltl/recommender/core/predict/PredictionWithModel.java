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

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.dkpro.tc.ml.uima.TcAnnotator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;
import de.unidue.ltl.recommender.core.DKProTcSkeleton;

public class PredictionWithModel
    extends DKProTcSkeleton
{
    private static final Logger logger = LoggerFactory
            .getLogger(PredictionWithModel.class.getName());

    File predictionOutput;

    public PredictionWithModel(File resultFolder) throws Exception
    {
        super();
        predictionOutput = resultFolder;
    }

    @Override
    public void run(String [] casBase64, String typeSystemBase64, String annotationName,
            String annotationFieldName, File model)
        throws Exception
    {
        dkproHome();

        TypeSystemDescription typeSystem = prepare(casBase64, typeSystemBase64);

        startPrediction(binCasInputFolder, typeSystem, model, annotationName, annotationFieldName);

        cleanUp();
    }

    private void startPrediction(File casPredictOutput, TypeSystemDescription typeSystem,
            File model, String annotationName, String annotationFieldName)
        throws Exception
    {

        logger.info("Start prediction pipeline with model [" + model.getAbsolutePath()
                + "], results will be stored at [" + predictionOutput.getAbsolutePath() + "]");

        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                BinaryCasReader.class, BinaryCasReader.PARAM_MERGE_TYPE_SYSTEM, true,
                BinaryCasReader.PARAM_LANGUAGE, "x-undefined", BinaryCasReader.PARAM_SOURCE_LOCATION,
                casPredictOutput.getAbsoluteFile(), BinaryCasReader.PARAM_PATTERNS, "*.bin");

        AnalysisEngineDescription tcAnnotation = AnalysisEngineFactory
                .createEngineDescription(TargetSetterAnnotator.class);

        AnalysisEngineDescription annotator = AnalysisEngineFactory.createEngineDescription(
                TcAnnotator.class, TcAnnotator.PARAM_NAME_SEQUENCE_ANNOTATION,
                Sentence.class.getName(), TcAnnotator.PARAM_NAME_UNIT_ANNOTATION,
                Token.class.getName(), TcAnnotator.PARAM_TC_MODEL_LOCATION, model,
                TcAnnotator.PARAM_RETAIN_TARGETS, false);

        AnalysisEngineDescription resultWriter = AnalysisEngineFactory.createEngineDescription(
                ResultWriterAnnotator.class, ResultWriterAnnotator.PARAM_ANNOTATION_TARGET_NAME,
                annotationName, ResultWriterAnnotator.PARAM_ANNOTATION_TARGET_FIELD_NAME,
                annotationFieldName, ResultWriterAnnotator.PARAM_OUTPUT_FOLDER, predictionOutput,
                ResultWriterAnnotator.PARAM_DEBUG_SYS_OUT, true,
                ResultWriterAnnotator.PARAM_MERGE_ADJACENT_ANNOTATIONS, true);

        SimplePipeline.runPipeline(reader, tcAnnotation, annotator, resultWriter);
    }
}
