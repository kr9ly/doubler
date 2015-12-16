# Doubler - Support Dagger2 Annotation Processing

[![Circle CI](https://circleci.com/gh/kr9ly/doubler/tree/master.svg?style=shield)](https://circleci.com/gh/kr9ly/doubler/tree/master)

# Usage

Add this to `repositories` block in your build.gradle

```
maven { url 'http://kr9ly.github.io/maven/' }
```

And Add this to `dependencies` block in your build.gradle

```
compile 'net.kr9ly:doubler:0.9.0'
apt 'net.kr9ly:doubler-compiler:0.9.0'
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
@dagger.Component(modules = {SampleModule.class})
public interface SampleComponent extends SampleInjectorsSupport {
    // this method contained within SampleInjectorsSupport(Auto-Generated).
    // void inject(SampleModel injectTo);
}
```
### Module Generation Support

```java
@SampleScope
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@net.kr9ly.doubler.ProvidersSupport
public @interface SampleProviders {
}
```

```java
@SampleProviders
public class SampleDependentRepository {

    // field injection
    @javax.inject.Inject
    SampleRepository sampleRepository;

    public String getDependentString() {
        return sampleRepository.getString() + "_dependent";
    }
}
```

```java
@SampleProviders
public class SampleDependentAssistedRepository {

    private SampleDependentRepository sampleFieldInjectRepository;

    private String suffix;

    // constructor injection
    @javax.inject.Inject
    public SampleDependentAssistedRepository(
            SampleDependentRepository sampleFieldInjectRepository,
            // support assisted injection
            @net.kr9ly.doubler.Assisted String suffix
    ) {
        this.sampleFieldInjectRepository = sampleFieldInjectRepository;
        this.suffix = suffix;
    }

    public String getDependentAssistedString() {
        return sampleFieldInjectRepository.getDependentString() + suffix;
    }
}
```

```java
@SampleScope
@dagger.Component(modules = {SampleProvidersModule.class})
public interface SampleComponent extends SampleProvidersModuleSupport {
    // this method contained within SampleProvidersModuleSupport(Auto-Generated).
    SampleDependentRepository sampleFieldInjectRepository();
    // you can inject Builder Class(Auto-Injected) also
    SampleDependentRepositoryBuilder sampleDependentRepositoryBuilder();
    // use SampleDependentAssistedRepositoryBuilder#build(String suffix) to get instance
    SampleDependentAssistedRepositoryBuilder sampleDependentAssistedRepositoryBuilder();
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