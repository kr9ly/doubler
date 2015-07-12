package net.kr9ly.doubler;

import net.kr9ly.doubler.component.DaggerSampleComponent;
import net.kr9ly.doubler.component.SampleComponent;
import net.kr9ly.doubler.module.SampleModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

/**
 * Copyright 2015 kr9ly
 * <br />
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <br />
 * http://www.apache.org/licenses/LICENSE-2.0
 * <br />
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@RunWith(JUnit4.class)
public class DoublerProcessorTest {

    @Test
    public void testDoublerModuleGenerate() {
        SampleComponent component = DaggerSampleComponent.builder()
                .sampleModule(new SampleModule())
                .build();

        SampleModel sampleModel = new SampleModel();
        component.inject(sampleModel);

        assertEquals("sample", sampleModel.getString());
        assertEquals("sample_dependent", sampleModel.getDependentString());
        assertEquals("sample_dependent_suffix", sampleModel.getDependentAssistedString());
    }
}
