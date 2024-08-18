# Tiny Java Server

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
  implementation "dev.latvian.apps:tiny-java-server:$server_version"
}
```

Find the latest version [here](https://maven.latvian.dev/releases/dev/latvian/apps/tiny-java-server/maven-metadata.xml)

Basic example [here](/src/test/java/dev/latvian/apps/tinyserver/test/TinyServerTest.java)