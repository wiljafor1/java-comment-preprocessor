/* 
 * Copyright 2014 Igor Maznitsa (http://www.igormaznitsa.com).
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
package com.igormaznitsa.jcp.cmdline;

import javax.annotation.Nonnull;

import com.igormaznitsa.jcp.context.PreprocessorContext;

/**
 * Allow spaces between comment and directive.
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class AllowSpacesBeforeDirectiveHandler implements CommandLineHandler {

  private static final String ARG_NAME = "/ES";

  @Override
  @Nonnull
  public String getDescription() {
    return "allow spaces between comment and directives";
  }

  @Override
  public boolean processCommandLineKey(@Nonnull final String key, @Nonnull final PreprocessorContext context) {
    boolean result = false;

    if (ARG_NAME.equalsIgnoreCase(key)) {
      context.setAllowSpaceBeforeDirectives(true);
      result = true;
    }

    return result;
  }

  @Override
  @Nonnull
  public String getKeyName() {
    return ARG_NAME;
  }

}