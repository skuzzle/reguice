[![Build Status](https://travis-ci.org/skuzzle/reguice.svg?branch=master)](https://travis-ci.org/skuzzle/reguice) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.skuzzle.inject/reguice/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.skuzzle.inject/reguice)
[![Coverage Status](https://coveralls.io/repos/skuzzle/reguice/badge.svg?branch=master&service=github)](https://coveralls.io/github/skuzzle/reguice?branch=master)
# reguice

... allows you to simply bind resources for injecting them with Google's Guice. 

Features:
* Bind from several sources (class path, file system, URL, web application context)
* Bind several content types (String, properties, json, xml and custom)
* Bind properties directly to a Java interface
* Allow reloading of bound Objects by injecting a `Provider`
* Fluent API for configuring the binding

## Maven artifact
reguice is available from Maven Central:
```xml
<dependency>
    <groupId>de.skuzzle.inject</groupId>
    <artifactId>reguice</artifactId>
    <version>0.3.0</version>
</dependency>
```

## Quickstart

The fluent API is accessible through the `Resources` class and may be used in your Guice 
`Module`. You can find some additional examples of how to bind resources 
[here](https://github.com/skuzzle/reguice/blob/master/src/it/java/de/skuzzle/inject/conf/BindingIntegrationTest.java).
The following example will bind the content of the MANIFEST.MF to a String 
annotated with `@Named("manifestContent")`.

```java
@Override
public void configure() {
    Resources.bind()
            .classPathResource("META-INF/MANIFEST.mf")
            .encodedWith(StandardCharset.UTF_8)
            .containingText()
            .to(Key.get(String.class, Names.named("manifestContent")))
            .in(Singleton.class)
            .using(binder());
}
```
Please note that the last call of the fluently chained methods must always be 
to `using(binder())`.

The text content of the file can automatically be mapped to a Manifest (provided by 
`java.util.jar`) instance by implementing your own TextContentType:

```java
public class ManifestContent implements TextContentType {

    @Override
    public <T> T createInstance(Class<T> type, TextResource resource) {
        checkArgument(Manifest.class.isAssignableFrom(type));
        try (InputStream in = resource.openBinaryStream()) {
            final Manifest mf = new Manifest();
            mf.read(in);
            return type.cast(mf);
        } catch (final IOException e) {
            throw new ProvisionException("Error while reading manifest", e);
        }
    }
}
```

The resource can then be bound to `Manifest.class` like:

```java
@Override
public void configure() {
    Resources.bind()
            .classPathResource("META-INF/MANIFEST.mf")
            .encodedWith(StandardCharset.UTF_8)
            .containing(new ManifestContent())
            .to(Manifest.class)
            .in(Singleton.class)
            .using(binder());
}
```

You can also bind properties from a `.properties` file directly to getter methods of an 
interface.

```
threadCount=4
dataFolder=data/
```

```java
public interface AppSettings {
    int getThreadCount();
    String getDataFolder();
}
```

```java
@Override
public void configure() {
    Resources.bind()
            .buffered()
            .classPathResource("config/appSettings.properties")
            .encodedWith(StandardCharset.UTF_8)
            .containingProperties()
            .to(AppSettings.class)
            .using(binder());
}
```

In this example, _reguice_ will automatically create an implementation of the provided 
interface and map its getters to the properties of the specified resource.

The same will work for json content type too.
