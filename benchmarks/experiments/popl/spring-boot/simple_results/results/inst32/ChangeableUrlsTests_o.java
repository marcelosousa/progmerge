/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.devtools.restart;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.springframework.util.StringUtils;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link ChangeableUrls}.
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 */
public class ChangeableUrlsTests {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
	public void folderUrl() throws Exception {
		URL url = makeUrl("myproject");
		assertThat(ChangeableUrls.fromUrls(url).size(), equalTo(1));
	}

	@Test
	public void fileUrl() throws Exception {
		URL url = this.temporaryFolder.newFile().toURI().toURL();
		assertThat(ChangeableUrls.fromUrls(url).size(), equalTo(0));
	}

	@Test
	public void httpUrl() throws Exception {
		URL url = new URL("http://spring.io");
		assertThat(ChangeableUrls.fromUrls(url).size(), equalTo(0));
	}

	@Test
	public void skipsUrls() throws Exception {
		ChangeableUrls urls = ChangeableUrls.fromUrls(makeUrl("spring-boot"),
				makeUrl("spring-boot-autoconfigure"), makeUrl("spring-boot-actuator"),
				makeUrl("spring-boot-starter"),
				makeUrl("spring-boot-starter-some-thing"));
		assertThat(urls.size(), equalTo(0));
	}

	@Test
	public void urlsFromJarClassPathAreConsidered() throws Exception {
		URL projectCore = makeUrl("project-core");
		URL projectWeb = makeUrl("project-web");
		ChangeableUrls urls = ChangeableUrls.fromUrlClassLoader(new URLClassLoader(
				new URL[] { makeJarFileWithUrlsInManifestClassPath(projectCore,
						projectWeb) }));
		assertThat(urls.toList(), contains(projectCore, projectWeb));
	}

	private URL makeUrl(String name) throws IOException {
		File file = this.temporaryFolder.newFolder();
		file = new File(file, name);
		file = new File(file, "target");
		file = new File(file, "classes");
		file.mkdirs();
		return file.toURI().toURL();
	}

	private URL makeJarFileWithUrlsInManifestClassPath(URL... urls) throws Exception {
		File classpathJar = this.temporaryFolder.newFile("classpath.jar");
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().putValue(Attributes.Name.MANIFEST_VERSION.toString(),
				"1.0");
		manifest.getMainAttributes().putValue(Attributes.Name.CLASS_PATH.toString(),
				StringUtils.arrayToDelimitedString(urls, " "));
		new JarOutputStream(new FileOutputStream(classpathJar), manifest).close();
		return classpathJar.toURI().toURL();
	}

}