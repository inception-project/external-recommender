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

import com.fasterxml.jackson.annotation.JsonProperty;

public class PredictionRequest
{
    @JsonProperty("typeSystem")
    private String typeSystem;

    @JsonProperty("document")
    private String document;

    @JsonProperty("layer")
    private String layer;

    @JsonProperty("feature")
    private String feature;

    public String getTypeSystem()
    {
        return typeSystem;
    }

    public void setTypeSystem(String aTypeSystem)
    {
        typeSystem = aTypeSystem;
    }

    public String getDocument()
    {
        return document;
    }

    public void setDocument(String aDocument)
    {
        document = aDocument;
    }

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

    public InceptionRequest toInceptionRequest()
    {
        InceptionRequest result = new InceptionRequest();
        result.setDocuments(new String[] { document });
        result.setTypeSystem(typeSystem);
        result.setLayer(layer);
        result.setFeature(feature);
        return result;
    }
}
