package io.doublegsoft.exprbase;

import com.doublegsoft.jcommons.metabean.ModelDefinition;

public class TestBase {

  protected Exprbase newExprbase(String... modules) {
    ModelDefinition dataModel = new ModelDefinition();
    return new Exprbase(dataModel);
  }

}
