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
package de.unidue.ltl.recommender.core;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.uima.jcas.JCas;
import org.apache.uima.pear.util.FileUtil;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

import de.unidue.ltl.recommender.core.train.TrainNewModel;
import de.unidue.ltl.recommender.core.util.CoreUtil;

public abstract class DKProTcSkeleton {
    protected File typeSystemXML;
    protected File binCasInputFolder;
    protected File dkproHomeFallback;

    protected Logger logger = LoggerFactory.getLogger(TrainNewModel.class);
    
    public abstract void run(String[]  cas, String typesystem, String annotationName,
                             String annotationFieldName, File targetFolder) throws Exception;

    public DKProTcSkeleton() throws Exception {
        typeSystemXML = FileUtil.createTempFile("typeSystemTmp", ".txt");
        binCasInputFolder = Files.createTempDir();
        typeSystemXML.deleteOnExit();
        binCasInputFolder.deleteOnExit();
    }

    /**
     * Tests if DKPRO_HOME is set as environmental variable. If it is not set, the
     * variable is set pointing to a temporary folder that is deleted at shutdown
     */
    protected void dkproHome() {
        String property = System.getProperty("DKPRO_HOME");
        if (property == null || property.isEmpty()) {
            dkproHomeFallback = Files.createTempDir();
            System.setProperty("DKPRO_HOME", dkproHomeFallback.getAbsolutePath());
            dkproHomeFallback.deleteOnExit();
            logger.debug("Set DKPRO_HOME to [" + dkproHomeFallback.getAbsolutePath() + "]");
        }
    }

    /**
     * Decodes the CAS information and writes them as binary CAS to disc. Furthermore, the 
     * is extracted and separately written to disc
     *
     * @param  casses        An array of CAS xmi strings
     * @param typesystem The typesystem used by the CAS
     * @return a TypeSystemDescription
     * @throws Exception In case of an error
     */
    protected TypeSystemDescription prepare(String[]  casses, String typesystem)
            throws Exception {
        writeTypeSystemToFile(typesystem);
        TypeSystemDescription typeSystemDesc = null;
        for (String cas : casses) {
            JCas jCas = CoreUtil.deserialize(cas, typeSystemXML);
            CoreUtil.writeCasBinary(jCas, binCasInputFolder);
        }
        return typeSystemDesc;
    }

    protected void cleanUp() throws IOException {
        logger.debug("Deleting quitely [" + typeSystemXML.getAbsolutePath() + "] ["
                + binCasInputFolder.getAbsolutePath() + "]");
        FileUtils.deleteQuietly(typeSystemXML);
        FileUtils.deleteDirectory(binCasInputFolder);
    }

    protected void writeTypeSystemToFile(String typeSystem) throws IOException {
        FileUtils.writeStringToFile(typeSystemXML, typeSystem, "utf-8");
    }
}
