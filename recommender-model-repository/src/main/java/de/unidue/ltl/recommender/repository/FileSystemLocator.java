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
package de.unidue.ltl.recommender.repository;

import java.io.File;

class FileSystemLocator
{
    private static String CONST = "_";

    static File locate(File root, Entry entry)
    {

        String filename = entry.getId() + CONST + entry.getTimeStamp();

        return new File(root, filename);
    }

    static boolean verifyFolderName(String folderName)
    {

        String[] split = folderName.split(CONST);
        if (split.length != 2) {
            return false;
        }

        String id = split[0];
        if (id.contains("/") || id.contains("\\")) {
            // more than just the foldername provided, e.g absolute path
            return false;
        }

        String timestampString = split[1];

        return timestampString.matches("^[0-9]+$");
    }

    static long getTimeStamp(String folderName)
    {
        if (!verifyFolderName(folderName)) {
            throw new IllegalArgumentException("[" + folderName + "] is not valid");
        }

        String[] split = folderName.split(CONST);
        String timestampString = split[1];

        return Long.parseLong(timestampString);
    }

    public static String getId(String folderName)
    {
        if (!verifyFolderName(folderName)) {
            throw new IllegalArgumentException("[" + folderName + "] is not valid");
        }

        String[] split = folderName.split(CONST);

        return split[0];
    }

}
