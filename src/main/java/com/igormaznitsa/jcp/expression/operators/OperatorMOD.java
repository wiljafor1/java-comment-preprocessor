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
package com.igormaznitsa.jcp.expression.operators;

import com.igormaznitsa.jcp.expression.ExpressionItemPriority;
import com.igormaznitsa.jcp.expression.Value;

/**
 * The class implements the MOD operator handler
 *
 * @author Igor Maznitsa (igor.maznitsa@igormaznitsa.com)
 */
public final class OperatorMOD extends AbstractOperator {

  @Override
  public int getArity() {
    return 2;
  }

  @Override
  public String getReference() {
    return "yields the remainder from the division of the left operand by the right operand";
  }

  @Override
  public String getKeyword() {
    return "%";
  }

  public Value executeIntInt(final Value arg1, final Value arg2) {
    return Value.valueOf(Long.valueOf(arg1.asLong().longValue() % arg2.asLong().longValue()));
  }

  public Value executeIntFloat(final Value arg1, final Value arg2) {
    return Value.valueOf(Float.valueOf(arg1.asLong().floatValue() % arg2.asFloat().floatValue()));
  }

  public Value executeFloatInt(final Value arg1, final Value arg2) {
    return Value.valueOf(Float.valueOf(arg1.asFloat().floatValue() % arg2.asLong().floatValue()));
  }

  public Value executeFloatFloat(final Value arg1, final Value arg2) {
    return Value.valueOf(Float.valueOf(arg1.asFloat().floatValue() % arg2.asFloat().floatValue()));
  }

  public ExpressionItemPriority getExpressionItemPriority() {
    return ExpressionItemPriority.ARITHMETIC_MUL_DIV_MOD;
  }
}