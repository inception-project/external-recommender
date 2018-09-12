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

package de.unidue.ltl.recommender.server.repository;

import java.io.File;

import de.unidue.ltl.recommender.server.train.InceptionRecommenderModel;

public interface Repository
{
    /**
     * Retrieves a model by its id from the model repository
     * 
     * @param id
     *            the id value
     * @return a model
     */
    InceptionRecommenderModel getModel(String id);

    /**
     * Checks in a model into the repository. If a model with the specified id does not exist yet a
     * new model is registered otherwise the existing entry is overwritten.
     * 
     * @param id
     *            the id of the model
     * @param timestamp
     *            a timestamp to track when a model was created
     * @param sourceLocation
     *            the location of the model in the filesystem
     * @param deleteSourceLocation
     *            if the model at the source location shall be deleted after copying the folder into
     *            the repository
     * @throws Exception
     *             in case of an error
     */
    void checkInModel(String id, long timestamp, File sourceLocation, boolean deleteSourceLocation)
        throws Exception;

    /**
     * Checks in a model into the repository. If a model with the specified id does not exist yet a
     * new model is registered otherwise the existing entry is overwritten.
     * 
     * @param irm
     *          a trained inception recommender model
     * @throws Exception
     *          in case of any error
     */
    void checkInModel(InceptionRecommenderModel irm, boolean deleteSourceLocation)
            throws Exception;
}
