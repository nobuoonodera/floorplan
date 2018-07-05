package com.github.dakusui.floorplan;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Component;
import com.github.dakusui.floorplan.component.Configurator;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.policy.Policy;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static com.github.dakusui.floorplan.utils.Checks.requireState;

public interface Deployment {
  <A extends Attribute> Component<A> lookUp(Ref ref);

  class Impl implements Deployment {
    private final Map<Ref, Component<?>> components;

    Impl(Policy policy, DeploymentConfigurator deploymentConfigurator) {
      this.components = new LinkedHashMap<Ref, Component<?>>() {{
        deploymentConfigurator.allReferences().stream().map(
            ref -> (Configurator<?>) deploymentConfigurator.lookUp(ref)
        ).map(
            configurator -> configurator.build(policy)
        ).forEach(
            component -> put(component.ref(), component)
        );
      }};
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A extends Attribute> Component<A> lookUp(Ref ref) {
      return requireState(
          (Component<A>) this.components.get(ref), ret -> Objects.equals(ret.spec().attributeType(), ref.spec().attributeType())
      );
    }
  }
}