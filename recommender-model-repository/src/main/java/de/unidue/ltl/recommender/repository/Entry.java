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

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Entry
{
    private static final Logger logger = LoggerFactory.getLogger(Entry.class.getName());
    
    private AtomicInteger modelAccesses = new AtomicInteger(0);
    private Semaphore modelUpdateOperation = new Semaphore(1, true);
    long timestamp;
    String id;
    

    public Entry(String modelId, long timestamp)
    {
        this.id = modelId;
        this.timestamp = timestamp;
    }

    public synchronized void updateTimeStamp(long timestamp) throws InterruptedException, IOException
    {
        logger.debug("Update on model with id [" + this.id + "] current read accesses ["
                + modelAccesses.get() + "]");
        modelUpdateOperation.acquire();

        this.timestamp = timestamp;

        modelUpdateOperation.release();
    }

    public synchronized void beginReadAccess() throws InterruptedException
    {

        logger.debug("Model update is taking place ["
                + (modelUpdateOperation.availablePermits() == 0 ? "true" : "false") + "]");
        if (modelUpdateOperation.tryAcquire()) {
            modelAccesses.incrementAndGet();
            logger.debug("Read access on model [" + id + "]- new access count ["
                    + modelAccesses.get() + "]");
            modelUpdateOperation.release();
        }
        else {
            logger.debug("Update of model [" + id + "] in progress");
            wait();
            beginReadAccess();
        }

    }

    public synchronized void endReadAccess()
    {
        modelAccesses.decrementAndGet();
        logger.debug("Decrease model access counter to [" + modelAccesses.get() + "]");
        notify();
    }

    public long getTimeStamp()
    {
        return timestamp;
    }

    public String getId()
    {
        return id;
    }

    Integer getNumberOfModelAccesses()
    {
        return modelAccesses.get();
    }

    public String toString()
    {
        return "[" + id + "] / [" + timestamp + "]";
    }

}
