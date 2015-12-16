package net.kr9ly.doubler;

import net.kr9ly.doubler.injectors.SampleInjectors;
import net.kr9ly.doubler.repository.SampleInterface1;
import net.kr9ly.doubler.repository.SampleInterface2;
import net.kr9ly.doubler.repository.SampleRepository;

import javax.inject.Inject;

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
@SampleInjectors
public class SampleModel2 {

    @Inject
    SampleRepository sampleRepository;

    @Inject
    SampleInterface1 sampleInterface1;

    @Inject
    SampleInterface2 sampleInterface2;

    public String getString() {
        return sampleRepository.getString();
    }

    public String getInterface1String() {
        return sampleInterface1.getString();
    }

    public String getInterface2String() {
        return sampleInterface2.getString();
    }
}
