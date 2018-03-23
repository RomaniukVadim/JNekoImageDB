package service.img_worker;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.exception.ConstraintViolationException;

import dao.ImageDuplicateProtect;
import dao.ImageId;
import dao.Tag;
import utils.Loggable;

public class Hibernate extends FsAbstractIO implements Loggable {
	private static Hibernate hibernate;

	private SessionFactory currSF;
	private StandardServiceRegistry registry;
	private final HashMap<String, Session> sessions = new HashMap<>();

	public static synchronized Hibernate getInstance() {
		if (hibernate == null) hibernate = new Hibernate();
		return hibernate;
	}

	private Hibernate() {
		super("db");
		initHibernate();
	}

	public void dispose() {
		sessions.values().forEach(session -> session.close());
		currSF.close();
		StandardServiceRegistryBuilder.destroy(registry);
	}

	public void transaction(HibernateTransaction daoServiceTransaction) {
		final Session s = getSession();
		s.beginTransaction();
		try {
			if (daoServiceTransaction.onTransaction(s)) {
				s.getTransaction().commit();
			} else {
				s.getTransaction().rollback();
				s.clear();
				W("Bad transaction: cancelled by user; thread " + Thread.currentThread().getName());
			}
		} catch (ConstraintViolationException e) {
			s.getTransaction().rollback();
			s.clear();
			W("Bad transaction: cancelled by " + e.getClass().getSimpleName() + "; thread " + Thread.currentThread().getName() + "; message: " + e.getMessage());
		}
	}

	public Session getSession() {
		final String threadName = Thread.currentThread().getName();
		if (!sessions.containsKey(threadName)) {
			final Session s = currSF.openSession();
			sessions.put(threadName, s);
		}

		final Session s = sessions.get(threadName);
		if (!s.isOpen()) {
			final Session s1 = currSF.openSession();
			sessions.remove(threadName);
			sessions.put(threadName, s1);
		}

		return s;
	}

	private void initHibernate() {
		if (SecurityService.getAuthData() == null) throw new IllegalThreadStateException("authData cannot be null");
		final byte[] sha512 = SecurityCryptUtils.sha512(SecurityService.getAuthData());

		final String dbURI = "jdbc:h2:" + this.storageDir + ";CIPHER=AES;";
		try {
			final String dbPassword = SecurityCryptUtils.toHex(Arrays.copyOfRange(SecurityCryptUtils.sha256(sha512), 0, 21));
			final Properties prop = new Properties();
			prop.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
			prop.setProperty("hibernate.hbm2ddl.auto", "update");
			prop.setProperty("hibernate.connection.url", dbURI);
			prop.setProperty("hibernate.connection.username", "default");
			prop.setProperty("hibernate.connection.password", dbPassword + " " + dbPassword);
			prop.setProperty("dialect", "org.hibernate.dialect.H2Dialect");
			prop.setProperty("hibernate.show_sql", "true");
			//prop.setProperty("hibernate.format_sql", "true");

			prop.setProperty("connection.provider_class", "org.hibernate.connection.C3P0ConnectionProvider");
			prop.setProperty("hibernate.c3p0.acquire_increment", "1");
			prop.setProperty("hibernate.c3p0.idle_test_period", "30");
			prop.setProperty("hibernate.c3p0.min_size", "1");
			prop.setProperty("hibernate.c3p0.max_size", "2");
			prop.setProperty("hibernate.c3p0.max_statements", "50");
			prop.setProperty("hibernate.c3p0.timeout", "0");
			prop.setProperty("hibernate.c3p0.acquireRetryAttempts", "1");
			prop.setProperty("hibernate.c3p0.acquireRetryDelay", "250");

			prop.setProperty("hibernate.transaction.factory_class", "org.hibernate.transaction.JDBCTransactionFactory");
			prop.setProperty("hibernate.current_session_context_class", "thread");

			final Configuration conf = new Configuration()
					.addProperties(prop)
					.addAnnotatedClass(ImageDuplicateProtect.class)
					.addAnnotatedClass(ImageId.class)
					.addAnnotatedClass(Tag.class)
					.configure();

			registry = new StandardServiceRegistryBuilder()
					.applySettings(conf.getProperties())
					.build();

			currSF = conf.buildSessionFactory(registry);
		} catch (Throwable ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}
}
