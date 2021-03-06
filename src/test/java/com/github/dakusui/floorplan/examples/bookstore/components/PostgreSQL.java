package com.github.dakusui.floorplan.examples.bookstore.components;

import com.github.dakusui.floorplan.component.Attribute;
import com.github.dakusui.floorplan.component.ComponentSpec;

import static com.github.dakusui.actionunit.core.ActionSupport.*;
import static com.github.dakusui.floorplan.resolver.Resolvers.immediate;
import static com.github.dakusui.floorplan.resolver.Resolvers.slotValue;
import static com.github.dakusui.floorplan.ut.utils.UtUtils.runShell;

/**
 * This is just an example.
 * <p>
 * This class models an instance of PostgreSQL DMBS and is intended to illustrate
 * how 'floorplan' library works and is to be used.
 * <p>
 * When you implement your own model for real purpose, you will need to execute
 * actual commands instead of just printing them and learn specification of component
 * for which you are creating model and implement your own class. But still this
 * class gives you a good idea about what you will need to do.
 */
public class PostgreSQL {
  public enum Attr implements Attribute {
    HOSTNAME(SPEC.property(String.class).defaultsTo(slotValue("hostname")).$()),
    PORTNUMBER(SPEC.property(Integer.class).defaultsTo(slotValue("port")).$()),
    BOOKSTORE_DATABASE(SPEC.property(String.class).defaultsTo(immediate("bookstore_db")).$()),
    BASEDIR(SPEC.property(String.class).defaultsTo(immediate("/usr/local/postgresql")).$()),
    DATADIR(SPEC.property(String.class).defaultsTo(immediate("/var/postgresql/data")).$()),
    INSTALL(SPEC.property(ActionFactory.class).defaultsTo(
        immediate(ActionFactory.of(
            component -> sequential(
                simple("yum install", (c) -> runShell(
                    "ssh -l root@%s yum install postgresql", component.<String>valueOf(Attr.HOSTNAME))),
                simple("initdb", (c) -> runShell(
                    "ssh -l root@%s postgresql-setup initdb", component.<String>valueOf(Attr.HOSTNAME))),
                named("Update postgresql.conf",
                    sequential(
                        simple("Update port", (c) -> runShell(
                            "ssh -l root@%s sed -i /etc/postgresql.conf s/PGPORT=.+/PGPORT=%s/g",
                            component.<String>valueOf(Attr.HOSTNAME),
                            component.<Integer>valueOf(Attr.PORTNUMBER)
                        )),
                        simple("Update data dir", (c) -> runShell(
                            "ssh -l root@%s sed -i /etc/postgresql.conf s/DATADIR=.+/DATADIR=%s/g",
                            component.<String>valueOf(Attr.HOSTNAME),
                            component.<String>valueOf(Attr.DATADIR)))
                    )))))).$()),
    START(SPEC.property(ActionFactory.class).defaultsTo(
        immediate(ActionFactory.of(
            component -> simple(
                "pg_ctl start",
                (c) -> runShell("ssh -l postgres@%s pg_ctl start")
            )
        ))).$()),
    STOP(SPEC.property(ActionFactory.class).defaultsTo(
        immediate(ActionFactory.of(
            component -> simple(
                "pg_ctl stop",
                (c) -> runShell("ssh -l postgres@%s pg_ctl stop")
            )
        ))).$()),
    NUKE(SPEC.property(ActionFactory.class).defaultsTo(
        immediate(ActionFactory.of(
            component -> simple(
                "send kill -9",
                (c) -> runShell("ssh -l root@%s pkill -9 postgres", component.valueOf(Attr.HOSTNAME)))
        ))).$()),
    UNINSTALL(SPEC.property(ActionFactory.class).defaultsTo(
        immediate(ActionFactory.of(
            component -> sequential(
                simple("remove basedir", (c) -> runShell(
                    "rm -fr %s", component.<String>valueOf(Attr.BASEDIR))),
                simple("remove datadir", (c) -> runShell(
                    "rm -fr %s", component.<String>valueOf(Attr.DATADIR))))
        ))).$());
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

  public static final ComponentSpec<Attr> SPEC = new ComponentSpec.Builder<>(Attr.class).build();
}
