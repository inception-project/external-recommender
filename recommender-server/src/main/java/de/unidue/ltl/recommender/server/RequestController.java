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

package de.unidue.ltl.recommender.server;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.unidue.ltl.recommender.server.repository.Repository;
import de.unidue.ltl.recommender.server.tc.prediction.Predictor;
import de.unidue.ltl.recommender.server.train.InceptionRecommenderModel;
import de.unidue.ltl.recommender.server.train.Trainer;

@RestController
public class RequestController
{
    @Autowired
    Repository repository;

    @Autowired
    Trainer trainer;
    
    @Autowired
    Predictor predictor;

    @RequestMapping(value = "/train", method = RequestMethod.POST)
    public ResponseEntity<String> executeTraining(@RequestBody InceptionRequest inceptionReq)
    {
        try {
            trainModel(inceptionReq);
        }
        catch (Exception e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<String>(HttpStatus.OK);
    }

    private InceptionRecommenderModel trainModel(InceptionRequest inceptionReq) throws Exception
    {
        InceptionRecommenderModel trainedModel = null;
        trainedModel = trainer.train(inceptionReq);
        repository.checkInModel(trainedModel, true);
        return trainedModel;
    }

    @RequestMapping(value = "/predict", method = RequestMethod.POST)
    public ResponseEntity<String> executePrediction(@RequestBody InceptionRequest inceptionReq)
    {
        String xmlCasArrayAsString = "-init-";

        try {
            xmlCasArrayAsString = prediction(inceptionReq);
        }
        catch (Exception e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(xmlCasArrayAsString, HttpStatus.OK);
    }

    private String prediction(InceptionRequest inceptionReq) throws Exception
    {
        InceptionRecommenderModel model = repository.getModel(inceptionReq.layer);
        predictor.predict(inceptionReq, model.getFileSystemLocation());
        return predictor.getResultsAsJson();
    }

    @ExceptionHandler
    void handleIllegalArgumentException(IllegalArgumentException e, HttpServletResponse response)
        throws IOException
    {
        response.sendError(HttpStatus.BAD_REQUEST.value());
    }
}