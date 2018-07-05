package com.github.dakusui.floorplan.ut;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionPerformer;
import com.github.dakusui.floorplan.Deployment;
import com.github.dakusui.floorplan.FloorPlan;
import com.github.dakusui.floorplan.component.*;
import com.github.dakusui.floorplan.examples.components.ReferenceComponent;
import com.github.dakusui.floorplan.examples.components.SimpleComponent;
import com.github.dakusui.floorplan.examples.profile.SimpleProfile;
import com.github.dakusui.floorplan.exception.MissingValueException;
import com.github.dakusui.floorplan.exception.TypeMismatch;
import com.github.dakusui.floorplan.policy.Policy;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.floorplan.resolver.Resolvers.*;

public class FloorPlanTest {
  @Test
  public void givenSimpleAttribute$whenConfiguredWithImmediate$thenAttributeIsResolvedCorrectly() {
    Ref simple1 = Ref.ref(SimpleComponent.SPEC, "simple1");
    Policy policy = policy(new FloorPlan().add(simple1), SimpleComponent.SPEC);

    Deployment deployment = policy.deploymentConfigurator(
    ).configure(
        simple1,
        SimpleComponent.Attr.INSTANCE_NAME,
        immediate("configured-instance-name-simple1")
    ).build();

    assertThat(
        deployment.lookUp(simple1),
        allOf(
            asObject(
                (Component<SimpleComponent.Attr> c) -> c.valueOf(SimpleComponent.Attr.INSTANCE_NAME)
            ).isInstanceOf(String.class).$(),
            asString("valueOf", SimpleComponent.Attr.INSTANCE_NAME).equalTo("configured-instance-name-simple1").$(),
            asString("valueOf", SimpleComponent.Attr.DEFAULT_TO_IMMEDIATE).equalTo("default-value").$(),
            asString("valueOf", SimpleComponent.Attr.DEFAULT_TO_INTERNAL_REFERENCE).equalTo("configured-instance-name-simple1").$()
        )
    );
  }

  @Test
  public void givenSimpleAttribute$whenConfiguredWithProfileAttribute$thenResolvedCorrectly() {
    Ref simple1 = Ref.ref(SimpleComponent.SPEC, "simple1");
    Policy policy = policy(new FloorPlan().add(simple1), SimpleComponent.SPEC);

    Deployment deployment = policy.deploymentConfigurator(
    ).configure(
        simple1,
        SimpleComponent.Attr.INSTANCE_NAME,
        profileValue("configured-instance-name-simple1")
    ).build();

    assertThat(
        deployment.lookUp(simple1),
        allOf(
            asObject(
                (Component<SimpleComponent.Attr> c) -> c.valueOf(SimpleComponent.Attr.INSTANCE_NAME)
            ).isInstanceOf(String.class).$(),
            asString("valueOf", SimpleComponent.Attr.INSTANCE_NAME).equalTo("profile(configured-instance-name-simple1)").$(),
            asString("valueOf", SimpleComponent.Attr.DEFAULT_TO_IMMEDIATE).equalTo("default-value").$(),
            asString("valueOf", SimpleComponent.Attr.DEFAULT_TO_INTERNAL_REFERENCE).equalTo("profile(configured-instance-name-simple1)").$()
        )
    );
  }

  @Test
  public void givenSimpleAttribute$whenConfiguredWithSlotAttribute$thenResolvedCorrectly() {
    Ref simple1 = Ref.ref(SimpleComponent.SPEC, "simple1");
    Policy policy = policy(new FloorPlan().add(simple1), SimpleComponent.SPEC);

    Deployment deployment = policy.deploymentConfigurator(
    ).configure(
        simple1,
        SimpleComponent.Attr.INSTANCE_NAME,
        slotValue("configured-instance-name-simple1")
    ).build();

    assertThat(
        deployment.lookUp(simple1),
        allOf(
            asObject(
                (Component<SimpleComponent.Attr> c) -> c.valueOf(SimpleComponent.Attr.INSTANCE_NAME)
            ).isInstanceOf(String.class).$(),
            asString("valueOf", SimpleComponent.Attr.INSTANCE_NAME).equalTo("slot(SimpleComponent#simple1, configured-instance-name-simple1)").$(),
            asString("valueOf", SimpleComponent.Attr.DEFAULT_TO_IMMEDIATE).equalTo("default-value").$(),
            asString("valueOf", SimpleComponent.Attr.DEFAULT_TO_INTERNAL_REFERENCE).equalTo("slot(SimpleComponent#simple1, configured-instance-name-simple1)").$()
        )
    );
  }

  @Test
  public void givenSimpleComponent$whenConfigureInstaller$thenIntendedOperatorIsUsed() {
    Ref simple1 = Ref.ref(SimpleComponent.SPEC, "simple1");
    Policy policy = policy(new FloorPlan().add(simple1), SimpleComponent.SPEC);

    List<String> out = new LinkedList<>();
    Deployment deployment = policy.deploymentConfigurator(
    ).configure(
        simple1,
        SimpleComponent.Attr.INSTANCE_NAME,
        immediate("configured-instance-name-simple1")
    ).configure(
        simple1,
        Operation.INSTALL,
        Operator.of(
            c -> context -> context.simple("simple", () -> out.add("hello")),
            () -> "simpleActionThatAddsAMessageToOut"
        )
    ).build();

    new ReportingActionPerformer.Builder(
        deployment.lookUp(simple1).install().apply(new Context.Impl())
    ).build(
    ).performAndReport();

    assertThat(
        out,
        allOf(
            asInteger("size").equalTo(1).$(),
            asString("get", 0).equalTo("hello").$()
        )
    );
  }

