package io.doublegsoft.exprbase.parser;

import com.doublegsoft.jcommons.metabean.AttributeDefinition;
import com.doublegsoft.jcommons.metabean.ModelDefinition;
import com.doublegsoft.jcommons.metabean.ObjectDefinition;
import com.doublegsoft.jcommons.metamodel.ComparisonDefinition;
import com.doublegsoft.jcommons.metamodel.UsecaseDefinition;
import com.doublegsoft.jcommons.metamodel.ValueDefinition;
import com.doublegsoft.jcommons.metamodel.VariableDefinition;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.math.BigDecimal;

public class ValueParser {

  private final ModelDefinition dataModel;

  public ValueParser(ModelDefinition dataModel) {
    this.dataModel = dataModel;
  }

  public void assemble(io.doublegsoft.exprbase.ExprbaseParser.Anybase_valueContext ctx,
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
        if (str.contains(".")) {
          String[] strs = str.split("\\.");
          VariableDefinition var = usecase.getVariable(strs[0]);
          if (var == null) {
            ObjectDefinition obj = dataModel.findObjectByName(strs[0]);
            AttributeDefinition attr = obj.getAttribute(strs[1]);
            value.setAttributeValue(attr);
          } else {
            value.setVariable(var);
          }
        } else {
          VariableDefinition var = usecase.getVariable(str);
          if (var == null) {
            var = new VariableDefinition();
            var.setName(str);
          }
          value.setVariable(var);
        }
      }
    } else if (ctx.anybase_number() != null) {
      value.setNumber(new BigDecimal(ctx.anybase_number().getText()));
    }
  }

  public void assemble(String originalText,
                       ValueDefinition value, UsecaseDefinition usecase) {
    CharStream input = CharStreams.fromString(originalText);
    io.doublegsoft.exprbase.ExprbaseLexer lexer = new io.doublegsoft.exprbase.ExprbaseLexer(input);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    io.doublegsoft.exprbase.ExprbaseParser parser = new io.doublegsoft.exprbase.ExprbaseParser(tokens);
    assemble(parser.anybase_value(), value, usecase);
  }
}
