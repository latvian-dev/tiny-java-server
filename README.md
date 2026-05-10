# Tiny Java HTTP Server

Gradle dependency:

```groovy
repositories {
  maven {
    url "https://maven.latvian.dev/releases"
    content {
      includeGroup "dev.latvian.apps"
    }
  }
}

dependencies {
  implementation "dev.latvian.apps:tiny-http:$server_version"
}
```

Find the latest version [here](https://maven.latvian.dev/releases/dev/latvian/apps/tiny-http/maven-metadata.xml)

Basic example [here](/src/test/java/dev/latvian/apps/tinyhttp/test/TinyServerTest.java)
