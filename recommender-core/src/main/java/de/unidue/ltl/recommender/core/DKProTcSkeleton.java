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
import org.apache.uima.util.TypeSystemUtil;
import org.codehaus.plexus.util.Base64;

import com.google.common.io.Files;

import de.unidue.ltl.recommender.core.util.CoreUtil;

public abstract class DKProTcSkeleton {
	protected File typeSystemXML;
	protected File binCasInputFolder;
	protected File dkproHomeFallback;

	public abstract void run(String[] casBase64, String typeSystemBase64, String annotationName,
			String annotationFieldName, File targetFolder) throws Exception;

	public DKProTcSkeleton() throws Exception {
		typeSystemXML = FileUtil.createTempFile("typeSystemTmp", ".txt");
		binCasInputFolder = Files.createTempDir();
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
		}
	}

	/**
	 * Decodes the CAS information and writes them as binary CAS to disc. Furthermore, the typesystem is extracted and separately written to disc 
	 * @param casBase64
	 * 			An array of base64 encoded CAS
	 * @param typeSystemBase64
	 * 			The typesystem used by the CAS also in base64 encoded
	 * @return
	 * 			a TypeSystemDescription
	 * 
	 * @throws Exception
	 * 			In cas of an error
	 */
	protected TypeSystemDescription prepare(String[] casBase64, String typeSystemBase64) throws Exception {
		writeTypeSystemToFile(decodeBase64(typeSystemBase64));
		TypeSystemDescription typeSystemDesc = null;
		for (String cas : casBase64) {
			JCas jCas = CoreUtil.deserialize(decodeBase64(cas), typeSystemXML);

			typeSystemDesc = TypeSystemUtil.typeSystem2TypeSystemDescription(jCas.getTypeSystem());
			CoreUtil.writeCasBinary(jCas, typeSystemDesc, binCasInputFolder);
		}

		return typeSystemDesc;
	}

	/**
	 * Decodes base64 encoding on a string
	 * @param v
	 * 			the base64 string
	 * @return
	 */
	protected String decodeBase64(String v) {
		return new String(Base64.decodeBase64(v.getBytes()));
	}

	protected void cleanUp() throws IOException {
		FileUtils.deleteQuietly(typeSystemXML);
		FileUtils.deleteDirectory(binCasInputFolder);
		if (dkproHomeFallback != null) {
			FileUtils.deleteDirectory(dkproHomeFallback);
		}
	}

	protected void writeTypeSystemToFile(String typeSystem) throws IOException {
		FileUtils.writeStringToFile(typeSystemXML, typeSystem, "utf-8");
	}
}
