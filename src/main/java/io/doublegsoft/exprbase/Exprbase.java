package io.doublegsoft.exprbase;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metabean.type.CollectionType;
import com.doublegsoft.jcommons.metamodel.*;
import io.doublegsoft.exprbase.parser.ValueParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;

import java.math.BigDecimal;

public class Exprbase {

  public final static UsecaseDefinition DUMMY = new UsecaseDefinition("DUMMY");

  private final ModelDefinition dataModel;

  private ValueParser valueParser;

  public Exprbase(ModelDefinition dataModel) {
    this.dataModel = dataModel;
  }

  public CalculationDefinition parseCalculation(String expr) {
    return parseCalculation(expr, DUMMY);
  }

  public CalculationDefinition parseCalculation(String expr, UsecaseDefinition usecase) {
    CharStream input = CharStreams.fromString(expr);
    io.doublegsoft.exprbase.ExprbaseLexer lexer = new io.doublegsoft.exprbase.ExprbaseLexer(input);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    io.doublegsoft.exprbase.ExprbaseParser parser = new io.doublegsoft.exprbase.ExprbaseParser(tokens);
    CalculationDefinition retVal = new CalculationDefinition();
    parseCalcExpr(parser.exprbase_calc_expr(), retVal, usecase);
    return retVal;
  }

  public ComparisonDefinition parseComparison(String expr) {
    return parseComparison(expr, DUMMY);
  }

  public ComparisonDefinition parseComparison(String expr, UsecaseDefinition usecase) {
    CharStream input = CharStreams.fromString(expr);
    io.doublegsoft.exprbase.ExprbaseLexer lexer = new io.doublegsoft.exprbase.ExprbaseLexer(input);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    io.doublegsoft.exprbase.ExprbaseParser parser = new io.doublegsoft.exprbase.ExprbaseParser(tokens);
    ComparisonDefinition retVal = new ComparisonDefinition();
    parseCmpExpr(parser.exprbase_cmp_expr(), retVal, usecase);
    return retVal;
  }

  private void parseCmpExpr(io.doublegsoft.exprbase.ExprbaseParser.Exprbase_cmp_exprContext ctx,
                            ComparisonDefinition cmp, UsecaseDefinition usecase) {
    if (ctx.exprbase_comparator() != null) {
      ValueDefinition value = new ValueDefinition();
      cmp.setComparator(ctx.exprbase_comparator().getText());
      String comparand = ctx.comparand.getText();
      VariableDefinition var = parseVariable(comparand, value, usecase);
      cmp.setComparand(var);
      parseValue(ctx.value, value, usecase);
      cmp.setValue(value);
      cmp.setOriginalText(getOriginalText(ctx));
    } else if (ctx.paren != null) {
      parseCmpExpr(ctx.exprbase_cmp_expr(0), cmp, usecase);
    } else if (ctx.and != null) {
      ComparisonDefinition lhs = new ComparisonDefinition();
      parseCmpExpr(ctx.exprbase_cmp_expr(0), lhs, usecase);
      ComparisonDefinition rhs = new ComparisonDefinition();
      parseCmpExpr(ctx.exprbase_cmp_expr(1), rhs, usecase);
      cmp.getAndComparisons().add(lhs);
      cmp.getAndComparisons().add(rhs);
      lhs.setOriginalText(getOriginalText(ctx.exprbase_cmp_expr(0)));
      rhs.setOriginalText(getOriginalText(ctx.exprbase_cmp_expr(1)));
    } else if (ctx.or != null) {
      ComparisonDefinition lhs = new ComparisonDefinition();
      parseCmpExpr(ctx.exprbase_cmp_expr(0), lhs, usecase);
      ComparisonDefinition rhs = new ComparisonDefinition();
      parseCmpExpr(ctx.exprbase_cmp_expr(1), rhs, usecase);
      cmp.getOrComparisons().add(lhs);
      cmp.getOrComparisons().add(rhs);
      lhs.setOriginalText(getOriginalText(ctx.exprbase_cmp_expr(0)));
      rhs.setOriginalText(getOriginalText(ctx.exprbase_cmp_expr(1)));
    }
  }

  private void parseCalcExpr(io.doublegsoft.exprbase.ExprbaseParser.Exprbase_calc_exprContext ctx,
                             CalculationDefinition calc, UsecaseDefinition usecase) {
    if (ctx.exprbase_calc_value() != null) {
      ValueDefinition value = new ValueDefinition();
      parseValue(ctx.exprbase_calc_value().anybase_value(), value, usecase);
      calc.setValue(value);
      value.setOriginalText(getOriginalText(ctx.exprbase_calc_value()));
    } else if (ctx.left != null) {
      calc.setOperator(ctx.operator.getText());
      CalculationDefinition lhs = new CalculationDefinition();
      parseCalcExpr(ctx.left, lhs, usecase);
      calc.setLeftOperand(lhs);
      CalculationDefinition rhs = new CalculationDefinition();
      parseCalcExpr(ctx.right, rhs, usecase);
      calc.setRightOperand(rhs);
      lhs.setOriginalText(getOriginalText(ctx.left));
      rhs.setOriginalText(getOriginalText(ctx.right));
    } else if (ctx.paren != null) {
      parseCalcExpr(ctx.exprbase_calc_expr(0), calc, usecase);
    }
  }

  private ValueParser getValueParser() {
    if (valueParser == null) {
      valueParser = new ValueParser(dataModel);
    }
    return valueParser;
  }

  private void parseValue(io.doublegsoft.exprbase.ExprbaseParser.Anybase_valueContext ctx,
                          ValueDefinition value, UsecaseDefinition usecase) {
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
        VariableDefinition var = parseVariable(str, value, usecase);
        value.setVariable(var);
      }
    } else if (ctx.anybase_number() != null) {
      value.setNumber(new BigDecimal(ctx.anybase_number().getText()));
    }
  }

  private VariableDefinition parseVariable(String varname, ValueDefinition value, UsecaseDefinition usecase) {
    if (varname.contains(".") && DUMMY != usecase) {
      String[] strs = varname.split("\\.");
      VariableDefinition var = usecase.getVariable(strs[0]);
      if (var == null) {
        var = new VariableDefinition();
        ObjectDefinition obj = dataModel.findObjectByName(strs[0]);
        var.setName(strs[0]);
        var.setType(obj);
      }
      if (value != null) {
        ObjectDefinition obj = null;
        if (var.getType().isCollection()) {
          CollectionType collType = (CollectionType) var.getType();
          obj = dataModel.findObjectByName(collType.getComponentType().getName());
        } else {
          obj = (ObjectDefinition) var.getType();
        }
        AttributeDefinition attr = obj.getAttribute(strs[1]);
        value.setAttributeValue(attr);
        value.setVariable(var);
      }
      return var;
    } else if (DUMMY != usecase){
      VariableDefinition var = usecase.getVariable(varname);
      if (var == null) {
        var = new VariableDefinition();
        var.setName(varname);
      }
      value.setVariable(var);
      return var;
    } else {
      VariableDefinition var = new VariableDefinition();
      var.setName(varname);
      value.setVariable(var);
      return var;
    }
  }

  public static String getOriginalText(ParserRuleContext ctx) {
    Interval intv = new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
    return ctx.start.getInputStream().getText(intv);
  }
}
