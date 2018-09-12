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
package de.unidue.ltl.recommender.server;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:test.properties")
public class RequestControllerTest
{

    private MockMvc mockMvc;

    @Autowired
    private RequestController controllerToTest;

    final String BASE_URL = "http://localhost:8080/";

    @Before
    public void setup()
    {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controllerToTest).build();
    }

    @Test
    public void trainRequest() throws Exception
    {
        controllerToTest = mock(RequestController.class);

        String trainRequest = FileUtils.readFileToString(new File(
                "src/test/resources/jsonTrainRequestV2small.txt"),
                "utf-8");

        mockMvc.perform(MockMvcRequestBuilders.post("/train")
                 .accept(MediaType.APPLICATION_JSON)
                 .contentType(MediaType.APPLICATION_JSON_VALUE)
                 .content(trainRequest))
                 .andExpect(MockMvcResultMatchers.status()
                 .isOk());
    }
    
    @Test
    public void predictRequest() throws Exception
    {
        
        String predictRequest = FileUtils
                .readFileToString(new File("src/test/resources/jsonPredictRequestV2small.txt"), "utf-8");

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/predict")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(predictRequest))
                .andExpect(MockMvcResultMatchers.status()
                .isOk())
                .andReturn();
        
        String contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.startsWith("[ \"<?xml version"));
    }

}
