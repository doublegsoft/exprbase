package io.doublegsoft.exprbase;

import com.doublegsoft.jcommons.metamodel.CalculationDefinition;
import com.doublegsoft.jcommons.metamodel.ComparisonDefinition;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class CmpTest extends TestBase {

  @Test
  public void test_calc_expr_single() throws Exception {
    Exprbase parser = newExprbase();
    String expr = "a > 100";
    ComparisonDefinition cmp = parser.parseComparison(expr);
    Assert.assertEquals("a", cmp.getComparand().getName());
    Assert.assertEquals(new BigDecimal(100), cmp.getValue().getNumber());
  }

  @Test
  public void test_calc_expr_and() throws Exception {
    Exprbase parser = newExprbase();
    String expr = "a > 100 and b > 200";
    ComparisonDefinition cmp = parser.parseComparison(expr);
    Assert.assertEquals("a", cmp.getAndComparisons().get(0).getComparand().getName());
    Assert.assertEquals("b", cmp.getAndComparisons().get(1).getComparand().getName());
  }

  @Test
  public void test_calc_expr_or_and() throws Exception {
    Exprbase parser = newExprbase();
    String expr = "(a > 100 or c < 10) and b > 200";
    ComparisonDefinition cmp = parser.parseComparison(expr);
    Assert.assertEquals("a", cmp.getAndComparisons().get(0).getOrComparisons().get(0).getComparand().getName());
    Assert.assertEquals("c", cmp.getAndComparisons().get(0).getOrComparisons().get(1).getComparand().getName());
    Assert.assertEquals("b", cmp.getAndComparisons().get(1).getComparand().getName());
  }

}
