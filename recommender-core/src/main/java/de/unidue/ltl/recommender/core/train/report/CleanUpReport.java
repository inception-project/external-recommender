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

package de.unidue.ltl.recommender.core.train.report;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.dkpro.lab.storage.StorageService;
import org.dkpro.tc.ml.report.TcAbstractReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// tracks created folders in order to delete them after the run
public class CleanUpReport
    extends TcAbstractReport
{
    
    private static final Logger logger = LoggerFactory
            .getLogger(CleanUpReport.class.getName());
    
    @Override
    public void execute() throws Exception
    {
        StorageService ss = getContext().getStorageService();

        Set<String> taskIds = getTaskIdsFromMetaData(getSubtasks());
        List<String> allIds = new ArrayList<>();
        allIds.addAll(collectTasks(taskIds));
        for (String id : taskIds) {
            File context = ss.locateKey(id, "");
            context.deleteOnExit();
            logger.debug("Marked temporary folder for deletionOnExit(): [" + context.getAbsolutePath() + "]");
        }
    }

}
