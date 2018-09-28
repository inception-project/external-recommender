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
package de.unidue.ltl.recommender.server.http;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TrainingRequest {
    @JsonProperty("layer")
    private String layer;

    @JsonProperty("feature")
    private String feature;

    @JsonProperty("typeSystem")
    private String typeSystem;

    @JsonProperty("documents")
    private List<String> documents;

    public String getLayer()
    {
        return layer;
    }

    public void setLayer(String aLayer)
    {
        layer = aLayer;
    }

    public String getFeature()
    {
        return feature;
    }

    public void setFeature(String aFeature)
    {
        feature = aFeature;
    }

    public String getTypeSystem()
    {
        return typeSystem;
    }

    public void setTypeSystem(String aTypeSystem)
    {
        typeSystem = aTypeSystem;
    }

    public List<String> getDocuments()
    {
        return documents;
    }

    public void setDocuments(List<String> aDocuments)
    {
        documents = aDocuments;
    }

    public InceptionRequest toInceptionRequest()
    {
        InceptionRequest result = new InceptionRequest();
        result.setDocuments(documents.toArray(new String[documents.size()]));
        result.setTypeSystem(typeSystem);
        result.setLayer(layer);
        result.setFeature(feature);
        return result;
    }
}
