import java.io.FileInputStream
import java.util.Properties

plugins {
	id("base")
	id("biz.aQute.bnd.builder") version "7.4.0"
	id("com.diffplug.spotless") version "8.10.2"
	id("eclipse")
	id("java")
	id("signing")
}

tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
}

val buildDir = file(".")
val props = Properties()
props.load(FileInputStream("${buildDir.getAbsolutePath()}/src/main/resources/com/neuronrobotics/nrjavaserial/build.properties"))

group = "com.neuronrobotics"
base.archivesName = props["app.name"] as String
version = props["app.version"] as String

sourceSets {
	test {
		java {
			srcDirs("test/src")
		}
	}
	main {
		resources {
			srcDirs("src/main/resources", "src/main/c/resources")
			include("**/*.so", "**/*.dll", "**/*.jnilib", "**/*.properties")
		}
	}
}

repositories {
	mavenCentral()
}

dependencies {
	testImplementation("junit:junit:4.12")
	implementation("commons-net:commons-net:3.9.0")
	compileOnly("net.java.dev.jna:jna:4.4.0")
	compileOnly("net.java.dev.jna:jna-platform:4.4.0")
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(8)
	}

	withJavadocJar()
	withSourcesJar()
}

// Matches both org.gradle.api.tasks.Copy and org.gradle.jvm.tasks.Jar.
tasks.withType<AbstractCopyTask> {
	duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.jar {
	bundle {
		bnd(mapOf(
			"Specification-Title" to props["app.name"],
			"Specification-Version" to props["app.version"],
			"Specification-Vendor" to "Commonwealth Robotics Cooperative",
			"Implementation-Title" to props["app.name"],
			"Implementation-Version" to props["app.version"],
			"Implementation-Vendor" to "Commonwealth Robotics Cooperative",
			"Import-Package" to listOf(
				"com.sun.jna.platform.win32;resolution:=optional",
				"org.apache.commons.net.telnet;resolution:=optional",
				"!gnu.io*",
				"*",
			).joinToString(","),
			"Export-Package" to "gnu.io*",
		))
	}
}

// withSourcesJar() creates the sourcesJar task which, by default, packages
// sourceSets.main.allSource. Including native libraries in that source set is
// the easiest way to include them the final library archive, but we don't want
// an extra half meg of binaries to be included in the source archive.
tasks.named<Jar>("sourcesJar") {
	exclude("native/")
}

spotless {
	isEnforceCheck = false
	ratchetFrom = "origin/master"

	format("misc") {
		target("*.gradle")

		trimTrailingWhitespace()
		leadingSpacesToTabs()
		endWithNewline()
	}

	java {
		importOrder()
		removeUnusedImports()
		eclipse()
	}
}

// You can provide a signatory in three ways. Either:
//
// 1. Pass the signatory properties – signing.keyId, signing.secretKeyRingFile,
//    and signing.password – on the command line:
//
//     ./gradlew ... \
//         -Psigning.keyId=key-id \
//         -Psigning.secretKeyRingFile=/path/to/.gnupg/secring.gpg \
//         -Psigning.password=secret
//
// 2. Configure those properties in ~/.gradle/gradle.properties.
// 3. Set the SIGNING_KEY and SIGNING_PASSWORD environment variables to an
//    ASCII-armored PGP key and password, respectively.
//
// For more details on the behaviour of the properties and their expected
// values, see the signing plugin documentation:
//
//     https://docs.gradle.org/current/userguide/signing_plugin.html#sec:signatory_credentials
//
// If a signatory has not been configured, or if the signing.skip property is
// set, artifacts will not be signed prior to publication.
//
// This signing block must appear after the publishing block in order to refer
// to the specific publication to be signed.
signing {
	if (project.hasProperty("signing.skip")) {
		// We've been explicitly told not to sign artifacts, even if we have a
		// configured signatory.
	} else if (System.getenv("SIGNING_KEY") != null
		&& System.getenv("SIGNING_PASSWORD") != null) {
		useInMemoryPgpKeys(
			System.getenv("SIGNING_KEY"),
			System.getenv("SIGNING_PASSWORD"))

		sign(configurations.archives.get())
	} else if (findProperty("signing.keyId") != null
		&& findProperty("signing.secretKeyRingFile") != null
		&& findProperty("signing.password") != null) {
		// No special configuration necessary for properties: the signing
		// plugin self-configures from the signing.keyId,
		// signing.secretKeyRingFile, and signing.password properties when
		// populated.

		sign(configurations.archives.get())
	} else {
		// No signatory is configured; skip signing.
	}
}

/*
artifacts {
	archives javadocJar
	archives sourcesJar
	archives jar
}

uploadArchives {
	repositories {
		mavenDeployer {
			beforeDeployment { MavenDeployment deployment -> signing.signPom(deployment) }

		repository(url: "https://oss.sonatype.org/service/local/staging/deploy/maven2/") {
			authentication(userName: ossrhUsername, password: ossrhPassword)
		}

		snapshotRepository(url: "https://oss.sonatype.org/content/repositories/snapshots/") {
			authentication(userName: ossrhUsername, password: ossrhPassword)
		}


			pom.project {
				name 'NRJavaSerial'
				packaging 'jar'
				description 'A fork of the RXTX library with a focus on ease of use and embeddability in other libraries.'
				url 'http://neuronrobotics.com'

				scm {
					connection			'scm:git:https://github.com/NeuronRobotics/nrjavaserial.git'
					developerConnection	'scm:git:git@github.com:NeuronRobotics/nrjavaserial.git'
					url					'https://github.com/NeuronRobotics/nrjavaserial'
				}

				licenses {
					license {
						name	'The Apache License, Version 2.0'
						url		'http://www.apache.org/licenses/LICENSE-2.0.txt'
					}
				}

				developers {
					developer {
						id		'madhephaestus'
						name	'Kevin Harrington'
						email	'kharrington@neuronrobotics.com'
					}
				}
			}
		}
	}
}
*/
