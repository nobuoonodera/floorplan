package com.github.dakusui.floorplan.ut.utils;

import org.junit.After;
import org.junit.Before;

public abstract class UtBase {
  @Before
  public void before() {
    UtUtils.suppressStdOutErrIfRunUnderSurefireOrPitest();
  }

  @After
  public void after() {
    UtUtils.restoreStdOutErr();
  }
}