package utils.workers.async_dao;

import org.hibernate.Session;

public interface DaoServiceTransaction {
    boolean onTransaction(Session s);
}
