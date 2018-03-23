package utils.workers.async_dao;

import static service.RootService.DATASTORAGE_ROOT;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
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
import service.img_worker.HibernateTransaction;
import utils.Loggable;
import utils.messages.MessageQueue;
import utils.messages.Msg;
import utils.messages.MultithreadedSingletone;
import service.img_worker.SecurityCryptUtils;
import service.img_worker.SecurityService;

public class AsyncDaoService extends MultithreadedSingletone<AsyncDaoTransaction<?>> implements Loggable {
	private static AsyncDaoService asyncDaoService;
	private static byte[] authData;

	private byte[] masterKey;
	private byte[] iv;
	private String storageName;
	private File storageDir;

	private SessionFactory currSF;
	private StandardServiceRegistry registry;
	private final HashMap<String, Session> sessions = new HashMap<>();

	public static void init() {
		authData = SecurityService.getAuthData();
		asyncDaoService = new AsyncDaoService(authData);
	}

	public static void dispose() {
		if (asyncDaoService != null) asyncDaoService.disposeInstance();
	}

	protected static AsyncDaoService getInstance() {
		return asyncDaoService;
	}

	@Override
	public void disposeInstance() {
		super.disposeInstance();
		sessions.values().forEach(session -> session.close());
		currSF.close();
		StandardServiceRegistryBuilder.destroy(registry);
	}

	protected AsyncDaoService(byte[] authData) {
		super(2);
		initHibernate();
		DaoServiceReader.init();
		MessageQueue.subscribe(SERVICE_UUID, (Msg<AsyncDaoTransaction<?>> msg) -> {
			pushTask(msg.getPayload());
		});
	}

	@Override
	public void processQueue(AsyncDaoTransaction<?> element) {
		switch (element.getType()) {
		case DELETE:
			transaction((s) -> {
				element.getObjects().stream()
						.filter(e -> (e != null) && (e instanceof Serializable))
						.forEach(el -> s.save(el));
				return true;
			});
			break;
		case INSERT:
			transaction((s) -> {
				element.getObjects().stream()
						.filter(e -> (e != null) && (e instanceof Serializable))
						.forEach(el -> s.save(el));
				return true;
			});
			break;
		case UPDATE:

			break;
		}
	}

	private void initHibernate() {
		if (Objects.isNull(authData)) throw new IllegalArgumentException("authData cannot be null");

		final byte[] sha512 = SecurityCryptUtils.sha512(authData);
		this.masterKey = Arrays.copyOfRange(sha512, 16, 48);
		this.iv = Arrays.copyOfRange(sha512, 48, 64);
		this.storageName = SecurityCryptUtils.toHex(Arrays.copyOfRange(SecurityCryptUtils.sha512(sha512), 0, 16));
		final String dirPath = DATASTORAGE_ROOT + File.separator + "databases" + File.separator;
		this.storageDir = new File(dirPath + storageName).getAbsoluteFile();
		new File(dirPath).mkdirs();

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

	protected void transaction(HibernateTransaction daoServiceTransaction) {
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

	protected Session getSession() {
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
}
