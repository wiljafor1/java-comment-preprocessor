/*
 * Copyright 2002-2019 Igor Maznitsa (http://www.igormaznitsa.com)
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.igormaznitsa.jcp.maven;

import com.igormaznitsa.jcp.context.PreprocessorContext;
import com.igormaznitsa.jcp.expression.Value;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

public final class PreprocessorMojoTest extends AbstractMojoTestCase {

  private static void assertArrayEqualsWithoutOrders(final Object[] array1, final Object[] array2) {
    final List<Object> list1 = new ArrayList<Object>(Arrays.asList(array1));
    final List<Object> list2 = new ArrayList<Object>(Arrays.asList(array2));

    while (!list1.isEmpty() && !list2.isEmpty()) {
      final Object list1obj = list1.get(0);
      for (int i = 0; i < list2.size(); i++) {
        if (list2.get(i).equals(list1obj)) {
          list2.remove(i);
          break;
        }
      }
      list1.remove(0);
    }

    assertTrue("Different values in arrays", list1.isEmpty() && list2.isEmpty());
  }

  @Before
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @After
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  @Test
  public void testConfiguration() throws Exception {
    final File testPom = new File(this.getClass().getResource("preprocessor_mojo_test_cfg.xml").toURI());
    assertTrue("Must be existing", testPom.exists());
    final PreprocessorMojo mojo = (PreprocessorMojo) lookupMojo("preprocess", testPom);
    assertNotNull("Must not be null", mojo);

    assertFalse(mojo.isSkip());
    mojo.setSkip(true);
    assertTrue(mojo.isSkip());

    final PreprocessorContext context = mojo.makePreprocessorContext("/");

    assertEquals("/", context.getSourceDirectories());
    assertEquals("destination_dir", context.getDestinationDirectoryAsFile().getName());
    assertArrayEqualsWithoutOrders(new String[] {"xml", "html"}, context.getExcludedFileExtensions());
    assertArrayEqualsWithoutOrders(new String[] {"java", "txt"}, context.getProcessingFileExtensions());
    assertEquals("UTF-16", context.getInCharacterEncoding());
    assertEquals("UTF-32", context.getOutCharacterEncoding());
    assertTrue("Must be true", context.isRemoveComments());
    assertTrue("Must be true", context.isVerbose());
    assertTrue("Must be true", context.isFileOutputDisabled());
    assertTrue("Must be true", context.doesClearDestinationDirBefore());
    assertTrue("Must be true", context.isKeepLines());
    assertTrue("Must be true", context.isCareForLastNextLine());
    assertTrue("Must be true", context.isCompareDestination());
    assertTrue("Must be true", context.isAllowWhitespace());
    assertTrue("Must be true", context.isPreserveIndent());
    assertTrue("Must be true", context.isCopyFileAttributes());
    assertTrue("Must be true", context.isUnknownVariableAsFalse());

    assertArrayEquals(new String[] {".git", ".hg", "**/.cvs", "c:/hello/**/world"}, context.getExcludedFolderPatterns());

    final File[] cfgfiles = context.getConfigFiles();
    assertEquals("Must be two", 2, cfgfiles.length);
    assertEquals("Must be test1.cfg", "test1.cfg", cfgfiles[0].getName());
    assertEquals("Must be test2.cfg", "test2.cfg", cfgfiles[1].getName());

    assertEquals("Must be 3", Value.INT_THREE, context.findVariableForName("globalvar1", true));
    assertEquals("Must be 'hello world'", Value.valueOf("hello world"), context.findVariableForName("globalvar2", true));
  }
}