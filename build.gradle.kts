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
/*
signing {
	required {
		gradle.taskGraph.hasTask("uploadArchives")
	}
	sign configurations.archives
}

artifacts {
	archives javadocJar
	archives sourcesJar
	archives jar
}

//import org.gradle.plugins.signing.Sign
//
//gradle.taskGraph.whenReady { taskGraph ->
//	if (taskGraph.allTasks.any { it instanceof Sign }) {
//		// Use Java 6's console to read from the console (no good for
//		// a CI environment)
//		Console console = System.console()
//		console.printf "\n\nWe have to sign some things in this build." +
//					   "\n\nPlease enter your signing details.\n\n"
//
//		def id = console.readLine("PGP Key Id: ")
//		def file = console.readLine("PGP Secret Key Ring File (absolute path): ")
//		def password = console.readPassword("PGP Private Key Password: ")
//
//		allprojects { ext."signing.keyId" = id }
//		allprojects { ext."signing.secretKeyRingFile" = file }
//		allprojects { ext."signing.password" = password }
//
//		console.printf "\nThanks.\n\n"
//	}
//}

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
