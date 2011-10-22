package com.igormaznitsa.jcpreprocessor.directives;

import com.igormaznitsa.jcpreprocessor.cfg.PreprocessorContext;
import com.igormaznitsa.jcpreprocessor.expression.Expression;
import com.igormaznitsa.jcpreprocessor.expression.Value;
import com.igormaznitsa.jcpreprocessor.expression.ValueType;
import java.io.IOException;

public class IfDirectiveHandler extends AbstractDirectiveHandler {

    @Override
    public String getName() {
        return "if";
    }

    @Override
    public boolean hasExpression() {
        return true;
    }

    @Override
    public boolean processOnlyIfProcessingEnabled() {
        return false;
    }

    @Override
    public DirectiveBehaviour execute(String string, ParameterContainer state, PreprocessorContext context) throws IOException {
        // Processing #if instruction
        if (state.isProcessingEnabled()) {
            Value p_value = Expression.eval(string,context);
            if (p_value == null || p_value.getType() != ValueType.BOOLEAN) {
                throw new IOException("You don't have a boolean result in the #if instruction");
            }
            if (state.isIfCounterZero()) {
                state.setLastIfFileName(state.getCurrentFileCanonicalPath());
                state.setLastIfStringNumber(state.getCurrentStringIndex());
            }
            state.increaseIfCounter();
            state.setActiveIfCounter(state.getIfCounter());

            if (((Boolean) p_value.getValue()).booleanValue()) {
                state.setIfEnabled(true);
            } else {
                state.setIfEnabled(false);
            }
        } else {
            state.increaseIfCounter();
        }
        return DirectiveBehaviour.NORMAL;
    }
}