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

public class InceptionRequest
{
    private String [] documents;
    private String typeSystem;
    private String layer;
    private String feature;
    private String anchoringMode;

    public String [] getDocuments()
    {
        return documents;
    }


    public void setDocuments(String [] documents)
    {
        this.documents = documents;
    }


    public String getTypeSystem()
    {
        return typeSystem;
    }
    
    public void setAnchoringMode(String anchoringMode){
        this.anchoringMode = anchoringMode;
    }
    
    public String getAnchoringMode(){
        return this.anchoringMode;
    }


    public void setTypeSystem(String typeSystem)
    {
        this.typeSystem = typeSystem;
    }


    public String getLayer()
    {
        return layer;
    }


    public void setLayer(String Layer)
    {
        this.layer = Layer;
    }


    public String getFeature()
    {
        return feature;
    }


    public void setFeature(String feature)
    {
        this.feature = feature;
    }
}
