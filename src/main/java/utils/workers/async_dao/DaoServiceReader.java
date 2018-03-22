package utils.workers.async_dao;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import dao.ImageDuplicateProtect;
import dao.ImageId;
import utils.Loggable;

public class DaoServiceReader {
	private static AsyncDaoService asyncDaoService;

	protected static void init() {
		asyncDaoService = AsyncDaoService.getInstance();
	}

	public static synchronized boolean hasDuplicates(Path p) {
		final ImageDuplicateProtect ip1 = new ImageDuplicateProtect(p);
		List list = asyncDaoService.getSession()
				.createCriteria(ImageDuplicateProtect.class)
				.add(Restrictions.eq("hashOfNameAndSize", ip1.getHashOfNameAndSize()))
				.list();
		return Objects.nonNull(list) && !list.isEmpty();
	}

	public static synchronized List<ImageId> getImageIdList(long[] in) {
		final Long[] array = new Long[in.length];
		for (int i=0; i<in.length; i++) array[i] = in[i];
		return asyncDaoService.getSession()
				.createCriteria(ImageId.class)
				.add(Restrictions.in("oid", array))
				.addOrder(Order.desc("oid"))
				.list();
	}

	public static long[] generateCache() {
		final List list = asyncDaoService.getSession()
				.createCriteria(ImageId.class)
				.setProjection(Projections.property("oid"))
				.addOrder(Order.desc("oid"))
				.list();
		if (Objects.nonNull(list) && !list.isEmpty() && (list.get(0) instanceof Long)) {
			final long[] array = new long[list.size()];
			for (int i=0; i<list.size(); i++) array[i] = ((Long) list.get(i));
			return array;
		}
		return null;
	}
}
