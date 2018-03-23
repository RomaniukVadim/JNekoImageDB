package service.img_worker;

import org.hibernate.Session;

public interface HibernateTransaction {
    boolean onTransaction(Session s);
}
