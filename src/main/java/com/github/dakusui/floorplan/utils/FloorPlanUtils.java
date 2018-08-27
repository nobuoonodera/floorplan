package com.github.dakusui.floorplan.utils;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Configurator;
import com.github.dakusui.floorplan.core.FloorPlan;
import com.github.dakusui.floorplan.core.FixtureConfigurator;
import com.github.dakusui.floorplan.core.FixtureDescriptor;
import com.github.dakusui.floorplan.core.FloorPlanGraph;
import com.github.dakusui.floorplan.policy.Policy;

import java.util.function.Function;

/**
 * A utility class that collects useful methods for users of the 'FloorPlan' library.
 */
public enum FloorPlanUtils {
  ;
  @SuppressWarnings("unchecked")
  public static <A extends Attribute, T> T resolve(A attr, Configurator<A> configurator, Policy policy) {
    return (T) Function.class.cast(Function.class.cast(configurator.resolverFor(attr, policy).apply(configurator))).apply(policy);
  }

  public static FloorPlan buildFixture(FixtureDescriptor fixtureDescriptor) {
    return createFixture(fixtureDescriptor, createPolicy(fixtureDescriptor));
  }

  @SuppressWarnings("unchecked")
  private static FloorPlan createFixture(FixtureDescriptor fixtureDescriptor, Policy policy) {
    FixtureConfigurator fixtureConfigurator = policy.fixtureConfigurator();
    fixtureDescriptor.attributes().forEach(
        each -> fixtureConfigurator.configure(
            each.target,
            each.attribute,
            each.resolver
        )
    );
    return fixtureConfigurator.build();
  }

  private static Policy createPolicy(FixtureDescriptor fixtureDescriptor) {
    Policy.Builder policyBuilder = new Policy.Builder().setProfile(
        fixtureDescriptor.profile()
    );
    fixtureDescriptor.specs().forEach(policyBuilder::addComponentSpec);
    policyBuilder.setFloorPlanGraph(createFloorPlanGraph(fixtureDescriptor));
    return policyBuilder.build();
  }

  private static FloorPlanGraph createFloorPlanGraph(FixtureDescriptor fixtureDescriptor) {
    FloorPlanGraph floorPlanGraph = new FloorPlanGraph.Impl();
    fixtureDescriptor.refs().forEach(floorPlanGraph::add);
    fixtureDescriptor.wires().forEach(each -> floorPlanGraph.wire(each.from, each.as, each.tos));
    return floorPlanGraph;
  }
}
