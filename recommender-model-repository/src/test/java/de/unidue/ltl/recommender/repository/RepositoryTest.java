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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.unidue.ltl.recommender.repository.Entry;
import de.unidue.ltl.recommender.repository.ModelRepository;

public class RepositoryTest
{
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    public TemporaryFolder modelDummyOne = new TemporaryFolder();
    public TemporaryFolder modelDummyTwo = new TemporaryFolder();

    ModelRepository repository;

    @Before
    public void setup() throws IOException
    {
        folder.create();
        modelDummyOne.create();
        modelDummyTwo.create();

        repository = new ModelRepository(folder.getRoot());
    }

    @Test
    public void testAddModel() throws IOException
    {
        Entry entry = new Entry("ABC", 232389);
        repository.addEntry(entry, modelDummyOne.getRoot(), true);
        entry = repository.getEntry("ABC");
        String id = entry.getId();
        long ts = entry.getTimeStamp();
        
        assertEquals("ABC", id);
        assertEquals(232389, ts);
        assertEquals(1, repository.getEntryIds().size());
        
        
        entry = new Entry("ABC-2", 5232389);
        id = entry.getId();
        ts = entry.getTimeStamp();
        repository.addEntry(entry, modelDummyTwo.getRoot(), true);
        
        assertEquals("ABC-2", id);
        assertEquals(5232389, ts);
        
        assertEquals(2, repository.getEntryIds().size());
    }
    
    @Test
    public void testOverwriteExistingModel() throws IOException, InterruptedException
    {
        Entry e = new Entry("ABC", 232389);
        repository.addEntry(e, modelDummyOne.getRoot(), true);
        
        repository.updateEntry(e.id, e.timestamp, modelDummyTwo.getRoot(), true);
        
    }

    @Test(expected = IllegalStateException.class)
    public void testExceptionOnUpdateNonExistingModel() throws IOException, InterruptedException
    {
        repository.updateEntry("ABC", 123456, modelDummyOne.getRoot(), true);
        List<String> modelIds = repository.getEntryIds();
        assertEquals(1, modelIds.size());

        Entry m = repository.getEntry("ABC");
        assertEquals("ABC", m.getId());
        assertEquals(123456, m.getTimeStamp());
    }

}