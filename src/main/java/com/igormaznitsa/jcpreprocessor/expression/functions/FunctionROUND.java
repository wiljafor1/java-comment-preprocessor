package com.igormaznitsa.jcpreprocessor.expression.functions;

import com.igormaznitsa.jcpreprocessor.cfg.PreprocessorContext;
import com.igormaznitsa.jcpreprocessor.expression.Expression;
import com.igormaznitsa.jcpreprocessor.expression.Value;
import java.io.File;

public final class FunctionROUND extends AbstractFunction {

    @Override
    public String getName() {
        return "round";
    }

    public void execute(PreprocessorContext context, Expression stack, int index) {
        if (!stack.isThereOneValueBefore(index)) throw new IllegalStateException("Operation ROUND needs an operand");

        Value _val0 = (Value)stack.getItemAtPosition(index-1);
        index--;
        stack.removeItemAt(index);

        switch (_val0.getType())
        {
            case INT:
                {
                    stack.setItemAtPosition(index, _val0);
                };break;
            case FLOAT:
                {
                    long l_result = Math.round(((Float) _val0.getValue()).longValue());
                    stack.setItemAtPosition(index, Value.valueOf(l_result));
                };break;
            default :
                throw new IllegalArgumentException("Function ROUND processes only the INTEGER or the FLOAT types");
        }

    }

    @Override
    public int getArity() {
        return 1;
    }
    
    
}