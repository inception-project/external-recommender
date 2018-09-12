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
package de.unidue.ltl.recommender.server.tc.prediction;

import java.io.File;
import java.util.List;

import de.unidue.ltl.recommender.server.InceptionRequest;

public interface Predictor
{
    void predict(InceptionRequest req, File model) throws Exception;

    List<String> getResults() throws Exception;
    
    String getResultsAsJson() throws Exception;
}
