package net.kr9ly.doubler.dynamic;

import net.kr9ly.doubler.CustomProvider;
import net.kr9ly.doubler.repository.SampleFieldInjectRepository;
import net.kr9ly.doubler.repository.SampleInterface1;
import net.kr9ly.doubler.repository.SampleInterface2;

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
public class SampleCustomProvider implements CustomProvider {
    @Override
    @SuppressWarnings("unchecked")
    public <T> T provide(Class<T> provideClass) {
        if (provideClass.equals(SampleFieldInjectRepository.class)) {
            return (T) new SampleFieldInjectRepository();
        } else if (provideClass.equals(SampleInterface1.class)) {
            return (T) new SampleInterface1() {
                @Override
                public String getString() {
                    return "interface1";
                }
            };
        } else if (provideClass.equals(SampleInterface2.class)) {
            return (T) new SampleInterface2() {
                @Override
                public String getString() {
                    return "interface2";
                }
            };
        }
        return null;
    }
}
