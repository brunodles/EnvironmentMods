# EnvironmentMods

A library to help to manage *environment aware dependencies*.
With this lib you can add a dependency only on specific *build type*

## Why?
Have you ever used some lib that should on be installed only on one environment / build type?
Something libraries like:
* [Stetho](http://facebook.github.io/stetho/) - debug only
* [Crashlytics](https://fabric.io/kits/android/crashlytics) - release only
* [Fabric](https://get.fabric.io/android) - debug setup
* [Firebase](https://www.firebase.com/) - debug setup
* [Picasso](https://github.com/square/picasso) - debug setup
* [Okhttp](https://github.com/square/okhttp) - debug setup
* [Flurry](https://developer.yahoo.com/flurry/docs/integrateflurry/android/) - release only
* [AndroidDevMetrics](https://github.com/frogermcs/AndroidDevMetrics) - debug only
* and many others...

All these libraries need to execute some "install script" on `Application.onCreate`, but manage all these
installs always get a mess when you have multiple libraries.


## All libraries need that?
Some libraries provides a *no-op* version, that does nothing.
Some of then:
* [Leakcanary](https://github.com/square/leakcanary) - [noop](https://github.com/square/leakcanary#getting-started)
* [Log4J](http://logging.apache.org/log4j/2.x/) - this lib have a noOp version
* [Android-DebugPort](https://github.com/jasonwyatt/Android-DebugPort) - [noop](https://github.com/jasonwyatt/Android-DebugPort-NOOP)
* and many others...

We don't need to worry about these ones that provide a noop version.

Our gradle file will be like:
```gradle
 dependencies {
   debugCompile 'group:library:version'
   releaseCompile 'group:library-no-op:version'
 }
```


## Debug Application?
Some people write a whole new Application class for **each** environment and change the `debug/AndroidManifest.xml` to replace
 the Application class using `tools:replace="android:name"`.

Take a look on [the suggested solution from **Stetho**](https://github.com/facebook/stetho/blob/master/stetho-sample/src/debug/AndroidManifest.xml).

This works but we can have duplicated code.

## Mod Helper Classes
I started to write helper classes to solve these problems, but my solution introduced another bug.
When I write a helper class on **debug** I have to write a similar class on **release**, and these classes should
 have the same interface (I mean the same declared methods).

My project structure gets like
```
App Module
 |- main
 |   |- Application
 |
 |- debug
 |   | - ApplicationMod
 |
 |- release
 |   |- ApplicationMod
 |
```

This is also a mess, now I can split the Application class but I can introduce a new bug if I change some parameter or
 method name on the `ApplicationMod` class.

## What this library does?
It generates the `AppicationMods` class for you, but not only that.
I was inspired by how the *DataBinding library* works and then made this library works like the `@BindingAdapter`.
Take a look on [this link](https://developer.android.com/reference/android/databinding/BindingAdapter.html) if you don't
know what I'm talking about.

You can write many methods, on any class they just need to be `public static` and contains the annotation `@ModFor`
passing the wanted class as parameter.
Like this:
```java
@ModFor(Application.class)
public static void addStetho(Application application){
    // TODO add stetho setup here.
}
```

This will generate a class named `ApplicationMods` that contains only one method `apply`.
You can call it inside your `Application.onCreate` method.

```java
@Moddable
public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ApplicationMods.apply(this);
    }
}
```

## What is this @Moddable annotation?
This is just a annotation that indicates to generate a Mod class (`ApplicationMods` in this case)
even if there's no mod for then on the current environment.
This will prevent a class not found error. This also helps the library to not use Reflection, without this annotation
we would need to find the class using reflection, which is bad for performance.

## How to include
First check if your project have *android apt*, if you don't have you can download it
[here](https://bitbucket.org/hvisser/android-apt).

In your *module level gradle file* `root/app/build.gradle`, add these dependencies.
```gradle
dependencies {
    ...
    // old style
    provided 'com.brunodles:environmentmods-annotation:{latest version}'
    apt 'com.brunodles:environmentmods-processor:{latest version}'
    
    // new style
    api 'com.brunodles:environmentmods-annotation:{latest version}'
    annotationProcessor 'com.brunodles:environmentmods-processor:{latest version}'
    
    // add this line too if you use kotlin
    kapt 'com.brunodles:environmentmods-processor:{latest version}'
}
```

## Other
While I wrote this README file I found
[this article](https://medium.com/@orhanobut/no-op-versions-for-dev-tools-b0a865934398#.vjp39sfas)
that explain how to write a Debug Application.
