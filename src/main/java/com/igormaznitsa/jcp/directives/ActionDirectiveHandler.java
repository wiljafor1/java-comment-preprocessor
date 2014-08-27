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
package com.igormaznitsa.jcp.directives;

import com.igormaznitsa.jcp.context.PreprocessorContext;
import com.igormaznitsa.jcp.expression.Expression;
import com.igormaznitsa.jcp.expression.ExpressionItem;
import com.igormaznitsa.jcp.expression.ExpressionParser;
import com.igormaznitsa.jcp.expression.ExpressionTree;
import com.igormaznitsa.jcp.expression.Value;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * The class implements the //#action directive handler
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public class ActionDirectiveHandler extends AbstractDirectiveHandler {

  @Override
  public String getName() {
    return "action";
  }

  @Override
  public String getReference() {
    return "calls a preprocessor extension and pass directive arguments to it (arguments separated by comma)";
  }

  @Override
  public DirectiveArgumentType getArgumentType() {
    return DirectiveArgumentType.MULTIEXPRESSION;
  }

  @Override
  public AfterDirectiveProcessingBehaviour execute(final String string, final PreprocessorContext context) {
    if (context.getPreprocessorExtension() != null) {

      try {
        final List<ExpressionTree> args = parseString(string, context);

        final Value[] results = new Value[args.size()];
        int index = 0;
        for (final ExpressionTree expr : args) {
          final Value val = Expression.evalTree(expr, context);
          results[index++] = val;
        }

        if (!context.getPreprocessorExtension().processAction(context, results)) {
          throw new RuntimeException("Extension can't process the action");
        }
      }
      catch (IOException ex) {
        throw new IllegalArgumentException("Wrong string detected [" + string + ']', ex);
      }
    }
    return AfterDirectiveProcessingBehaviour.PROCESSED;
  }

  private List<ExpressionTree> parseString(final String str, final PreprocessorContext context) throws IOException {
    final ExpressionParser parser = ExpressionParser.getInstance();

    final PushbackReader reader = new PushbackReader(new StringReader(str));
    final List<ExpressionTree> result = new ArrayList<ExpressionTree>();

    while (true) {
      final ExpressionTree tree = new ExpressionTree();
      final ExpressionItem delimiter = parser.readExpression(reader, tree, context, false, true);

      if (delimiter != null && ExpressionParser.SpecialItem.COMMA != delimiter) {
        throw new IllegalArgumentException("Wrong format of an argument detected");
      }

      if (tree.isEmpty()) {
        if (delimiter == null) {
          break;
        }
        else {
          throw new IllegalArgumentException("Empty argument detected");
        }
      }
      else {
        result.add(tree);
        if (delimiter == null) {
          break;
        }
      }
    }

    return result;
  }
}