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

public class RepositoryUtil
{
    public static void nullCheck(Object o)
    {
        if (isNull(o)) {
            throw new NullPointerException("Variable is null");
        }
    }

    public static boolean isNull(Object o)
    {
        return o == null;
    }

    public static void createFileSystemLocation(File location)
    {
        if (!location.exists()) {
            boolean mkdirs = location.mkdirs();
            if (!mkdirs) {
                throw new InstantiationError("Specified location [" + location.getAbsolutePath()
                        + "] did not exist - failed to create folder");
            }
        }
    }
    
}
