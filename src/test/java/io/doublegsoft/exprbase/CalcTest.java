package io.doublegsoft.exprbase;

import com.doublegsoft.jcommons.metamodel.CalculationDefinition;
import org.junit.Assert;
import org.junit.Test;

public class CalcTest extends TestBase {

  @Test
  public void test_calc_expr_add() throws Exception {
    Exprbase parser = newExprbase();
    String expr = "a + b / d * 3";
    CalculationDefinition calc = parser.parseCalculation(expr);
    Assert.assertEquals("a", calc.getLeftOperand().getValue().getVariable().getName());
    Assert.assertEquals("b", calc.getRightOperand().getLeftOperand().getLeftOperand()
        .getValue().getVariable().getName());
  }

  @Test
  public void test_calc_expr_paren_add() throws Exception {
    Exprbase parser = newExprbase();
    String expr = "(a + b) / d * 3";
    CalculationDefinition calc = parser.parseCalculation(expr);
    Assert.assertEquals("a", calc.getLeftOperand().getLeftOperand().getLeftOperand()
        .getValue().getVariable().getName());
    Assert.assertEquals("b", calc.getLeftOperand().getLeftOperand().getRightOperand()
        .getValue().getVariable().getName());
  }

}
