package net.kr9ly.doubler;

import net.kr9ly.doubler.injectors.SampleInjectors;
import net.kr9ly.doubler.repository.SampleDependentAssistedRepositoryBuilder;
import net.kr9ly.doubler.repository.SampleDependentRepository;
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
public class SampleModel {

    @Inject
    SampleRepository sampleRepository;

    @Inject
    SampleDependentRepository sampleDependentRepository;

    @Inject
    SampleDependentAssistedRepositoryBuilder sampleDependentAssistedRepositoryBuilder;

    public String getString() {
        return sampleRepository.getString();
    }

    public String getDependentString() {
        return sampleDependentRepository.getDependentString();
    }

    public String getDependentAssistedString() {
        return sampleDependentAssistedRepositoryBuilder.build("_suffix").getDependentAssistedString();
    }
}
