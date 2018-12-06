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

    @JsonProperty("typeSystem")
    private String typeSystem;

    @JsonProperty("documents")
    private List<Document> documents;

    @JsonProperty("metadata")
    private Metadata metadata;

    public String getTypeSystem()
    {
        return typeSystem;
    }

    public void setTypeSystem(String aTypeSystem)
    {
        typeSystem = aTypeSystem;
    }

    public List<Document> getDocuments()
    {
        return documents;
    }

    public void setDocuments(List<Document> aDocuments)
    {
        documents = aDocuments;
    }

    public Metadata getMetadata()
    {
        return metadata;
    }

    public void setMetadata(Metadata aMetadata) {
        metadata = aMetadata;
    }

    public InceptionRequest toInceptionRequest()
    {
        InceptionRequest result = new InceptionRequest();
        String [] xmiDocuments = documents.stream().map(Document::getXmi).toArray(String[]::new);
        result.setDocuments(xmiDocuments);
        result.setTypeSystem(typeSystem);
        result.setLayer(getMetadata().getLayer());
        result.setFeature(getMetadata().getFeature());
        result.setAnchoringMode(getMetadata().getAnchoringMode());
        return result;
    }
}
