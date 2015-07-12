# Doubler - Support Dagger2 Annotation Processing

[![Circle CI](https://circleci.com/gh/kr9ly/doubler/tree/master.svg?style=shield)](https://circleci.com/gh/kr9ly/doubler/tree/master)

# Usage

Add this to `repositories` block in your build.gradle

```
maven { url 'http://kr9ly.github.io/maven/' }
```

And Add this to `dependencies` block in your build.gradle

```
apt 'net.kr9ly:doubler:0.0.1'
```

### Auto Module Expose Support

```java
@dagger.Module
public class SampleModule {

    @dagger.Provides
    SampleRepository provideSampleRepository() {
        return new SampleRepository();
    }
}
```

```java
@dagger.Component(modules = {SampleModule.class})
public interface SampleComponent extends SampleModuleSupport {
    // this method contained within SampleModuleSupport(Auto-Generated).
    // SampleRepository sampleRepository();
}
```

### Auto Injector Support

```java
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@net.kr9ly.doubler.InjectorsSupport
public @interface SampleInjectors {
}
```

```java
@SampleInjectors
public class SampleModel {

    @javax.inject.Inject
    SampleRepository sampleRepository;

    public String getString() {
        return sampleRepository.getString();
    }
}
```

```java
@Component(modules = {SampleModule.class})
public interface SampleComponent extends SampleInjectorsSupport {
    // this method contained within SampleInjectorsSupport(Auto-Generated).
    // void inject(SampleModel injectTo);
}
```

# License

```
Copyright 2015 kr9ly

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```