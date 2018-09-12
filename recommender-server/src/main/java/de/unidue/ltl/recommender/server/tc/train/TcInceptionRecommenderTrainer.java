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

package de.unidue.ltl.recommender.server.tc.train;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.unidue.ltl.recommender.core.train.TrainNewModel;
import de.unidue.ltl.recommender.server.InceptionRequest;
import de.unidue.ltl.recommender.server.train.InceptionRecommenderModel;
import de.unidue.ltl.recommender.server.train.Trainer;

@Component
public class TcInceptionRecommenderTrainer
    implements Trainer
{

    private static final Logger logger = LoggerFactory.getLogger(TcInceptionRecommenderTrainer.class.getName());

    @Override
    public InceptionRecommenderModel train(InceptionRequest req) throws Exception
    {
        String [] documents = req.getDocuments();
        String typeSystem = req.getTypeSystem();
        String layer = req.getLayer();
        String feature = req.getFeature();

        long timestamps = System.currentTimeMillis();
        File modelLocation = new File(FileUtils.getTempDirectory(), layer);

        logger.info("Will store model temporary at [" + modelLocation.getAbsolutePath() + "]");

        TrainNewModel model = new TrainNewModel();
        model.run(documents, typeSystem, layer, feature, modelLocation);

        logger.info("Will create model with id [" + layer + "] at location ["
                + modelLocation.getAbsolutePath() + "]");

        return new TcModel(layer, timestamps, modelLocation);
    }

}
