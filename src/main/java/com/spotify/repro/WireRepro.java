package com.spotify.repro;

import com.google.common.base.Throwables;
import com.spotify.apollo.Environment;
import com.yahoo.elide.Elide;
import com.yahoo.elide.audit.Slf4jLogger;
import com.yahoo.elide.datastores.hibernate5.HibernateStore;
import com.yahoo.elide.security.executors.BypassPermissionExecutor;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

/**
 * TODO: document!
 */
class WireRepro {

  static void configure(Environment environment) {
    SessionFactory sessionFactory;
    // A SessionFactory is set up once for an application!
    final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
        .configure() // configures settings from hibernate.cfg.xml
        .build();
    try {
      sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
      Elide elide = new Elide.Builder(new HibernateStore(sessionFactory))
          .withAuditLogger(new Slf4jLogger())
          .withPermissionExecutor(BypassPermissionExecutor.class)
          .build();

      ElideResource resource = new ElideResource(elide, "/v2/");
      environment.routingEngine()
          .registerRoutes(resource.routes());
    } catch (Exception e) {
      // The registry would be destroyed by the SessionFactory, but we had trouble building the SessionFactory
      // so destroy it manually.
      StandardServiceRegistryBuilder.destroy(registry);
      throw Throwables.propagate(e);
    }
  }
}
