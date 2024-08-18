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

Find latest version [here](https://maven.latvian.dev/releases/dev/latvian/apps/tiny-java-server/maven-metadata.xml).

You can find a basic example [here](/src/test/java/dev/latvian/apps/tinyserver/test/TinyServerTest.java)