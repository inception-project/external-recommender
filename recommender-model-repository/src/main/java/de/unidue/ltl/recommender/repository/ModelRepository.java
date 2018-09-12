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
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelRepository
{
    private static final Logger logger = LoggerFactory.getLogger(ModelRepository.class.getName());

    File repositoryRoot;
    Map<String, Entry> registerMap = new HashMap<>();

    public ModelRepository(File storeRootDirectory)
    {
        this.repositoryRoot = storeRootDirectory;
        RepositoryUtil.nullCheck(this.repositoryRoot);
        RepositoryUtil.createFileSystemLocation(this.repositoryRoot);
        logger.info("Create [" + ModelRepository.class.getSimpleName() + "] with root folder located at ["
                + this.repositoryRoot.getAbsolutePath() + "]");
    }

    public void addEntry(Entry entry, File sourceLocation, boolean deleteSource) throws IOException
    {
        registerMap.put(entry.getId(), entry);
        File target = FileSystemLocator.locate(repositoryRoot, entry);
        FileUtils.copyDirectory(sourceLocation, target);

        if (deleteSource) {
            FileUtils.deleteDirectory(sourceLocation);
        }
    }

    public void updateEntry(String id, long timestamp, File updatedModelExternalLocation,
            boolean deleteSource)
        throws IOException, InterruptedException
    {
        RepositoryUtil.nullCheck(updatedModelExternalLocation);

        Entry entry = registerMap.get(id);
        if (RepositoryUtil.isNull(entry)) {
            throw new IllegalStateException(
                    "Tried to update model with id [" + id + "], which did not exist");
        }

        logger.debug("Existing model found (id: [" + entry.toString() + "])");
        File pathToOldVersion = FileSystemLocator.locate(repositoryRoot, entry);

        entry.timestamp = timestamp;
        File pathToInternalLocation = FileSystemLocator.locate(repositoryRoot, entry);
        FileUtils.copyDirectory(updatedModelExternalLocation, pathToInternalLocation);

        FileUtils.deleteDirectory(pathToOldVersion);
        logger.info("Deleted old version [" + pathToOldVersion.getAbsolutePath() + "]");

        if (deleteSource) {
            logger.info("Deleted source version of new model originally located at ["
                    + updatedModelExternalLocation.getAbsolutePath() + "]");
            FileUtils.deleteDirectory(updatedModelExternalLocation);
        }

    }

    public List<String> getEntryIds()
    {
        return new ArrayList<String>(registerMap.keySet());
    }

    public Entry getEntry(String id)
    {
        Entry model = registerMap.get(id);
        return model;
    }

    void restoreSerializedEntry(Entry entry)
    {
        registerMap.put(entry.getId(), entry);
    }

    public void screenFolderAndLoad()
    {
        registerMap.clear();

        File[] files = repositoryRoot.listFiles(new FileFilter()
        {

            @Override
            public boolean accept(File pathname)
            {
                return pathname.isDirectory();
            }
        });

        StringBuilder sb = new StringBuilder();
        for (File f : files) {
            sb.append(f.getAbsolutePath() + ",");
        }
        logger.info("Root folder [" + repositoryRoot.getAbsolutePath() + "] contains [" + sb.toString()
                + "] folders");

        for (File file : files) {
            String name = file.getName();
            String id = FileSystemLocator.getId(name);
            long timeStamp = FileSystemLocator.getTimeStamp(name);
            registerMap.put(id, new Entry(id, timeStamp));

            logger.info("Loaded item with id: [" + id + "] named [" + file.getName()
                    + "] in root directory [" + repositoryRoot.getAbsolutePath() + "]");

        }
    }

    public File getFileSystemLocationOfEntry(String id)
    {
        RepositoryUtil.nullCheck(id);

        if (!registerMap.containsKey(id)) {
            throw new IllegalArgumentException("The id [" + id + "] is unknown");
        }
        

        return FileSystemLocator.locate(repositoryRoot, registerMap.get(id));
    }

}
