package com.github.dakusui.floorplan.ut;

import com.github.dakusui.floorplan.ut.utils.UtBase;
import com.github.dakusui.floorplan.utils.ObjectSynthesizer;
import org.junit.Before;
import org.junit.Test;

import java.util.Objects;

import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.floorplan.utils.ObjectSynthesizer.methodCall;

public class ObjectSynthesizerTest extends UtBase {
  private X fallbackObject;

  interface A {
    // ReflectivelyCalled
    @SuppressWarnings("unused")
    String aMethod();
  }

  interface B {
    // ReflectivelyCalled
    @SuppressWarnings("unused")
    String bMethod();
  }

  interface C {
    // ReflectivelyCalled
    @SuppressWarnings("unused")
    String cMethod();
  }

  interface X extends A, B, C {
    // ReflectivelyCalled
    @SuppressWarnings("unused")
    String xMethod();

  }

  public interface Y extends X {
    default String yMethod() {
      return "yMethod";
    }
  }

  @Before
  public void before() {
    this.fallbackObject = createX("");
  }

  private X createX(String value) {
    return new X() {
      @Override
      public String xMethod() {
        return "xMethod" + value;
      }

      @Override
      public String cMethod() {
        return "cMethod";
      }

      @Override
      public String bMethod() {
        return "bMethod";
      }

      @Override
      public String aMethod() {
        return "aMethod";
      }

      @Override
      public int hashCode() {
        return value.hashCode();
      }

      @Override
      public boolean equals(Object anotherObject) {
        if (anotherObject instanceof X) {
          X another = (X) anotherObject;
          return Objects.equals(another.xMethod(), this.xMethod());
        }
        return false;
      }
    };
  }

  @Test
  public void whenMethodsCalled$thenProxiedToIntendedMethods() {
    X x = ObjectSynthesizer.builder(X.class)
        .handle(methodCall("aMethod").with((self, args) -> "a is called"))
        .handle(methodCall("bMethod").with((self, args) -> "b is called"))
        .fallbackTo(fallbackObject)
        .build()
        .synthesize();
    assertThat(
        x,
        allOf(
            asString("aMethod").equalTo("a is called").$(),
            asString("bMethod").equalTo("b is called").$(),
            asString("toString").startsWith("com.github.dakusui.floorplan.ut.ObjectSynthesizerTest$1@").$(),
            asString("cMethod").equalTo("cMethod").$(),
            asString("xMethod").equalTo("xMethod").$(),
            asInteger(call("xMethod").andThen("toString").andThen("length").$()).equalTo(7).$()
        ));
  }

  @Test
  public void whenEqualsOnSameObject$thenTrue() {
    X x = ObjectSynthesizer.builder(X.class)
        .handle(methodCall("aMethod").with((self, args) -> "a is called"))
        .handle(methodCall("bMethod").with((self, args) -> "b is called"))
        .fallbackTo(fallbackObject)
        .build()
        .synthesize();
    assertThat(
        x,
        allOf(
            asBoolean("equals", x).isTrue().$(),
            asBoolean("equals", fallbackObject).isTrue().$()
        ));
  }

  @Test
  public void whenEqualsOnAnotherObjectNotEqual$thenFalse() {
    X x = ObjectSynthesizer.builder(X.class)
        .handle(methodCall("aMethod").with((self, args) -> "a is called"))
        .handle(methodCall("bMethod").with((self, args) -> "b is called"))
        .fallbackTo(fallbackObject)
        .build()
        .synthesize();
    assertThat(
        x,
        asBoolean("equals", "Hello").isFalse().$()
    );
  }

  @Test
  public void whenEqualsOnAnotherXNotEqual$thenFalse() {
    X x = ObjectSynthesizer.builder(X.class)
        .handle(methodCall("aMethod").with((self, args) -> "a is called"))
        .handle(methodCall("bMethod").with((self, args) -> "b is called"))
        .fallbackTo(fallbackObject)
        .build()
        .synthesize();
    assertThat(
        x,
        asBoolean("equals", createX("Hello")).isFalse().$()
    );
  }

  @Test
  public void whenEqualsOnAnotherProxiedObjectEqualToIt$thenTrue() {
    X x = ObjectSynthesizer.builder(X.class)
        .handle(methodCall("aMethod").with((self, args) -> "a is called"))
        .handle(methodCall("bMethod").with((self, args) -> "b is called"))
        .fallbackTo(fallbackObject)
        .build()
        .synthesize();
    X x2 = ObjectSynthesizer.builder(X.class)
        .handle(methodCall("aMethod").with((self, args) -> "a is called"))
        .handle(methodCall("bMethod").with((self, args) -> "b is called"))
        .fallbackTo(createX(""))
        .build()
        .synthesize();
    assertThat(
        x,
        asBoolean("equals", x2).isTrue().$()
    );
  }

  @Test
  public void whenEqualsOnAnotherObjectEqualToIt$thenTrue() {
    X x = ObjectSynthesizer.builder(X.class)
        .handle(methodCall("aMethod").with((self, args) -> "a is called"))
        .handle(methodCall("bMethod").with((self, args) -> "b is called"))
        .fallbackTo(fallbackObject)
        .build()
        .synthesize();
    X x2 = createX("");
    assertThat(
        x,
        asBoolean("equals", x2).isTrue().$()
    );
  }

  @Test
  public void whenDefaultMethodCalled$thenValueReturned() {
    Y y = ObjectSynthesizer.builder(Y.class)
        .handle(methodCall("aMethod").with((self, args) -> "a is called"))
        .handle(methodCall("bMethod").with((self, args) -> "b is called"))
        .fallbackTo(fallbackObject)
        .build()
        .synthesize();
    assertThat(
        y.yMethod(),
        asString().equalTo("yMethod").$()
    );
  }
}
