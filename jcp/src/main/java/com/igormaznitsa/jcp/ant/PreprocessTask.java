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

package com.igormaznitsa.jcp.ant;

import com.igormaznitsa.jcp.JcpPreprocessor;
import com.igormaznitsa.jcp.context.PreprocessorContext;
import com.igormaznitsa.jcp.context.SpecialVariableProcessor;
import com.igormaznitsa.jcp.exceptions.PreprocessorException;
import com.igormaznitsa.jcp.expression.Value;
import com.igormaznitsa.jcp.logger.PreprocessorLogger;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.meta.common.utils.GetUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static com.igormaznitsa.meta.common.utils.Assertions.assertNotNull;

/**
 * The class implements an ANT task to allow calls for preprocessing from ANT build scripts. Also it allows to out messages from preprocessor directives into the ANT log and read
 * ANT properties as global variables (with the "ant." prefix)
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class PreprocessTask extends Task implements PreprocessorLogger, SpecialVariableProcessor {
  private Sources sources = null;
  private String eol = null;
  private boolean keepAttributes = false;
  private String target = null;
  private String sourceEncoding = null;
  private String targetEncoding = null;
  private boolean ignoreMissingSources = false;
  private ExcludeExtensions excludeExtensions = null;
  private Extensions extensions = null;
  private boolean unknownVarAsFalse = false;
  private boolean dryRun = false;
  private boolean verbose = false;
  private boolean clearTarget = false;
  private boolean careForLastEol = false;
  private boolean keepComments = false;
  private Vars vars = null;
  private ExcludeFolders excludeFolders = null;
  private ConfigFiles configFiles = null;
  private boolean keepLines = true;
  private boolean allowWhitespace = false;
  private boolean preserveIndents = false;
  private boolean dontOverwriteSameContent = false;
  private Map<String, Value> antVariables = new HashMap<>();

  private void registerConfigFiles(@Nonnull final PreprocessorContext context) {
    if (this.getConfigFiles() != null) {
      for (final Sources.Path f : this.getConfigFiles().getPaths()) {
        log("Registering config file: " + f.getValue());
        context.registerConfigFile(assertNotNull("File must not be null", new File(f.getValue().trim())));
      }
    }
  }

  private void fillGlobalVars(@Nonnull final PreprocessorContext context) {
    if (this.getVars() != null) {
      for (final Vars.Var g : this.getVars().getVars()) {
        context.setGlobalVariable(assertNotNull("Name must not be null", g.getName()), Value.recognizeRawString(assertNotNull("Value must not be null", g.getValue())));
      }
    }
  }

  @Nonnull
  PreprocessorContext makePreprocessorContext() {
    fillAntVariables();

    final PreprocessorContext context = new PreprocessorContext(getProject().getBaseDir());
    context.setPreprocessorLogger(this);
    context.registerSpecialVariableProcessor(this);

    if (this.getTarget() != null) {
      context.setTarget(new File(this.getTarget()));
    }

    if (this.getSources() != null) {
      context.setSources(this.getSources().getPaths()
          .stream()
          .map(Sources.Path::getValue)
          .collect(Collectors.toList())
      );
    }

    if (this.getExcludeExtensions() != null) {
      context.setExcludeExtensions(this.getExcludeExtensions().extensions
          .stream()
          .map(x -> x.name.trim())
          .filter(x -> !x.isEmpty())
          .collect(Collectors.toList())
      );
    }
    if (this.getExtensions() != null) {
      context.setExtensions(this.getExtensions().extensions
          .stream()
          .map(x -> x.name.trim())
          .filter(x -> !x.isEmpty())
          .collect(Collectors.toList())
      );
    }

    if (this.getSourceEncoding() != null) {
      context.setSourceEncoding(Charset.forName(this.getSourceEncoding()));
    }

    if (this.getTargetEncoding() != null) {
      context.setTargetEncoding(Charset.forName(this.getTargetEncoding()));
    }

    context.setDontOverwriteSameContent(this.isDontOverwriteSameContent());
    context.setClearTarget(this.isClearTarget());
    context.setDryRun(this.isDryRun());
    context.setKeepComments(this.isKeepComments());
    context.setVerbose(this.isVerbose());
    context.setKeepLines(this.isKeepLines());
    context.setCareForLastEol(this.isCareForLastEol());
    context.setAllowWhitespace(this.isAllowWhitespace());
    context.setPreserveIndents(this.isPreserveIndents());
    context.setKeepAttributes(this.isKeepAttributes());
    context.setUnknownVariableAsFalse(this.isUnknownVarAsFalse());

    if (this.getEol() != null) {
      context.setEol(StringEscapeUtils.unescapeJava(this.getEol()));
    }

    if (this.getExcludeFolders() != null) {
      context.setExcludeFolders(
          this.getExcludeFolders().getFolders()
              .stream()
              .map(ExcludeFolders.Folder::getPath)
              .collect(Collectors.toList())
      );
    }

    this.registerConfigFiles(context);
    this.fillGlobalVars(context);

    return context;
  }

  @Override
  public void execute() throws BuildException {
    PreprocessorContext context;
    JcpPreprocessor preprocessor;

    this.antVariables.clear();
    this.antVariables.putAll(fillAntVariables());

    try {
      context = makePreprocessorContext();
    } catch (Exception unexpected) {
      final PreprocessorException pp = PreprocessorException.extractPreprocessorException(unexpected);
      throw new BuildException(pp == null ? unexpected.getMessage() : pp.toString(), pp == null ? unexpected : pp);
    }

    preprocessor = new JcpPreprocessor(context);

    try {
      preprocessor.execute();
    } catch (Exception unexpected) {
      final PreprocessorException pp = PreprocessorException.extractPreprocessorException(unexpected);
      throw new BuildException(pp == null ? unexpected.getMessage() : pp.toString(), pp == null ? unexpected : pp);
    }
  }

  @Override
  public void error(@Nullable final String message) {
    log(message, Project.MSG_ERR);
  }

  @Override
  public void info(@Nullable final String message) {
    log(message, Project.MSG_INFO);
  }

  @Override
  public void debug(@Nullable final String message) {
    log(message, Project.MSG_DEBUG);
  }

  @Override
  public void warning(@Nullable final String message) {
    log(message, Project.MSG_WARN);
  }

  @Nonnull
  private Map<String, Value> fillAntVariables() {
    final Project theProject = getProject();

    final Map<String, Value> result;

    if (theProject == null) {
      result = Collections.emptyMap();
    } else {
      result = new HashMap<>();

      for (final Object key : getProject().getProperties().keySet()) {
        final String keyStr = key.toString();
        final String value = theProject.getProperty(keyStr);
        if (value != null) {
          result.put("ant." + keyStr.toLowerCase(Locale.ENGLISH), Value.valueOf(value));
        }
      }
    }
    return result;
  }

  @Override
  @Nonnull
  @MustNotContainNull
  public String[] getVariableNames() {
    String[] result;

    if (antVariables == null) {
      result = new String[0];
    } else {
      result = antVariables.keySet().toArray(new String[0]);
    }

    return result;
  }

  @Override
  @Nonnull
  public Value getVariable(@Nonnull final String varName, @Nonnull final PreprocessorContext context) {
    if (antVariables == null) {
      throw context.makeException("Non-initialized ANT property map detected", null);
    }
    final Value result = this.antVariables.get(varName);

    if (result == null) {
      throw context.makeException("Request for unsupported Ant property \'" + varName + '\'', null);
    }
    return result;
  }

  @Override
  public void setVariable(@Nonnull final String varName, @Nonnull final Value value, @Nonnull final PreprocessorContext context) {
    throw context.makeException("Request to change ANT property \'" + varName + "\'. NB! ANT properties are read only!", null);
  }

  @Nonnull
  public Extensions createExtensions() {
    this.extensions = new Extensions();
    return this.extensions;
  }

  @Nonnull
  public ExcludeExtensions createExcludeExtensions() {
    this.excludeExtensions = new ExcludeExtensions();
    return this.excludeExtensions;
  }

  @Nonnull
  public Sources createSources() {
    this.sources = new Sources();
    return this.sources;
  }

  @Nonnull
  public ConfigFiles createConfigFiles() {
    this.configFiles = new ConfigFiles();
    return this.configFiles;
  }

  @Nonnull
  public Vars createVars() {
    this.vars = new Vars();
    return this.vars;
  }

  @Data
  @EqualsAndHashCode(callSuper=false)
  public static class Sources {

    protected List<Path> paths = new ArrayList<>();

    @Nonnull
    public Path createPath() {
      final Path result = new Path();
      paths.add(result);
      return result;
    }

    @Data
    public static class Path {
      private String value = "";

      public void addText(@Nullable final String text) {
        this.value += GetUtils.ensureNonNull(text, "");
      }
    }

  }

  @Data
  @EqualsAndHashCode(callSuper=false)
  public static class ConfigFiles extends Sources {

  }

  @Data
  @EqualsAndHashCode(callSuper=false)
  public static class ExcludeFolders {

    private List<Folder> folders = new ArrayList<>();

    @Nonnull
    public Folder createFolder() {
      final Folder result = new Folder();
      this.folders.add(result);
      return result;
    }

    @Data
    public static class Folder {
      private String path = "";

      public void addText(@Nullable final String text) {
        this.path = GetUtils.ensureNonNull(text, "");
      }
    }
  }

  @Data
  @EqualsAndHashCode(callSuper=false)
  public static class ExcludeExtensions extends Extensions {

  }

  @Data
  @EqualsAndHashCode(callSuper=false)
  public static class Extensions {
    protected final List<Extension> extensions = new ArrayList<>();

    @Nonnull
    public Extension createExtension() {
      final Extension result = new Extension();
      this.extensions.add(result);
      return result;
    }

    @Data
    public static class Extension {
      private String name = "";

      public void addText(@Nullable final String text) {
        this.name += GetUtils.ensureNonNull(text, "");
      }
    }
  }

  @Data
  @EqualsAndHashCode(callSuper=false)
  public static class Vars {
    private List<Var> vars = new ArrayList<>();

    @Nonnull
    public Var createVar() {
      final Var result = new Var();
      this.vars.add(result);
      return result;
    }

    @Data
    @EqualsAndHashCode(callSuper=false)
    public static class Var {
      private String name = "";
      private String value = "";

      public void addText(@Nonnull final String text) {
        this.value += text;
      }
    }
  }
}
