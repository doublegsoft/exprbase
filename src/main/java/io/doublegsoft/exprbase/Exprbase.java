package io.doublegsoft.exprbase;

import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metamodel.*;
import io.doublegsoft.usebase.parser.ValueParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Exprbase {

  private final ModelDefinition dataModel;

  private ValueParser valueParser;

  public Exprbase(ModelDefinition dataModel) {
    this.dataModel = dataModel;
  }

  public CalculationDefinition parseCalculation(String expr) {
    CharStream input = CharStreams.fromString(expr);
    io.doublegsoft.exprbase.ExprbaseLexer lexer = new io.doublegsoft.exprbase.ExprbaseLexer(input);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    io.doublegsoft.exprbase.ExprbaseParser parser = new io.doublegsoft.exprbase.ExprbaseParser(tokens);
    CalculationDefinition retVal = new CalculationDefinition();
    parseCalcExpr(parser.exprbase_calc_expr(), retVal);
    return retVal;
  }

  public ComparisonDefinition parseComparison(String expr) {
    CharStream input = CharStreams.fromString(expr);
    io.doublegsoft.exprbase.ExprbaseLexer lexer = new io.doublegsoft.exprbase.ExprbaseLexer(input);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    io.doublegsoft.exprbase.ExprbaseParser parser = new io.doublegsoft.exprbase.ExprbaseParser(tokens);
    return parseCmpExpr(parser.exprbase_cmp_expr());
  }

  private ComparisonDefinition parseCmpExpr(io.doublegsoft.exprbase.ExprbaseParser.Exprbase_cmp_exprContext ctx) {
    ComparisonDefinition retVal = new ComparisonDefinition();
    if (ctx.msg != null) {
      String msg = ctx.msg.getText();
      retVal.setError(msg.substring(0, msg.length() - 1));
    }
    parseCmp(ctx.exprbase_cmp(), retVal);
    return retVal;
  }

  private void parseCmp(io.doublegsoft.exprbase.ExprbaseParser.Exprbase_cmpContext ctx,
                        ComparisonDefinition cmp) {
    if (ctx.exprbase_comparator() != null) {
      cmp.setComparator(ctx.exprbase_comparator().getText());
      String comparand = ctx.comparand.getText();
      VariableDefinition var = new VariableDefinition();
      var.setName(comparand);
      cmp.setComparand(var);
      ValueDefinition value = new ValueDefinition();
      parseValue(ctx.value, value);
      cmp.setValue(value);
    } else if (ctx.paren != null) {
      parseCmp(ctx.exprbase_cmp(0), cmp);
    } else if (ctx.and != null) {
      ComparisonDefinition lhs = new ComparisonDefinition();
      parseCmp(ctx.exprbase_cmp(0), lhs);
      ComparisonDefinition rhs = new ComparisonDefinition();
      parseCmp(ctx.exprbase_cmp(1), rhs);
      cmp.getAndComparisons().add(lhs);
      cmp.getAndComparisons().add(rhs);
    } else if (ctx.or != null) {
      ComparisonDefinition lhs = new ComparisonDefinition();
      parseCmp(ctx.exprbase_cmp(0), lhs);
      ComparisonDefinition rhs = new ComparisonDefinition();
      parseCmp(ctx.exprbase_cmp(1), rhs);
      cmp.getOrComparisons().add(lhs);
      cmp.getOrComparisons().add(rhs);
    }
  }

  private void parseCalcExpr(io.doublegsoft.exprbase.ExprbaseParser.Exprbase_calc_exprContext ctx,
                             CalculationDefinition calc) {
    if (ctx.exprbase_calc_value() != null) {
      ValueDefinition value = new ValueDefinition();
      parseValue(ctx.exprbase_calc_value().anybase_value(), value);
      calc.setValue(value);
    } else if (ctx.left != null) {
      calc.setOperator(ctx.operator.getText());
      CalculationDefinition lhs = new CalculationDefinition();
      parseCalcExpr(ctx.left, lhs);
      calc.setLeftOperand(lhs);
      CalculationDefinition rhs = new CalculationDefinition();
      parseCalcExpr(ctx.right, rhs);
      calc.setRightOperand(rhs);
    } else if (ctx.paren != null) {
      parseCalcExpr(ctx.exprbase_calc_expr(0), calc);
    }
  }

  private ValueParser getValueParser() {
    if (valueParser == null) {
      valueParser = new ValueParser(dataModel);
    }
    return valueParser;
  }

  private void parseValue(io.doublegsoft.exprbase.ExprbaseParser.Anybase_valueContext ctx,
                          ValueDefinition value) {
    if (ctx.anybase_string() != null) {
      String str = ctx.anybase_string().getText();
      value.setString(str.substring(1, str.length() - 1));
    } else if (ctx.anybase_identifier() != null) {
      String str = ctx.anybase_identifier().getText();
      if ("now".equals(str) || "null".equals(str)) {
        value.setKeyword(str);
      } else if ("true".equals(str) || "false".equals(str)){
        value.setBool(str);
      } else {
        VariableDefinition var = new VariableDefinition();
        var.setName(str);
        value.setVariable(var);
      }
    } else if (ctx.anybase_number() != null) {
      value.setNumber(new BigDecimal(ctx.anybase_number().getText()));
    }
  }

}
