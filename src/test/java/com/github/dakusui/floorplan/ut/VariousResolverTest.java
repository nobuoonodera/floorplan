package com.github.dakusui.floorplan.ut;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.ComponentSpec;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.core.FloorPlan;
import com.github.dakusui.floorplan.resolver.Resolvers;
import com.github.dakusui.floorplan.ut.utils.UtUtils;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.List;

import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.floorplan.resolver.Mappers.mapper;
import static com.github.dakusui.floorplan.resolver.Resolvers.*;
import static com.github.dakusui.floorplan.ut.utils.UtUtils.buildPolicy;

@RunWith(Enclosed.class)
public class VariousResolverTest {
  public static class TransformResolver {
    static class Cut {
      enum Attr implements Attribute {
        BASE(SPEC.property(String.class).defaultsTo(immediate("hello")).$()),
        TRANSFORM(SPEC.property(Integer.class).defaultsTo(transform(referenceTo(BASE), mapper(String::length))).$());

        private final Definition<Attr> definition;

        Attr(Definition<Attr> definition) {
          this.definition = definition;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Definition<Attr> definition() {
          return this.definition;
        }
      }

      static final ComponentSpec<Cut.Attr> SPEC = new ComponentSpec.Builder<>(Cut.Attr.class).build();
    }

    @Test
    public void givenTransformAttribute$whenEvaluate$thenCorrect() {
      Ref cut = Ref.ref(Cut.SPEC, "1");
      FloorPlan floorPlan = buildPolicy(UtUtils.createUtFloorPlanGraph().add(cut), Cut.SPEC).floorPlanConfigurator().build();

      assertThat(
          floorPlan.lookUp(cut).valueOf(Cut.Attr.TRANSFORM),
          asInteger().equalTo(5).$()
      );
    }
  }

  public static class TransformListResolver {
    static class Cut {
      enum Attr implements Attribute {
        @SuppressWarnings("unchecked")
        BASE(SPEC.property(List.class).defaultsTo(listOf(String.class, immediate("hello"), immediate("world!"))).$()),
        TRANSFORM_LIST(SPEC.property(List.class).defaultsTo(Resolvers.transformList(referenceTo(BASE), mapper(String::length))).$());

        private final Definition<Attr> definition;

        Attr(Definition<Attr> definition) {
          this.definition = definition;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Definition<Attr> definition() {
          return this.definition;
        }
      }

      static final ComponentSpec<Cut.Attr> SPEC = new ComponentSpec.Builder<>(Cut.Attr.class).build();
    }

    @Test
    public void givenTransformAttribute$whenEvaluate$thenCorrect() {
      Ref cut = Ref.ref(Cut.SPEC, "1");
      FloorPlan floorPlan = buildPolicy(UtUtils.createUtFloorPlanGraph().add(cut), Cut.SPEC).floorPlanConfigurator().build();

      assertThat(
          floorPlan.lookUp(cut).valueOf(Cut.Attr.TRANSFORM_LIST),
          allOf(
              asInteger("size").equalTo(2).$(),
              asInteger("get", 0).equalTo(5).$(),
              asInteger("get", 1).equalTo(6).$()
          )
      );
    }
  }

  public static class SizeOfResolver {
    static class Cut {
      enum Attr implements Attribute {
        @SuppressWarnings("unchecked")
        BASE(SPEC.property(List.class).defaultsTo(listOf(String.class, immediate("hello"), immediate("world!"))).$()),
        SIZE_OF(SPEC.property(Integer.class).defaultsTo(Resolvers.sizeOf(referenceTo(BASE))).$());

        private final Definition<Attr> definition;

        Attr(Definition<Attr> definition) {
          this.definition = definition;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Definition<Attr> definition() {
          return this.definition;
        }
      }

      static final ComponentSpec<Cut.Attr> SPEC = new ComponentSpec.Builder<>(Cut.Attr.class).build();
    }

    @Test
    public void givenTransformAttribute$whenEvaluate$thenCorrect() {
      Ref cut = Ref.ref(Cut.SPEC, "1");
      FloorPlan floorPlan = buildPolicy(UtUtils.createUtFloorPlanGraph().add(cut), Cut.SPEC).floorPlanConfigurator().build();

      assertThat(
          floorPlan.lookUp(cut).valueOf(Cut.Attr.SIZE_OF),
          asInteger().equalTo(2).$()
      );
    }
  }
}