  @Test(expected = UnsupportedOperationException.class)
  public void givenSimpleComponent$whenInstallerIsNotConfigured$thenExceptionThrown() {
    Ref simple1 = Ref.ref(SimpleComponent.SPEC, "simple1");
    Policy policy = policy(new FloorPlan().add(simple1), SimpleComponent.SPEC);

    Deployment deployment = policy.deploymentConfigurator(
    ).configure(
        simple1,
        SimpleComponent.Attr.INSTANCE_NAME,
        immediate("configured-instance-name-simple1")
    ).build();

    new ReportingActionPerformer.Builder(
        deployment.lookUp(simple1).install().apply(new Context.Impl())
    ).build(
    ).performAndReport();
  }


  @Test
  public void givenReferencingAttribute$whenConfiguredWithReference$thenAttributeIsResolvedCorrectly() {
    Ref simple1 = Ref.ref(SimpleComponent.SPEC, "simple1");
    Ref ref1 = Ref.ref(ReferenceComponent.SPEC, "ref1");
    Policy policy = policy(
        new FloorPlan().add(simple1).add(ref1.spec(), ref1.id()),
        SimpleComponent.SPEC,
        ReferenceComponent.SPEC
    );

    Deployment deployment = policy.deploymentConfigurator(
    ).configure(
        simple1,
        SimpleComponent.Attr.INSTANCE_NAME,
        immediate("configured-instance-name-simple1")
    ).configure(
        ref1,
        ReferenceComponent.Attr.REFERENCE_TO_ANOTHER_COMPONENT_INSTANCE,
        referenceTo(simple1)
    ).build();

    assertThat(
        deployment.lookUp(ref1),
        allOf(
            asObject(
                "valueOf", ReferenceComponent.Attr.REFERENCE_TO_ANOTHER_COMPONENT_INSTANCE
            ).isInstanceOf(Configurator.class).$(),
            asString(
                "valueOf", ReferenceComponent.Attr.REFERENCE_TO_ATTRIBUTE
            ).equalTo(
                "configured-instance-name-simple1"
            ).$()
        )
    );
  }

  @Test
  public void givenReferencingAttribute$whenFloorPlanIsConfigured$thenAttributeIsResolvedCorrectly() {
    Ref simple1 = Ref.ref(SimpleComponent.SPEC, "simple1");
    Ref ref1 = Ref.ref(ReferenceComponent.SPEC, "ref1");
    Policy policy = policy(
        new FloorPlan(
        ).add(
            simple1
        ).add(
            ref1
        ).wire(
            ref1, simple1, ReferenceComponent.Attr.REFERENCE_TO_ANOTHER_COMPONENT_INSTANCE
        ),
        SimpleComponent.SPEC,
        ReferenceComponent.SPEC
    );

    Deployment deployment = policy.deploymentConfigurator(
    ).configure(
        simple1,
        SimpleComponent.Attr.INSTANCE_NAME,
        immediate("configured-instance-name-simple1")
    ).build();

    assertThat(
        deployment.lookUp(ref1),
        allOf(
            asObject(
                "valueOf", ReferenceComponent.Attr.REFERENCE_TO_ANOTHER_COMPONENT_INSTANCE
            ).isInstanceOf(Configurator.class).$(),
            asString(
                "valueOf", ReferenceComponent.Attr.REFERENCE_TO_ATTRIBUTE
            ).equalTo(
                "configured-instance-name-simple1"
            ).$()
        )
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void unknownSpec() {
    Ref simple1 = Ref.ref(SimpleComponent.SPEC, "simple1");
    try {
      policy(new FloorPlan().add(simple1)/*, SimpleComponent.SPEC*/);
    } catch (IllegalArgumentException e) {
      assertThat(
          e,
          asString("getMessage")
              .startsWith("References using unknown specs")
              .containsString("SimpleComponent#simple1")
              .$()
      );
      throw e;
    }
  }

  @Test(expected = MissingValueException.class)
  public void missingValue() {
    Ref simple1 = Ref.ref(SimpleComponent.SPEC, "simple1");
    try {
      policy(new FloorPlan().add(simple1), SimpleComponent.SPEC).deploymentConfigurator().build();
    } catch (MissingValueException e) {
      assertThat(
          e,
          asString("getMessage")
              .startsWith("Missing value")
              .containsString(SimpleComponent.Attr.INSTANCE_NAME.name())
              .containsString(simple1.toString())
              .$()
      );
      throw e;
    }
  }


  @Test(expected = TypeMismatch.class)
  public void typeMismatch() {
    Ref simple1 = Ref.ref(SimpleComponent.SPEC, "simple1");
    try {
      Deployment deployment = policy(new FloorPlan().add(simple1), SimpleComponent.SPEC).deploymentConfigurator()
          .configure(simple1, SimpleComponent.Attr.INSTANCE_NAME, immediate(123))
          .build();
      System.out.println(String.format("value='%s'", deployment.lookUp(simple1).valueOf(SimpleComponent.Attr.INSTANCE_NAME)));
    } catch (TypeMismatch e) {
      assertThat(
          e,
          asString("getMessage")
              .startsWith("A value of")
              .containsString(String.class.getCanonicalName())
              .containsString("123")
              .containsString(Integer.class.getCanonicalName())
              .$()
      );
      throw e;
    }
  }


  private Policy policy(FloorPlan floorPlan, ComponentSpec<?>... specs) {
    Policy.Builder builder = new Policy.Builder();
    for (ComponentSpec<?> each : specs) {
      builder = builder.addComponentSpec(each);
    }
    return builder.setFloorPlan(floorPlan).setProfile(new SimpleProfile()).build();
  }
}