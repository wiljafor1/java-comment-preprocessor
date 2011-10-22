package com.igormaznitsa.jcpreprocessor.expression;

import com.igormaznitsa.jcpreprocessor.cfg.PreprocessorContext;
import java.io.File;

public interface AbstractExpressionExecutor {
    void execute(PreprocessorContext context, Expression stack, int index);
}