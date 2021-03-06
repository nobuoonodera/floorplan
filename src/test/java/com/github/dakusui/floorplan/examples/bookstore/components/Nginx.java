package com.github.dakusui.floorplan.examples.bookstore.components;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.Component;
import com.github.dakusui.floorplan.component.ComponentSpec;

import java.util.LinkedList;
import java.util.List;

import static com.github.dakusui.actionunit.core.ActionSupport.*;
import static com.github.dakusui.floorplan.resolver.Mappers.mapper;
import static com.github.dakusui.floorplan.resolver.Resolvers.*;
import static com.github.dakusui.floorplan.ut.utils.UtUtils.runShell;

public class Nginx {
  public enum Attr implements Attribute {
    @SuppressWarnings("unchecked")
    HOSTNAME(SPEC.property(String.class).defaultsTo(slotValue("hostname")).$()),
    PORTNUMBER(SPEC.property(Integer.class).defaultsTo(slotValue("port")).$()),
    BOOKSTORE_APPNAME(SPEC.property(String.class).defaultsTo(immediate("bookstore")).$()),
    UPSTREAM(SPEC.listPropertyOf(BookstoreApp.SPEC).required().$()),
    @SuppressWarnings("unchecked") AUTHORS(SPEC.listPropertyOf(String.class).defaultsTo(listOf(
        String.class,
        immediate("mrx"),
        immediate("mry"))
    ).$()),
    @SuppressWarnings("unchecked")
    ENDPOINT(SPEC.property(String.class).defaultsTo(transform(
        listOf(
            Object.class,
            referenceTo(HOSTNAME),
            referenceTo(PORTNUMBER),
            referenceTo(BOOKSTORE_APPNAME)
        ),
        mapper((List<Object> args) -> String.format("https://%s:%s/%s", args.get(0), args.get(1), args.get(2)))
    )).$()),
    INSTALL(SPEC.property(ActionFactory.class).defaultsTo(
        immediate(ActionFactory.of(
            component -> sequential(
                simple(
                    "yum install",
                    (c) -> runShell("ssh -l root@%s 'yum install nginx'", component.<String>valueOf(Attr.HOSTNAME))),
                simple(
                    "configure",
                    (c) -> runShell(
                        "ssh -l root@%s, echo \"upstream dynamic {%n" +
                            "%s",
                        "}\" > /etc/nginx.conf%n",
                        component.<String>valueOf(Attr.HOSTNAME),
                        new LinkedList<String>() {{
                          component.<Component<BookstoreApp.Attr>>streamOf(Attr.UPSTREAM).forEach(
                              (app) -> add(
                                  String.format(
                                      "  server:%s:%s",
                                      app.<String>valueOf(BookstoreApp.Attr.WEBSERVER_HOST),
                                      app.<String>valueOf(BookstoreApp.Attr.WEBSERVER_PORT)
                                  )
                              ));
                        }}
                    )
                )
            )
        ))
    ).$()),
    START(SPEC.property(ActionFactory.class).defaultsTo(
        immediate(ActionFactory.of(
            component -> {
              throw new UnsupportedOperationException();
            }
        ))
    ).$()),
    STOP(SPEC.property(ActionFactory.class).defaultsTo(
        immediate(ActionFactory.of(
            component -> {
              throw new UnsupportedOperationException();
            }
        ))
    ).$()),
    NUKE(SPEC.property(ActionFactory.class).defaultsTo(
        immediate(ActionFactory.of(
            component -> {
              throw new UnsupportedOperationException();
            }
        ))
    ).$()),
    UNINSTALL(SPEC.property(ActionFactory.class).defaultsTo(
        immediate(ActionFactory.of(
            attrComponent -> named("Do something for uninstallation", nop())
        ))
    ).$());

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

  public static final ComponentSpec<Attr> SPEC = new ComponentSpec.Builder<>(
      Attr.class
  ).build();
}
