package com.github.dakusui.floorplan.examples;

import com.github.dakusui.floorplan.examples.bookstore.BookstoreExample;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import static com.github.dakusui.crest.Crest.*;

public class ExamplesTest {
  @Test
  public void givenBookstoreExample$whenExecuteTests$thenAllPass() {
    assertThat(
        JUnitCore.runClasses(BookstoreExample.class),
        allOf(
            asBoolean("wasSuccessful").isTrue().$(),
            asInteger("getRunCount").equalTo(8).$(),
            asInteger("getFailureCount").equalTo(0).$(),
            asInteger("getIgnoreCount").equalTo(0).$()
        )
    );
  }
}
