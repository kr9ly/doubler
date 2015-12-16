package net.kr9ly.doubler.component;

import dagger.Component;
import net.kr9ly.doubler.injectors.SampleInjectorsSupport;
import net.kr9ly.doubler.module.SampleModule;
import net.kr9ly.doubler.module.SampleModuleSupport;
import net.kr9ly.doubler.providers.SampleProvidersModule;
import net.kr9ly.doubler.providers.SampleProvidersModuleSupport;
import net.kr9ly.doubler.scope.SampleScope;

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
@SampleScope
@Component(modules = {SampleModule.class, SampleProvidersModule.class})
public interface SampleComponent extends SampleModuleSupport, SampleInjectorsSupport, SampleProvidersModuleSupport {
}
