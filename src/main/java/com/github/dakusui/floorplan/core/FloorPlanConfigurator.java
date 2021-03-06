package com.github.dakusui.floorplan.core;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Configurator;
import com.github.dakusui.floorplan.component.Ref;
import com.github.dakusui.floorplan.exception.Exceptions;
import com.github.dakusui.floorplan.policy.Policy;
import com.github.dakusui.floorplan.resolver.Resolver;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static com.github.dakusui.floorplan.utils.Checks.requireState;
import static com.github.dakusui.floorplan.utils.InternalUtils.singletonCollector;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public interface FloorPlanConfigurator {
  <A extends Attribute> Configurator<A> lookUp(Ref ref);

  Set<Ref> allReferences();

  FloorPlan build();

  default <A extends Attribute> FloorPlanConfigurator configure(Ref ref, A attr, Resolver<A, ?> resolver) {
    this.<A>lookUp(ref).configure(attr, resolver);
    return this;
  }

  class Impl implements FloorPlanConfigurator {
    private final Set<Ref>              refs;
    private final List<Configurator<?>> configurators;
    private final Policy                policy;
    private final FloorPlan.Factory     floorPlanFactory;

    Impl(Policy policy, Set<Ref> refs, FloorPlan.Factory floorPlanFactory) {
      this.policy = requireNonNull(policy);
      this.refs = unmodifiableSet(requireNonNull(refs));
      this.configurators = unmodifiableList(
          refs.stream().map(
              // Not all components require slots.
              (Function<Ref, Configurator<?>>) ref -> ref.spec().configurator(ref.id())
          ).collect(toList())
      );
      this.floorPlanFactory = requireNonNull(floorPlanFactory);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A extends Attribute> Configurator<A> lookUp(Ref ref) {
      return (Configurator<A>) requireState(
          configurators.stream().filter(
              c -> Objects.equals(ref, c.ref())
          ).collect(
              singletonCollector()
          ).orElseThrow(
              Exceptions.noSuchElement("Configurator for '%s' was not found.", ref)
          ), ret -> Objects.equals(ret.spec().attributeType(), ref.spec().attributeType())
      );
    }

    public Set<Ref> allReferences() {
      return refs;
    }

    @Override
    public FloorPlan build() {
      return this.floorPlanFactory.create(this.policy, this);
    }
  }
}
