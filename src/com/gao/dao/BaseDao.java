package com.gao.dao;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import javax.annotation.Resource;

import jxl.write.WritableImage;
import jxl.write.WritableSheet;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.internal.OracleClob;
import oracle.sql.BLOB;
import oracle.sql.CLOB;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.cosog.model.scada.CallbackDataItems;
import com.gao.model.Org;
import com.gao.model.gridmodel.CalculateManagerHandsontableChangedData;
import com.gao.model.gridmodel.InverOptimizeHandsontableChangedData;
import com.gao.model.gridmodel.ProductionOutGridPanelData;
import com.gao.model.gridmodel.PumpGridPanelData;
import com.gao.model.gridmodel.ResProHandsontableChangedData;
import com.gao.model.gridmodel.ReservoirPropertyGridPanelData;
import com.gao.model.gridmodel.WellGridPanelData;
import com.gao.model.gridmodel.WellHandsontableChangedData;
import com.gao.model.gridmodel.WellProHandsontableChangedData;
import com.gao.model.gridmodel.WellringGridPanelData;
import com.gao.tast.EquipmentDriverServerTast;
import com.gao.model.WellInformation;
import com.gao.model.Wells;
import com.gao.model.calculate.CommResponseData;
import com.gao.model.calculate.ElectricCalculateResponseData;
import com.gao.model.calculate.TimeEffResponseData;
import com.gao.model.calculate.TimeEffTotalResponseData;
import com.gao.model.calculate.TotalAnalysisRequestData;
import com.gao.model.calculate.TotalAnalysisResponseData;
import com.gao.model.calculate.WellAcquisitionData;
import com.gao.model.drive.RTUDriveConfig;
import com.gao.utils.DataModelMap;
import com.gao.utils.EquipmentDriveMap;
import com.gao.utils.OracleJdbcUtis;
import com.gao.utils.Page;
import com.gao.utils.StringManagerUtils;
/**
 * <p>
 * 描述：核心服务dao处理接口类
 * </p>
 * 
 * @author gao 2014-06-04
 * @since 2013-08-08
 * @version 1.0
 * 
 */
@SuppressWarnings({ "unused", "unchecked", "rawtypes", "deprecation" })
@Repository("baseDao")
public class BaseDao extends HibernateDaoSupport {
	private static Log log = LogFactory.getLog(BaseDao.class);
	private Session session = null;
	public static String ConvertBLOBtoString(Blob BlobContent) {
		byte[] msgContent = null;
		try {
			msgContent = BlobContent.getBytes(1, (int) BlobContent.length());
		} catch (SQLException e1) {
			e1.printStackTrace();
		} // BLOB转换为字节数组
		String newStr = ""; // 返回字符串
		long BlobLength; // BLOB字段长度
		try {
			BlobLength = BlobContent.length(); // 获取BLOB长度
			if (msgContent == null || BlobLength == 0) // 如果为空，返回空值
			{
				return "";
			} else // 处理BLOB为字符串
			{
				newStr = new String(BlobContent.getBytes(1, 900), "gb2312"); // 简化处理，只取前900字节
				return newStr;
			}
		} catch (Exception e) // oracle异常捕获
		{
			e.printStackTrace();
		}
		return newStr;
	}

	/**
	 * @param dataSheet
	 * @param col
	 * @param row
	 * @param width
	 * @param height
	 * @param imgFile
	 */
	public static void insertImg(WritableSheet dataSheet, int col, int row, int width, int height, File imgFile) {
		WritableImage img = new WritableImage(col, row, width, height, imgFile);
		dataSheet.addImage(img);
	}

	@Transactional
	public <T> void addObject(T clazz) {

		this.save(clazz);
	}


	/**
	 * <p>
	 * 描述：批量删除对象信息
	 * </p>
	 * 
	 * @param hql
	 * @throws Exception
	 * 
	 * @author gao 2014-06-06
	 * 
	 */
	@Transactional
	public void bulkObjectDelete(final String hql) throws Exception {
		Session session=getSessionFactory().getCurrentSession();
		Query query = session.createQuery(hql);
		query.executeUpdate();
	}

	/**
	 * <p>
	 * 描述：根据传入的对象类型，删除该对象的一条记录
	 * </p>
	 * 
	 * @param obj
	 *            传入的对象
	 * @return
	 */
	public Serializable delectObject(Object obj) {
		Session session = getSessionFactory().getCurrentSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.delete(obj);
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 存储过程调用删除 传入数组String[] ->删除(带占位符的)
	 * 
	 * @param callSql
	 * @param values
	 * @return
	 */
	public void deleteCallParameter(final String callSql, final Object... values) {
		Query query = getSessionFactory().getCurrentSession().createQuery(callSql);
		for (int i = 0; i < values.length; i++) {
			query.setParameter(i, values[i]);
		}
		query.executeUpdate();
	}

	public Object deleteCallSql(final String sql, final Object... values) {
		Session session = getSessionFactory().getCurrentSession();
				SQLQuery query = session.createSQLQuery(sql);
				for (int i = 0; i < values.length; i++) {
					query.setParameter(i, values[i]);
				}
				return query.executeUpdate();
	}

	public int deleteObject(final String hql) {
		Session session = getSessionFactory().getCurrentSession();
				SQLQuery query = session.createSQLQuery(hql);
				return query.executeUpdate();
	}

	public <T> void deleteObject(T clazz) {
		this.getHibernateTemplate().delete(clazz);
	}

	/**
	 * 传入数组String[] ->删除(带占位符的) (批量)
	 * 
	 * @param queryString
	 * @param parametName
	 * @param parametValue
	 * @return
	 */
	public Query deleteQueryParameter(String queryString, String parametName, Object[] parametValue, Object... values) {
		Query query = getSessionFactory().getCurrentSession().createQuery(queryString);
		query.setParameterList(parametName, parametValue);
		for (int i = 0; i < values.length; i++) {
			query.setParameter(i, values[i]);
		}
		return query;
	}

	public boolean deleteWellTrajectoryInfoById(String track, int line, int jbh) throws SQLException {
		boolean flag = false;
		int ok = -1;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		conn=SessionFactoryUtils.getDataSource(getSessionFactory()).getConnection();
		String sql = "update t_welltrajectory set jsgj=? where jbh=?";
		StringBuffer sb = new StringBuffer();
		ByteArrayInputStream bais = null;
		try {
			String[] tracks = track.split(";");
			for (int i = 0; i < tracks.length; i++) {
				if ((i + 1) == line) {
					continue;
				} else {
					sb.append(tracks[i] + ";");
				}
			}
			ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			byte[] buffer = (sb.toString()).getBytes();
			bais = new ByteArrayInputStream(buffer);
			ps.setBinaryStream(1, bais, bais.available()); // 第二个参数为文件的内容
			ps.setInt(2, jbh);
			ok = ps.executeUpdate();
			if (ok > 0) {
				flag = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {

			try {
				bais.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return flag;
	}

	/**
	 * 修改一个对象
	 * 
	 * @param object
	 */
	@Transactional
	public void edit(Object object) {
		getSessionFactory().getCurrentSession().update(object);
	}

	/**
	 * 执行一个SQL，update或insert
	 * 
	 * @param sql
	 * @return update或insert的记录数
	 */
	public int executeSqlUpdate(String sql) {
		int n = 0;
		Statement stat = null;
		try {
			stat=SessionFactoryUtils.getDataSource(getSessionFactory()).getConnection().createStatement();
			n = stat.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stat != null) {
					stat.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return n;
	}

	/**
	 * HQL查询
	 * 
	 * @param queryString
	 *            HQL语句
	 * @param values
	 *            HQL参数
	 * @return
	 */
	public List find(String queryString, Object... values) {
		Query query = getSessionFactory().getCurrentSession().createQuery(queryString);
		for (int i = 0; i < values.length; i++) {
			query.setParameter(i, values[i]);
		}
		return query.list();
	}

	/**
	 * sql调用查询
	 * 
	 * @param queryString
	 *            callSql语句
	 * @param values
	 *            callSql参数
	 * @author qiands
	 * @return List<?>
	 */
	public List<?> findCallSql(final String callSql, final Object... values) {
		Session session=getSessionFactory().getCurrentSession();
				SQLQuery query = session.createSQLQuery(callSql);
				for (int i = 0; i < values.length; i++) {
					query.setParameter(i, values[i]);
				}
				return query.list();
	}
	
	/*
	 * 查询曲线数据
	 * */
	public List<?> findGtData(final String sql){
		Session session=getSessionFactory().getCurrentSession();
				SQLQuery query = session.createSQLQuery(sql);
				return query.list();
	}
	 
	public <T> List<T> findgObjectByIdSql(final String sql ){
		Session session=getSessionFactory().getCurrentSession();
				Query query = session.createSQLQuery(sql);
				List listQuery = query.list();
				return listQuery;
	}
	
	public <T> List<T> findChartsList(final String hql) throws Exception {
		Session session=getSessionFactory().getCurrentSession();
					Query query = session.createQuery(hql);
					List<T> list = query.list();
					return list;
	}

	public List<Org> findChildOrg(Integer parentId) {
		String queryString = "SELECT u FROM Org u where u.orgParent=" + parentId + " order by u.orgId ";
		return getObjects(queryString);
	}

	/**
	 * SQL查询
	 * 
	 * @param queryString
	 *            HQL语句
	 * @param values
	 *            HQL参数
	 * @author qiands
	 * @return list
	 */

	public List<?> findSql(final String sql, final Object... values) {
		Session session=getSessionFactory().getCurrentSession();
				SQLQuery query = session.createSQLQuery(sql);
				for (int i = 0; i < values.length; i++) {
					query.setParameter(i, values[i]);
				}
				return query.list();
	}
	public <T> List<T> findLeakageSql(final String sql, final Object... values) {
		Session session=getSessionFactory().getCurrentSession();
		SQLQuery query = session.createSQLQuery(sql);
		for (int i = 0; i < values.length; i++) {
			query.setParameter(i, values[i]);
		}
		return query.list();
	}

	/**
	 * 根据ID获取一个对象，如果查不到返回null
	 * 
	 * @param entityClass
	 * @param id
	 *            :查询对象的id
	 * @return <T>
	 */
	public <T> T get(Class<T> entityClass, Serializable id) {
		return (T) getSessionFactory().getCurrentSession().get(entityClass, id);
	}

	public <T> List<T> getAllObjects(Class<T> clazz) {
		return this.getHibernateTemplate().loadAll(clazz);
	}

	/**
	 * HQL Hibernate分页
	 * 
	 * @param hql
	 *            HSQL 查询语句
	 * @param page
	 *            分页条件信息
	 * @return List<T> 查询结果集
	 */
	public <T> List<T> getAllPageByHql(final String hql, final Page page) {
		Session session=getSessionFactory().getCurrentSession();
				Query query = session.createQuery(hql);
				ScrollableResults scrollableResults = query.scroll(ScrollMode.SCROLL_SENSITIVE);
				scrollableResults.last();
				query.setFirstResult(page.getStart());
				query.setMaxResults(page.getLimit());
				page.setTotalCount(scrollableResults.getRowNumber() + 1);
				return query.list();
	}

	/**
	 * 返回当前页的数据信息,执行的是sql查询操作
	 * 
	 * @author gao 2014-05-08
	 * @param sql
	 *            查询的sql语句
	 * @param pager
	 *            分页信息
	 * @param values动态参数
	 * @return list 数据结果集合
	 */
	public <T> List<T> getAllPageBySql(final String sql, final Page pager, final Object... values) {
		Session session=getSessionFactory().getCurrentSession();
				SQLQuery query = session.createSQLQuery(sql);
				/****
				 * 为query对象参数赋值操作
				 * */
				for (int i = 0; i < values.length; i++) {
					query.setParameter(i, values[i]);
				}
				query.setFirstResult(pager.getStart());// 设置起始位置
				query.setMaxResults(pager.getLimit());// 设置分页条数
			 int totals = getTotalCountRows(sql, values);//设置数据表中的总记录数
				pager.setTotalCount(totals);
				return query.list();
	}
	public <T> List<T> getMyCustomPageBySql(final String sqlAll,final String sql, final Page pager, final Object... values) {
		Session session=getSessionFactory().getCurrentSession();
				SQLQuery query = session.createSQLQuery(sql);
				/****
				 * 为query对象参数赋值操作
				 * */
				for (int i = 0; i < values.length; i++) {
					values[i] = values[i].toString().replace("@", ",");
					query.setParameter(i, values[i]);
				}
				int totals = getTotalCountRows(sqlAll, values);//设置数据表中的总记录数
				pager.setTotalCount(totals);
				return query.list();
	}
	//漏失分析获取平均值
	public <T> List<T> getAverageBySql(final String sqlAll,final String[] col, final Object... values) {
		String sql = "select ";
		for(int i = 0; i < col.length;i++){
			String[] attr = col[i].split(" as ");
			if (null != attr && attr.length > 1) {
				col[i] = attr[attr.length-1];
			}
			if( col[i].equals("id") ){
				sql += "(max(" + col[i] + ")+1) as id,";
			}else if( col[i].equals("jssj") ){
				sql += "'平均值' as jssj,";
			}else if( col[i].contains("pf") || col[i].contains("jljh")){
				sql += "avg(" + col[i] + "),";
			}else if( col[i].contains("js" ) && !col[i].equals("jssj") ){
				sql += "avg(" + col[i] + "),";
			}else if( col[i].contains("lsxs") ){
				sql += "decode(avg(" + col[i-1] + "),0,null,null,null," + "avg(" + col[i-2] + ")/avg(" + col[i-1] + ")) as lsxs,";
			}else{
				sql += "null as " + col[i] +",";
			}
		}
		sql = sql.substring(0, sql.length()-1);
		sql += " from (" + sqlAll + ")";
		Session session=getSessionFactory().getCurrentSession();
		SQLQuery query = session.createSQLQuery(sql);
		/**为query对象参数赋值操作 **/
		for (int i = 0; i < values.length; i++) {
			query.setParameter(i, values[i]);
		}
		return query.list();
	}

	/***
	 * *************************************begin
	 * 
	 * @author qiands
	 */
	/**
	 * 分页方法
	 * 
	 * @param sql
	 * @param pager
	 * @return
	 * @author qiands
	 */
	public <T> List<T> getAllPageBySql(final String sql, final Page pager, final Vector<String> v) {
		Session session=getSessionFactory().getCurrentSession();
				SQLQuery query = session.createSQLQuery(sql);
				int index = -1;
				String data = "";
				for (int i = 0; i < v.size(); i++) {
					if (!"".equals(v.get(i)) && null != v.get(i) && v.get(i).length() > 0) {
						index += 1;
						data = v.get(i);
						query.setParameter(index, data);
					}

				}
				ScrollableResults scrollableResults = query.scroll(ScrollMode.SCROLL_SENSITIVE);
				scrollableResults.last();
				query.setFirstResult(pager.getStart());
				query.setMaxResults(pager.getLimit());
				pager.setTotalCount(scrollableResults.getRowNumber() + 1);
				return query.list();
	}

	public void getChildrenList(List parentitem, Integer orgid) {
		List childlist = null;
		try {
			childlist = findChildOrg(orgid.intValue());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (childlist != null && childlist.size() > 0) {
			for (int i = 0; i < childlist.size(); i++) {
				Org bean = (Org) childlist.get(i);
				parentitem.add(bean.getOrgId());
				getChildrenList(parentitem, bean.getOrgId());
			}
		}
	}

	/**
	 * 
	 * @param sql
	 * @return
	 */
	public Integer getCountRows(String sql) {
		Session session=getSessionFactory().getCurrentSession();
		Query query = session.createSQLQuery(sql);
		Integer rows = (Integer) query.list().size();
		return rows;
	}

	/**
	 * <p>
	 * 描述：查询数据库中的记录总数
	 * </p>
	 * 
	 * @param sql
	 * @return
	 */
	public Integer getCountSQLRows(String sql) {
		Session session=getSessionFactory().getCurrentSession();
		SQLQuery query = session.createSQLQuery(sql);
		BigDecimal obj = (BigDecimal) query.uniqueResult();
		return obj.intValue();
	}

	public <T> List<T> getfindByIdList(final String hql) throws Exception {
		Session session=getSessionFactory().getCurrentSession();
					Query query = session.createQuery(hql);
					List<T> list = query.list();
					return list;
	}

	public <T> List<T> getListAndTotalCountForPage(final Page pager, final String hql, final String allhql) throws Exception {
		Session session=getSessionFactory().getCurrentSession();
					Query query = session.createQuery(hql);
			       ScrollableResults scrollableResults = query.scroll(ScrollMode.SCROLL_SENSITIVE);
					scrollableResults.last();
					 int totals = scrollableResults.getRowNumber()+1;
					pager.setTotalCount(totals);
					query.setFirstResult(pager.getStart());
					query.setMaxResults(pager.getLimit());
					List<T> list = query.list();
					return list;
	}

	public <T> List<T> getListAndTotalForPage(final Page pager, final String hql) throws Exception {
		Session session=getSessionFactory().getCurrentSession();
					Query query = session.createQuery(hql);
			         int total = query.list().size();
					pager.setTotalCount(total);
					query.setFirstResult(pager.getStart());
					query.setMaxResults(pager.getLimit());
					List<T> list = query.list();
					return list;
	}

	/**
	 * <p>
	 * 描述：获取记录数据库中的总记录数
	 * </p>
	 * 
	 * @param o
	 * @return
	 */
	public Long getListCountRows(final String o) {
		final String hql = "select count(*) from  " + o;
		Long result = null;
		Session session=getSessionFactory().getCurrentSession();
				Query query = session.createQuery(hql);
				return (Long) query.uniqueResult();
	}

	protected <T> List<T> getListForPage(final Class<T> clazz, final Criterion[] criterions, final int offset, final int length) {
		Session session=getSessionFactory().getCurrentSession();
				Criteria criteria = session.createCriteria(clazz);
				for (int i = 0; i < criterions.length; i++) {
					criteria.add(criterions[i]);
				}
				criteria.setFirstResult(offset);
				criteria.setMaxResults(length);
				return criteria.list();
	}
	
	public <T> List<T> getListForPage(final int offset, final int pageSize,final String hql) throws Exception {
		Session session=getSessionFactory().getCurrentSession();
		Query query = session.createQuery(hql);
		query.setFirstResult(offset);
		query.setMaxResults(pageSize);
		List<T> list = query.list();
		return list;

	}

	/**
	 * @param offset
	 * @param pageSize
	 * @param pager
	 * @param hql
	 * @param o
	 *            出入当前的 实体类对象
	 * @return 返回分页后的数据集合
	 * @throws Exception
	 */
	public <T> List<T> getListForPage(final Page pager, final String hql, final String o) throws Exception {
		Session session=getSessionFactory().getCurrentSession();
					Query query = session.createQuery(hql);
					pager.setTotalCount(Integer.parseInt(getMaxCountValue(o) + ""));
					query.setFirstResult(pager.getStart());
					query.setMaxResults(pager.getLimit());
					List<T> list = query.list();
					return list;
	}

	public <T> List<T> getListForReportPage(final int offset, final int pageSize, final String hql) throws Exception {
		Session session=getSessionFactory().getCurrentSession();
					Query query = session.createSQLQuery(hql);
					query.setFirstResult(offset);
					query.setMaxResults(pageSize);
					List<T> list = query.list();
					return list;
	}

	/**
	 * <p>
	 * 描述：hql查询分页方法
	 * </p>
	 * 
	 * @param offset
	 *            数据偏移量
	 * @param pageSize
	 *            分页大小
	 * @param hql
	 *            查询语句
	 * @return
	 * @throws Exception
	 */
	public <T> List<T> getListPage(final int offset, final int pageSize, final String hql) throws Exception {
		Session session=getSessionFactory().getCurrentSession();
					Query query = session.createSQLQuery(hql);
					query.setFirstResult(offset);
					query.setMaxResults(pageSize);
					List<T> list = query.list();
					return list;
	}

	public Long getMaxCountValue(final String o) {
		Session session=getSessionFactory().getCurrentSession();
		final String hql = "select count(*) from " + o;
				Query query = session.createQuery(hql);
				return (Long) query.uniqueResult();
	}

	public <T> T getObject(Class<T> clazz, Serializable id) {
		return this.getHibernateTemplate().get(clazz, id);
	}

	public List<Wells> getObjectList(String sql) {
		Session session = this.getHibernateTemplate().getSessionFactory().openSession();
		List<Wells> list = null;
		try {
			Transaction tran = session.beginTransaction();
			SQLQuery query = session.createSQLQuery(sql);
			list = query.list();
			tran.commit();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			session.close();
		}
		return list;
	}

	/**
	 * <p>
	 * 描述：根据传入的hql语句返回一个List数据集合
	 * </p>
	 * 
	 * @author gao 2014-06-04
	 * @param hql
	 *            传入的hql语句
	 * @return List<T>
	 */
	public <T> List<T> getObjects(String hql) {
		Session session=getSessionFactory().getCurrentSession();
		return (List<T>) this.getHibernateTemplate().find(hql);
	}
	
	public <T> List<T> getSqlToHqlOrgObjects(String sql) {
		Session session=getSessionFactory().getCurrentSession();
		return (List<T>) session.createSQLQuery(sql).addEntity("Org", Org.class).list();
	}

	
	public <T> List<T> getSQLObjects(final String sql) {
		Session session=getSessionFactory().getCurrentSession();
				SQLQuery query = session.createSQLQuery(sql);
				List list = query.list();
				return (List<T>) list;
	}

	public Integer getRecordCountRows(String hql) {
		Session session=getSessionFactory().getCurrentSession();
		Query query = session.createQuery(hql);
		Integer rows = (Integer) query.list().size();
		return rows;
	}

	/**
	 * <p>根据传入的SQL语句来分页查询List集合</p>
	 * 
	 * @param offset
	 * @param pageSize
	 * @param hql
	 * @return
	 * @throws Exception
	 */
	public <T> List<T> getSQLListForPage(final int offset, final int pageSize, final String hql) throws Exception {
		Session session=getSessionFactory().getCurrentSession();
					Query query = session.createSQLQuery(hql);
					query.setFirstResult(offset);
					query.setMaxResults(pageSize);
					List<T> list = query.list();
					return list;
	}

	/**
	 * <p>
	 * 描述：根据普通的sql来查询一个结果List集合
	 * </p>
	 * 
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public <T> List<T> getSQLList(final String sql) throws Exception {
		Session session=getSessionFactory().getCurrentSession();
					Query query = session.createSQLQuery(sql);
					List<T> list = query.list();
					return list;
	}

	public Integer getTotalCountRows(String sql, final Object... values) {
		String allsql="";
		if(sql.trim().indexOf("count(1)")>0||sql.trim().indexOf("count(*)")>0){
			allsql=sql;
			
		}else{
			if(sql.indexOf("distinct")>0||sql.indexOf("group")>0){
				allsql = "select count(1)  from  (" + sql + ")";
			}else{
				String strarr[]=sql.split("from");
				if(strarr.length>1){
					allsql="select count(1) ";
					for(int i=1;i<strarr.length;i++){
						allsql+="from "+strarr[i];
					}
				}else{
					allsql = "select count(1)  from  (" + sql + ")";
				}
			}
		}
		Integer rows =0;
			SQLQuery query = getSessionFactory().getCurrentSession().createSQLQuery(allsql);
			for (int i = 0; i < values.length; i++) {
				values[i] = values[i].toString().replace("@", ",");
				query.setParameter(i, values[i]);
			}
			rows= Integer.parseInt(query.uniqueResult() + "");
		return rows;
	}

	public Long getTotalCountValue(final String o) {
		Session session=getSessionFactory().getCurrentSession();
				Query query = session.createQuery(o);
				return (Long) query.uniqueResult();
	}

	public Integer getTotalSqlCountRows(String sql, final Object... values) {
		String allsql = "select count(*)  from  (" + sql + ")";
		Query query = getSessionFactory().getCurrentSession().createSQLQuery(allsql);
		for (int i = 0; i < values.length; i++) {
			query.setParameter(i, values[i]);
		}
		List<BigDecimal> list = query.list();
		int count = list.get(0).intValue();
		return count;
	}

	/**
	 * @param orgId
	 * @return 递归取出当前组织Id下的所有Id字符串集合
	 */
	public String getUserOrgIds(int orgId) {
		List childOrgList = new ArrayList();
		String orgIds = orgId + ",";
		getChildrenList(childOrgList, orgId);
		for (int i = 0; i < childOrgList.size(); i++) {
			orgIds = orgIds + childOrgList.get(i) + ",";
		}
		orgIds = orgIds.substring(0, orgIds.length() - 1);
		return orgIds;
	}

	public List<Wells> getWellList(int orgId) {
		String queryString = "select w from Wells as w,Org as o where w.dwbh = o.orgCode and o.orgId=" + orgId;
		Session session = this.getHibernateTemplate().getSessionFactory().openSession();
		List<Wells> list = null;
		try {
			Transaction tran = session.beginTransaction();

			log.debug("queryString" + queryString);
			list = session.createQuery(queryString).list();
			tran.commit();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			session.close();
		}
		return list;
	}

	public <T> List<T> getWellListForPage(final int offset, final int pageSize, final String hql, final int orgId) throws Exception {
		Session session=getSessionFactory().getCurrentSession();
					Query query = session.createQuery(hql);
					query.setInteger("orgId", orgId);
					query.setFirstResult(offset);
					query.setMaxResults(pageSize);
					List<T> list = query.list();
					return list;
	}

	public Serializable insertObject(Object obj) {
		Session session=getSessionFactory().getCurrentSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(obj);
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			e.printStackTrace();
		}
		return null;
	}

	public Serializable modifyByObject(String hql) {
		Session session=getSessionFactory().getCurrentSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Query query = session.createQuery(hql);
			query.executeUpdate();
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			e.printStackTrace();
		}
		return null;
	}

	public Serializable modifyObject(Object obj) {
		Session session=getSessionFactory().getCurrentSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.update(obj);
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			e.printStackTrace();
		}
		return null;
	}

	public List MonthJssj(final String sql) {
		Session session=getSessionFactory().getCurrentSession();
				Query query = session.createSQLQuery(sql);
				List list = query.list();
				return list;
	}

	public Integer queryProObjectTotals(String sql) throws SQLException {
		ResultSet rs = null;
		PreparedStatement ps = null;
		int total = 0;
		Connection conn=SessionFactoryUtils.getDataSource(getSessionFactory()).getConnection();
		try {
			ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			rs = ps.executeQuery();
			while (rs.next()) {
				total = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return total;
	}

	public <T> List<T> queryProReservoirPropertyDatasHql(int offset, int pageSize, String sql) {
		List<T> list = null;
		try {
			Session session = this.getHibernateTemplate().getSessionFactory().getCurrentSession();
			SQLQuery query = session.createSQLQuery("{call PRO_QUERY_OBJECTDATA(?,?) }");
			query.setFirstResult(offset);
			query.setMaxResults(pageSize);
			query.setString(1, sql);
			query.setParameter(2, oracle.jdbc.OracleTypes.CURSOR);
			query.executeUpdate();
			list = query.list();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public List<WellInformation> queryWellInformationDatas(String sql) throws SQLException {
		List<WellInformation> list = new ArrayList<WellInformation>();
		ResultSet rs = null;
		WellInformation well = null;
		Connection conn=SessionFactoryUtils.getDataSource(getSessionFactory()).getConnection();
		CallableStatement cs;
		try {
			cs = conn.prepareCall("{ call PRO_QUERY_OBJECTDATA(?,?) }");
			cs.setString(1, sql);
			cs.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR);
			cs.execute();
			rs = (ResultSet) cs.getObject(2);
			while (rs.next()) {
				well = new WellInformation();
				well.setJlbh(rs.getInt(1));
				well.setDwbh(rs.getString(2));
				well.setJc(rs.getString(3));
				well.setJhh(rs.getString(4));
				well.setJh(rs.getString(5));
				well.setJlx(rs.getInt(6));
				well.setSsjw(rs.getInt(7));
				well.setSszcdy(rs.getInt(8));
				well.setRgzsjd(rs.getString(9));
				well.setOrgName(rs.getString(10));
				well.setResName(rs.getString(11));
				well.setYqcbh(rs.getString(12));
				well.setRgzsj(rs.getDouble("rgzsj"));
				list.add(well);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			rs.close();
			conn.close();
		}
		return list;
	}


	/**
	 * 新增一个对象
	 * 
	 * @param object
	 */
	@Transactional
	public void save(Object object) {
		getSessionFactory().getCurrentSession().save(object);
	}

	public <T> void saveOrUpdateObject(T clazz) {
		this.getHibernateTemplate().saveOrUpdate(clazz);
	}
	public int updateOrDeleteBySql(String sql) throws SQLException{
		Connection conn=null;
		PreparedStatement ps=null;
		int result=0;
		try {
			conn = SessionFactoryUtils.getDataSource(getSessionFactory()).getConnection();
			ps=conn.prepareStatement(sql);
			result=ps.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			if(ps!=null){
				ps.close();
			}
			if(conn!=null){
				conn.close();
			}
		}
		
		return result;
	}
	
	@SuppressWarnings("resource")
	public Boolean saveWellEditerGridData(WellHandsontableChangedData wellHandsontableChangedData,String orgIds,String orgId) throws SQLException {
		Connection conn=SessionFactoryUtils.getDataSource(getSessionFactory()).getConnection();
		CallableStatement cs=null;
		PreparedStatement ps=null;
		Map<String, Object> equipmentDriveMap = EquipmentDriveMap.getMapObject();
		if(equipmentDriveMap.size()==0){
			EquipmentDriverServerTast.initDriverConfig();
		}
		try {
			cs = conn.prepareCall("{call prd_save_wellinformation(?,?,?,?,?,?,?,?,?,?,?,?,?)}");
			if(wellHandsontableChangedData.getUpdatelist()!=null){
				for(int i=0;i<wellHandsontableChangedData.getUpdatelist().size();i++){
					if(StringManagerUtils.isNotNull(wellHandsontableChangedData.getUpdatelist().get(i).getWellName())){
						String driverName=wellHandsontableChangedData.getUpdatelist().get(i).getDriverName();
						String driverCode="";
						for(Entry<String, Object> entry:equipmentDriveMap.entrySet()){
							RTUDriveConfig driveConfig=(RTUDriveConfig)entry.getValue();
							if(driverName.equals(driveConfig.getDriverName())){
								driverCode=driveConfig.getDriverCode();
								break;
							}
						}
						
						cs.setString(1, wellHandsontableChangedData.getUpdatelist().get(i).getOrgName());
						cs.setString(2, wellHandsontableChangedData.getUpdatelist().get(i).getWellName());
						cs.setString(3, wellHandsontableChangedData.getUpdatelist().get(i).getUnitTypeName());
						cs.setString(4, driverCode);
						cs.setString(5, wellHandsontableChangedData.getUpdatelist().get(i).getDriverAddr());
						cs.setString(6, wellHandsontableChangedData.getUpdatelist().get(i).getDriverId());
						cs.setString(7, wellHandsontableChangedData.getUpdatelist().get(i).getAcqcycle_diagram());
						cs.setString(8, wellHandsontableChangedData.getUpdatelist().get(i).getAcqcycle_discrete());
						cs.setString(9, wellHandsontableChangedData.getUpdatelist().get(i).getSavecycle_discrete());
						cs.setString(10, wellHandsontableChangedData.getUpdatelist().get(i).getVideoUrl());
						cs.setString(11, wellHandsontableChangedData.getUpdatelist().get(i).getSortNum());
						cs.setString(12, orgIds);
						cs.setString(13, orgId);
						cs.executeUpdate();
					}
				}
			}
			if(wellHandsontableChangedData.getInsertlist()!=null){
				for(int i=0;i<wellHandsontableChangedData.getInsertlist().size();i++){
					if(StringManagerUtils.isNotNull(wellHandsontableChangedData.getInsertlist().get(i).getWellName())){
						String driverName=wellHandsontableChangedData.getInsertlist().get(i).getDriverName();
						String driverCode="";
						for(Entry<String, Object> entry:equipmentDriveMap.entrySet()){
							RTUDriveConfig driveConfig=(RTUDriveConfig)entry.getValue();
							if(driverName.equals(driveConfig.getDriverName())){
								driverCode=driveConfig.getDriverCode();
								break;
							}
						}
						
						cs.setString(1, wellHandsontableChangedData.getInsertlist().get(i).getOrgName());
						cs.setString(2, wellHandsontableChangedData.getInsertlist().get(i).getWellName());
						cs.setString(3, wellHandsontableChangedData.getInsertlist().get(i).getUnitTypeName());
						cs.setString(4, driverCode);
						cs.setString(5, wellHandsontableChangedData.getInsertlist().get(i).getDriverAddr());
						cs.setString(6, wellHandsontableChangedData.getInsertlist().get(i).getDriverId());
						cs.setString(7, wellHandsontableChangedData.getInsertlist().get(i).getAcqcycle_diagram());
						cs.setString(8, wellHandsontableChangedData.getInsertlist().get(i).getAcqcycle_discrete());
						cs.setString(9, wellHandsontableChangedData.getInsertlist().get(i).getSavecycle_discrete());
						cs.setString(10, wellHandsontableChangedData.getInsertlist().get(i).getVideoUrl());
						cs.setString(11, wellHandsontableChangedData.getInsertlist().get(i).getSortNum());
						cs.setString(12, orgIds);
						cs.setString(13, orgId);
						
						cs.executeUpdate();
					}
				}
			}
			if(wellHandsontableChangedData.getDelidslist()!=null){
				String delIds="";
				String delSql="";
				for(int i=0;i<wellHandsontableChangedData.getDelidslist().size();i++){
					delIds+=wellHandsontableChangedData.getDelidslist().get(i);
					if(i<wellHandsontableChangedData.getDelidslist().size()-1){
						delIds+=",";
					}
				}
				delSql="delete from tbl_wellinformation t where t.id in ("+delIds+")";
				ps=conn.prepareStatement(delSql);
				int result=ps.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			if(ps!=null){
				ps.close();
			}
			if(cs!=null){
				cs.close();
			}
			conn.close();
		}
		return true;
	}
	
	public Boolean editWellName(String oldWellName,String newWellName,String orgid) throws SQLException {
		Connection conn=SessionFactoryUtils.getDataSource(getSessionFactory()).getConnection();
		CallableStatement cs=null;
		try {
			cs = conn.prepareCall("{call prd_change_wellname(?,?,?)}");
			cs.setString(1,oldWellName);
			cs.setString(2, newWellName);
			cs.setString(3, orgid);
			cs.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			if(cs!=null){
				cs.close();
			}
			conn.close();
		}
		return true;
	}
	
	public Boolean saveCBMDailyCalculationData(TimeEffResponseData timeEffResponseData,CommResponseData commResponseData) throws SQLException {
		Connection conn=SessionFactoryUtils.getDataSource(getSessionFactory()).getConnection();
		CallableStatement cs=null;
		try {
			cs = conn.prepareCall("{call prd_save_cbmDailyData(?,?,?,?,?,?,?,?,?,?)}");
			cs.setString(1,timeEffResponseData.getWellName());
			
			cs.setInt(2, commResponseData.getDaily().getCommStatus()?1:0);
			cs.setFloat(3, commResponseData.getDaily().getCommEfficiency().getTime());
			cs.setFloat(4, commResponseData.getDaily().getCommEfficiency().getEfficiency());
			cs.setString(5, commResponseData.getDaily().getCommEfficiency().getRangeString());
			
			cs.setInt(6, timeEffResponseData.getDaily().getRunStatus()?1:0);
			cs.setFloat(7, timeEffResponseData.getDaily().getRunEfficiency().getTime());
			cs.setFloat(8, timeEffResponseData.getDaily().getRunEfficiency().getEfficiency());
			cs.setString(9, timeEffResponseData.getDaily().getRunEfficiency().getRangeString());
			
			cs.setString(10, timeEffResponseData.getDaily().getDate());
			cs.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			if(cs!=null){
				cs.close();
			}
			conn.close();
		}
		return true;
	}
	
	public Boolean savePumpEditerGridData(PumpGridPanelData p, String ids, String comandType) throws SQLException {
		Connection conn=SessionFactoryUtils.getDataSource(getSessionFactory()).getConnection();
		CallableStatement cs=null;
		try {
			cs = conn.prepareCall("{call PRO_savePumpData(?,?,?,?,?,?,?)}");
			cs.setString(1, p.getSccj());
			cs.setString(2, p.getCybxh());
			cs.setString(3, p.getBlxName());
			cs.setString(4, p.getBjbName());
			cs.setString(5, p.getBtlxName());
			cs.setDouble(6, p.getBj());
			cs.setDouble(7, p.getZsc());
			cs.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			if(cs!=null){
				cs.close();
			}
			conn.close();
		}
		return true;
	}
	
	public Boolean savePumpingUnitEditerGridData(JSONObject jsonObject) throws SQLException {
		Connection conn=SessionFactoryUtils.getDataSource(getSessionFactory()).getConnection();
		CallableStatement cs=null;
		try {
			cs = conn.prepareCall("{call PRO_savepumpingunitdata(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
			cs.setInt(1, StringManagerUtils.getJSONObjectInt(jsonObject,"id"));
			cs.setString(2, StringManagerUtils.getJSONObjectString(jsonObject, "sccj"));
			cs.setString(3, StringManagerUtils.getJSONObjectString(jsonObject, "cyjxh"));
			cs.setDouble(4, StringManagerUtils.getJSONObjectDouble(jsonObject, "xdedzh"));
			cs.setDouble(5, StringManagerUtils.getJSONObjectDouble(jsonObject, "jsqednj"));
			cs.setDouble(6, StringManagerUtils.getJSONObjectDouble(jsonObject, "dkqbzl"));
			cs.setDouble(7, StringManagerUtils.getJSONObjectDouble(jsonObject, "qbzxbj"));
			cs.setDouble(8, StringManagerUtils.getJSONObjectDouble(jsonObject, "jgbphz"));
			cs.setString(9, StringManagerUtils.getJSONObjectString(jsonObject, "dkphkzl"));
			cs.setDouble(10, StringManagerUtils.getJSONObjectDouble(jsonObject, "qbpzj"));
			cs.setString(11, StringManagerUtils.getJSONObjectString(jsonObject, "xzfxmc"));
			cs.setDouble(12, StringManagerUtils.getJSONObjectDouble(jsonObject, "zdtzjl"));
			cs.setInt(13, StringManagerUtils.getJSONObjectInt(jsonObject,"bpphks"));
			cs.setString(14, StringManagerUtils.getJSONObjectString(jsonObject, "cyjlxmc"));
			cs.setDouble(15, StringManagerUtils.getJSONObjectDouble(jsonObject, "pdxl"));
			cs.setDouble(16, StringManagerUtils.getJSONObjectDouble(jsonObject, "jsxxl"));
			cs.setDouble(17, StringManagerUtils.getJSONObjectDouble(jsonObject, "slgxl"));
			cs.setDouble(18, StringManagerUtils.getJSONObjectDouble(jsonObject, "djxl"));
			cs.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			if(cs!=null){
				cs.close();
			}
			conn.close();
		}
		return true;
	}
	
	
	public Boolean saveBalanceAlarmStatusGridData(JSONObject jsonObject) throws SQLException {
		Connection conn=SessionFactoryUtils.getDataSource(getSessionFactory()).getConnection();
		CallableStatement cs=null;
		try {
			cs = conn.prepareCall("{call PRO_T_205_SAVEBALANCELIMITDATA(?,?,?)}");
			cs.setInt(1, StringManagerUtils.getJSONObjectInt(jsonObject,"id"));
			cs.setDouble(2, StringManagerUtils.getJSONObjectDouble(jsonObject, "minvalue"));
			cs.setDouble(3, StringManagerUtils.getJSONObjectDouble(jsonObject, "maxvalue"));
			cs.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			if(cs!=null){
				cs.close();
			}
			conn.close();
		}
		return true;
	}
	
	public Boolean doStatItemsSetSave(String statType,String data) throws SQLException {
		Connection conn=SessionFactoryUtils.getDataSource(getSessionFactory()).getConnection();
		Statement st=null; 
		JSONObject jsonObject = JSONObject.fromObject("{\"data\":"+data+"}");//解析数据
		JSONArray jsonArray = jsonObject.getJSONArray("data");
		
		try {
			st=conn.createStatement(); 
			if(!"GLPHD".equalsIgnoreCase(statType)&&!"PHD".equalsIgnoreCase(statType)){
				String sql="delete from tbl_statistics_conf t where t.s_type='"+statType+"'";
				int updatecount=st.executeUpdate(sql);
			}
			
			for(int i=0;i<jsonArray.size();i++){
				JSONObject everydata = JSONObject.fromObject(jsonArray.getString(i));
				String statitem=everydata.getString("statitem");
				String downlimit=everydata.getString("downlimit");
				String uplimit=everydata.getString("uplimit");
				String sql="insert into tbl_statistics_conf(s_level,s_min,s_max,s_type) values('"+statitem+"',"+downlimit+","+uplimit+",'"+statType+"')";
				if("GLPHD".equalsIgnoreCase(statType)||"PHD".equalsIgnoreCase(statType)){
					sql="update tbl_statistics_conf set s_min="+downlimit+",s_max="+uplimit+" where s_type='"+statType+"' and s_level='"+statitem+"'";
				}
				int updatecount=st.executeUpdate(sql);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(st!=null)
				st.close();
			conn.close();
		}
		return true;
	}

	/**
	 * 注入sessionFactory
	 */
	@Resource(name = "sessionFactory")
	public void setSuperSessionFactory(SessionFactory sessionFactory) {
		super.setSessionFactory(sessionFactory);
	}

	public int updatealarmmessage(final String sql) throws Exception {
		Session session=getSessionFactory().getCurrentSession();
					SQLQuery query = session.createSQLQuery(sql);
					return query.executeUpdate();

	}

	public int updateBySQL(final String sql, final List pl) throws Exception {
		Session session=getSessionFactory().getCurrentSession();
					SQLQuery query = session.createSQLQuery(sql);
					if (pl != null && !pl.isEmpty()) {
						for (int i = 0; i < pl.size(); i++) {
							query.setParameter(i, pl.get(i));
						}
						return query.executeUpdate();
					}
					return 0;
	}

	/**
	 * 跟新当前传入的数据信息
	 * 
	 * @author ding
	 * @param sql
	 * @return 
	 */
	public Object updateObject(final String sql) {
		Session session=getSessionFactory().getCurrentSession();
				SQLQuery query = session.createSQLQuery(sql);
				return query.executeUpdate();
	}

	/**
	 * <p>
	 * 更新当前对象的数据信息
	 * </p>
	 * 
	 * @author gao 2014-06-04
	 * @param clazz
	 *            传入的对象
	 */
	public <T> void updateObject(T clazz) {
		this.getHibernateTemplate().update(clazz);
	}

	public int updateWellorder(final String hql) {
		Session session=getSessionFactory().getCurrentSession();
				SQLQuery query = session.createSQLQuery(hql);
				return query.executeUpdate();
	}
	
	public boolean updateWellorder(JSONObject everydata,String orgid) throws SQLException {
		Connection conn=null;
		try {
			conn=SessionFactoryUtils.getDataSource(getSessionFactory()).getConnection();
			CallableStatement cs;
			cs = conn.prepareCall("{call PRO_SAVEWELLORDER(?,?,?)}");
			cs.setString(1, everydata.getString("jh"));
			cs.setInt(2, everydata.getInt("pxbh"));
			cs.setString(3, orgid);
			cs.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}finally{
			if(conn!=null){
				conn.close();
			}
		}
		return true;
	}

	public void callProcedureByCallName() {
		String callName = "{Call proc_test(?,?)}";
		ResultSet rs = null;
		CallableStatement call=null;
		try {
			call=SessionFactoryUtils.getDataSource(getSessionFactory()).getConnection().prepareCall(callName);
			call.setString(1, "");
			call.registerOutParameter(2, Types.VARCHAR);
			rs = call.executeQuery();
		}catch (HibernateException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 调用存储过程 删除流程相关记录
	public boolean deleteByCallPro(String procinstid) throws SQLException {
		String procdure = "{Call sp_deleteInstByRootID(?)}";
		CallableStatement cs = null;
		try {
			cs = SessionFactoryUtils.getDataSource(getSessionFactory()).getConnection().prepareCall(procdure);
			cs.setString(1, procinstid);
		} catch (HibernateException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return cs.execute();
	}

	public Boolean setAlarmLevelColor(String alarmLevelBackgroundColor0,String alarmLevelBackgroundColor1,String alarmLevelBackgroundColor2,String alarmLevelBackgroundColor3,
			String alarmLevelColor0,String alarmLevelColor1,String alarmLevelColor2,String alarmLevelColor3,
			String alarmLevelOpacity0,String alarmLevelOpacity1,String alarmLevelOpacity2,String alarmLevelOpacity3) throws SQLException {
		Connection conn=SessionFactoryUtils.getDataSource(getSessionFactory()).getConnection();
		CallableStatement cs=null;
		try {
			cs = conn.prepareCall("{call prd_save_alarmcolor(?,?,?,?,?,?,?,?,?,?,?,?)}");
			cs.setString(1, alarmLevelBackgroundColor0);
			cs.setString(2, alarmLevelBackgroundColor1);
			cs.setString(3, alarmLevelBackgroundColor2);
			cs.setString(4, alarmLevelBackgroundColor3);
			cs.setString(5, alarmLevelColor0);
			cs.setString(6, alarmLevelColor1);
			cs.setString(7, alarmLevelColor2);
			cs.setString(8, alarmLevelColor3);
			
			cs.setString(9, alarmLevelOpacity0);
			cs.setString(10, alarmLevelOpacity1);
			cs.setString(11, alarmLevelOpacity2);
			cs.setString(12, alarmLevelOpacity3);
			cs.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			if(cs!=null){
				cs.close();
			}
			conn.close();
		}
		return true;
	}
	
	
	public Boolean saveSurfaceCard(WellAcquisitionData wellAcquisitionData) throws SQLException, ParseException {
		Connection conn=SessionFactoryUtils.getDataSource(getSessionFactory()).getConnection();
		CallableStatement cs=null;
		StringBuffer gtsjBuff=new StringBuffer();
		SimpleDateFormat dateFormat1=new SimpleDateFormat("yyyyMMdd_HHmmss");
        SimpleDateFormat dateFormat2=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String wellname=wellAcquisitionData.getWellName();
		String cjsjstr=wellAcquisitionData.getAcquisitionTime();
		
		Date date=dateFormat2.parse(wellAcquisitionData.getAcquisitionTime());
		String cjsjstr2=dateFormat1.format(date);
		CLOB gtClob=null;
		gtClob=new CLOB((OracleConnection) conn);
		gtClob = oracle.sql.CLOB.createTemporary(conn,false,1);
		if(wellAcquisitionData.getDiagram()!=null){
			gtsjBuff.append(cjsjstr2.split("_")[0]).append("\r\n").append(cjsjstr2.split("_")[1]).append("\r\n");
			gtsjBuff.append(wellAcquisitionData.getDiagram().getS().size()+"\r\n");
			gtsjBuff.append(wellAcquisitionData.getDiagram().getSPM()+"\r\n");
			gtsjBuff.append(wellAcquisitionData.getDiagram().getStroke()+"\r\n");
			for(int i=0;i<wellAcquisitionData.getDiagram().getS().size();i++){
				gtsjBuff.append(wellAcquisitionData.getDiagram().getS().get(i)+"\r\n"+wellAcquisitionData.getDiagram().getF().get(i));
				if(i<wellAcquisitionData.getDiagram().getS().size()-1){
					gtsjBuff.append("\r\n");
				}
			}
			gtClob.putString(1, gtsjBuff.toString());
		}else{
			gtClob.putString(1, "");
		}
		try {
			cs = conn.prepareCall("{call PRO_SAVEACQFSDiagram(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
			cs.setString(1, wellname);
			cs.setString(2,cjsjstr);
			if(wellAcquisitionData.getDiagram()!=null){
				cs.setInt(3, wellAcquisitionData.getDiagram().getAcquisitionCycle());
				cs.setClob(4, gtClob);
				cs.setFloat(5, wellAcquisitionData.getDiagram().getStroke());
				cs.setFloat(6, wellAcquisitionData.getDiagram().getSPM());
				cs.setString(7, null);
				cs.setString(8, null);
				cs.setString(9, StringUtils.join(wellAcquisitionData.getDiagram().getS(), ","));
				cs.setString(10, StringUtils.join(wellAcquisitionData.getDiagram().getF(), ","));
				cs.setString(11, StringUtils.join(wellAcquisitionData.getDiagram().getP(), ","));
				cs.setString(12, StringUtils.join(wellAcquisitionData.getDiagram().getA(), ","));
			}else{
				cs.setInt(3, 0);
				cs.setClob(4, gtClob);
				cs.setFloat(5, 0);
				cs.setFloat(6, 0);
				cs.setString(7, null);
				cs.setString(8, null);
				cs.setString(9, null);
				cs.setString(10, null);
				cs.setString(11, null);
				cs.setString(12, null);
			}
			cs.setString(13, null);
			cs.setString(14, null);
			cs.setString(15, null);
			cs.setString(16, null);
			
			cs.setFloat(17, wellAcquisitionData.getElectric().getCurrentA());
			cs.setFloat(18, wellAcquisitionData.getElectric().getCurrentB());
			cs.setFloat(19, wellAcquisitionData.getElectric().getCurrentC());
			cs.setFloat(20, wellAcquisitionData.getElectric().getVoltageA());
			cs.setFloat(21, wellAcquisitionData.getElectric().getVoltageB());
			cs.setFloat(22, wellAcquisitionData.getElectric().getVoltageC());
			cs.setFloat(23, wellAcquisitionData.getElectric().getActivePowerConsumption());
			cs.setFloat(24, wellAcquisitionData.getElectric().getReactivePowerConsumption());
			cs.setFloat(25, wellAcquisitionData.getElectric().getActivePower());
			cs.setFloat(26, wellAcquisitionData.getElectric().getReactivePower());
			cs.setFloat(27, wellAcquisitionData.getElectric().getReversePower());
			cs.setFloat(28, wellAcquisitionData.getElectric().getPowerFactor());
			cs.setFloat(29, wellAcquisitionData.getProductionParameter().getTubingPressure());
			cs.setFloat(30, wellAcquisitionData.getProductionParameter().getCasingPressure());
			cs.setFloat(31, wellAcquisitionData.getProductionParameter().getBackPressure());
			cs.setFloat(32, wellAcquisitionData.getProductionParameter().getWellHeadFluidTemperature());
			cs.setFloat(33, wellAcquisitionData.getProductionParameter().getBpszpl());
			cs.setFloat(34, wellAcquisitionData.getProductionParameter().getBpyxpl());
			if(wellAcquisitionData.getScrewPump()!=null){
				cs.setFloat(35, wellAcquisitionData.getScrewPump().getRPM());
				cs.setFloat(36, wellAcquisitionData.getScrewPump().getTorque());
			}else{
				cs.setFloat(35, 0);
				cs.setFloat(36, 0);
			}
			
			cs.setString(37, null);
			cs.setString(38, null);
			cs.setString(39, null);
			cs.setString(40, null);
			cs.setString(41, null);
			cs.setString(42, null);
			
			cs.setString(43, null);
			cs.setString(44, null);
			cs.setString(45, null);
			cs.setString(46, null);
			cs.setString(47, null);
			cs.setString(48, null);
			cs.setString(49, null);
			cs.setString(50, null);
			cs.setInt(51, 0);
			
			cs.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}finally{
			if(cs!=null)
				cs.close();
			conn.close();
		}
		return true;
	}
	
	public Boolean saveTotalCalculationData(TotalAnalysisResponseData totalAnalysisResponseData,TotalAnalysisRequestData totalAnalysisRequestData,String tatalDate) throws SQLException, ParseException {
		Connection conn=SessionFactoryUtils.getDataSource(getSessionFactory()).getConnection();
		CallableStatement cs = null;
		try{
			cs = conn.prepareCall("{call PRO_SAVEAggregationDATA(?,?,?,?,?,?,?,?,?,?,?,?,?,"          //13
					+ "?,?,?,?,?,?,?,?,?,"                                               //9
					+ "?,?,?,?,?,?,?,?,?,?,?,?,"                                         //12
					+ "?,?,?,?,?,?,?,?,?,?,?,?,"                                         //12
					+ "?,?,?,?,?,?,?,?,?,?,?,?,"                                         //12
					+ "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"                                   //15
					+ "?,?,?,?,?,?,?,?,?,?,?,?,"                                         //12
					+ "?,?,?,?,?,?,"                                                     //6
					+ "?,?,"                                                              //2
					+ "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"                      //22
					+ "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"                                    //15
					+ "?,?,"                                                             //2
					+ "?,?,"                                                             //2
					+ "?)}");                                                            //1
			cs.setString(1, totalAnalysisRequestData.getWellName());//井名
			cs.setInt(2, totalAnalysisResponseData.getResultStatus());//计算状态
			
			cs.setInt(3, totalAnalysisResponseData.getCommStatus());//通信状态
			cs.setFloat(4, totalAnalysisResponseData.getCommTime());//在线时间
			cs.setFloat(5, totalAnalysisResponseData.getCommTimeEfficiency());//在线时率
			cs.setString(6, totalAnalysisResponseData.getCommRange());//在线区间
			
			cs.setInt(7, totalAnalysisResponseData.getRunStatus());//运行状态
			cs.setFloat(8, totalAnalysisResponseData.getRunTime());//运行时间
			cs.setFloat(9, totalAnalysisResponseData.getRunTimeEfficiency());//运行时率
			cs.setString(10, totalAnalysisResponseData.getRunRange());//运行区间
			cs.setInt(11, totalAnalysisResponseData.getFSResultCode());//功图工况代码
			cs.setString(12, totalAnalysisResponseData.getFSResultString());//功图工况字符串
			cs.setInt(13, totalAnalysisResponseData.getExtendedDays());//功图延用天数
			
			//冲程、冲次、充满系数
			cs.setFloat(14, totalAnalysisResponseData.getStroke().getValue());//冲程
			cs.setFloat(15, totalAnalysisResponseData.getStroke().getMax());//冲程最大值
			cs.setFloat(16, totalAnalysisResponseData.getStroke().getMin());//冲程最小值
			cs.setFloat(17, totalAnalysisResponseData.getSPM().getValue());//冲次
			cs.setFloat(18, totalAnalysisResponseData.getSPM().getMax());//冲次最大值
			cs.setFloat(19, totalAnalysisResponseData.getSPM().getMin());//冲次最小值
			cs.setFloat(20, totalAnalysisResponseData.getFullnessCoefficient().getValue());//充满系数
			cs.setFloat(21, totalAnalysisResponseData.getFullnessCoefficient().getMax());//充满系数最大值
			cs.setFloat(22, totalAnalysisResponseData.getFullnessCoefficient().getMin());//充满系数最小值
			
			//油压、套压、井口油温、生产气油比
			cs.setFloat(23, totalAnalysisResponseData.getTubingPressure().getValue());
			cs.setFloat(24, totalAnalysisResponseData.getTubingPressure().getMax());
			cs.setFloat(25, totalAnalysisResponseData.getTubingPressure().getMin());
			cs.setFloat(26, totalAnalysisResponseData.getCasingPressure().getValue());
			cs.setFloat(27, totalAnalysisResponseData.getCasingPressure().getMax());
			cs.setFloat(28, totalAnalysisResponseData.getCasingPressure().getMin());
			cs.setFloat(29, totalAnalysisResponseData.getWellHeadFluidTemperature().getValue());
			cs.setFloat(30, totalAnalysisResponseData.getWellHeadFluidTemperature().getMax());
			cs.setFloat(31, totalAnalysisResponseData.getWellHeadFluidTemperature().getMin());
			cs.setFloat(32, totalAnalysisResponseData.getProductionGasOilRatio().getValue());
			cs.setFloat(33, totalAnalysisResponseData.getProductionGasOilRatio().getMax());
			cs.setFloat(34, totalAnalysisResponseData.getProductionGasOilRatio().getMin());
			
			//体积-产液、产油、产水、含水
			cs.setFloat(35, totalAnalysisResponseData.getLiquidVolumetricProduction().getValue());
			cs.setFloat(36, totalAnalysisResponseData.getLiquidVolumetricProduction().getMax());
			cs.setFloat(37, totalAnalysisResponseData.getLiquidVolumetricProduction().getMin());
			cs.setFloat(38, totalAnalysisResponseData.getOilVolumetricProduction().getValue());
			cs.setFloat(39, totalAnalysisResponseData.getOilVolumetricProduction().getMax());
			cs.setFloat(40, totalAnalysisResponseData.getOilVolumetricProduction().getMin());
			cs.setFloat(41, totalAnalysisResponseData.getWaterVolumetricProduction().getValue());
			cs.setFloat(42, totalAnalysisResponseData.getWaterVolumetricProduction().getMax());
			cs.setFloat(43, totalAnalysisResponseData.getWaterVolumetricProduction().getMin());
			cs.setFloat(44, totalAnalysisResponseData.getVolumeWaterCut().getValue());
			cs.setFloat(45, totalAnalysisResponseData.getVolumeWaterCut().getMax());
			cs.setFloat(46, totalAnalysisResponseData.getVolumeWaterCut().getMin());
			
			//重量-产液、产油、产水、含水
			cs.setFloat(47, totalAnalysisResponseData.getLiquidWeightProduction().getValue());
			cs.setFloat(48, totalAnalysisResponseData.getLiquidWeightProduction().getMax());
			cs.setFloat(49, totalAnalysisResponseData.getLiquidWeightProduction().getMin());
			cs.setFloat(50, totalAnalysisResponseData.getOilWeightProduction().getValue());
			cs.setFloat(51, totalAnalysisResponseData.getOilWeightProduction().getMax());
			cs.setFloat(52, totalAnalysisResponseData.getOilWeightProduction().getMin());
			cs.setFloat(53, totalAnalysisResponseData.getWaterWeightProduction().getValue());
			cs.setFloat(54, totalAnalysisResponseData.getWaterWeightProduction().getMax());
			cs.setFloat(55, totalAnalysisResponseData.getWaterWeightProduction().getMin());
			cs.setFloat(56, totalAnalysisResponseData.getWeightWaterCut().getValue());
			cs.setFloat(57, totalAnalysisResponseData.getWeightWaterCut().getMax());
			cs.setFloat(58, totalAnalysisResponseData.getWeightWaterCut().getMin());
			
			//泵效、泵径、泵挂、动液面、沉没度
			cs.setFloat(59, totalAnalysisResponseData.getPumpEff().getValue());
			cs.setFloat(60, totalAnalysisResponseData.getPumpEff().getMax());
			cs.setFloat(61, totalAnalysisResponseData.getPumpEff().getMin());
			cs.setFloat(62, totalAnalysisResponseData.getPumpBoreDiameter().getValue()*1000);
			cs.setFloat(63, totalAnalysisResponseData.getPumpBoreDiameter().getMax()*1000);
			cs.setFloat(64, totalAnalysisResponseData.getPumpBoreDiameter().getMin()*1000);
			cs.setFloat(65, totalAnalysisResponseData.getPumpSettingDepth().getValue());
			cs.setFloat(66, totalAnalysisResponseData.getPumpSettingDepth().getMax());
			cs.setFloat(67, totalAnalysisResponseData.getPumpSettingDepth().getMin());
			cs.setFloat(68, totalAnalysisResponseData.getProducingfluidLevel().getValue());
			cs.setFloat(69, totalAnalysisResponseData.getProducingfluidLevel().getMax());
			cs.setFloat(70, totalAnalysisResponseData.getProducingfluidLevel().getMin());
			cs.setFloat(71, totalAnalysisResponseData.getSubmergence().getValue());
			cs.setFloat(72, totalAnalysisResponseData.getSubmergence().getMax());
			cs.setFloat(73, totalAnalysisResponseData.getSubmergence().getMin());
			
			//井下效率、地面效率、系统效率、吨液百米耗电量
			cs.setFloat(74, totalAnalysisResponseData.getWellDownSystemEfficiency().getValue());
			cs.setFloat(75, totalAnalysisResponseData.getWellDownSystemEfficiency().getMax());
			cs.setFloat(76, totalAnalysisResponseData.getWellDownSystemEfficiency().getMin());
			cs.setFloat(77, totalAnalysisResponseData.getSurfaceSystemEfficiency().getValue());
			cs.setFloat(78, totalAnalysisResponseData.getSurfaceSystemEfficiency().getMax());
			cs.setFloat(79, totalAnalysisResponseData.getSurfaceSystemEfficiency().getMin());
			cs.setFloat(80, totalAnalysisResponseData.getSystemEfficiency().getValue());
			cs.setFloat(81, totalAnalysisResponseData.getSystemEfficiency().getMax());
			cs.setFloat(82, totalAnalysisResponseData.getSystemEfficiency().getMin());
			cs.setFloat(83, totalAnalysisResponseData.getPowerConsumptionPerTHM().getValue());
			cs.setFloat(84, totalAnalysisResponseData.getPowerConsumptionPerTHM().getMax());
			cs.setFloat(85, totalAnalysisResponseData.getPowerConsumptionPerTHM().getMin());
			
			//电流平衡度、功率平衡度
			cs.setFloat(86, totalAnalysisResponseData.getIDegreeBalance().getValue());
			cs.setFloat(87, totalAnalysisResponseData.getIDegreeBalance().getMax());
			cs.setFloat(88, totalAnalysisResponseData.getIDegreeBalance().getMin());
			cs.setFloat(89, totalAnalysisResponseData.getIDegreeBalance().getValue());
			cs.setFloat(90, totalAnalysisResponseData.getIDegreeBalance().getMax());
			cs.setFloat(91, totalAnalysisResponseData.getIDegreeBalance().getMin());
			
			//电参
			cs.setInt(92, totalAnalysisResponseData.getETResultCode());//电参工况代码
			cs.setString(93, totalAnalysisResponseData.getETResultString());//电参工况综合
			
			//三相电流、三相电压
			cs.setFloat(94, totalAnalysisResponseData.getIA().getValue());
			cs.setFloat(95, totalAnalysisResponseData.getIA().getMax());
			cs.setFloat(96, totalAnalysisResponseData.getIA().getMin());
			cs.setFloat(97, totalAnalysisResponseData.getIB().getValue());
			cs.setFloat(98, totalAnalysisResponseData.getIB().getMax());
			cs.setFloat(99, totalAnalysisResponseData.getIB().getMin());
			cs.setFloat(100, totalAnalysisResponseData.getIC().getValue());
			cs.setFloat(101, totalAnalysisResponseData.getIC().getMax());
			cs.setFloat(102, totalAnalysisResponseData.getIC().getMin());
			cs.setString(103, totalAnalysisResponseData.getIMaxString());
			cs.setString(104, totalAnalysisResponseData.getVMinString());
			
			cs.setFloat(105, totalAnalysisResponseData.getVA().getValue());
			cs.setFloat(106, totalAnalysisResponseData.getVA().getMax());
			cs.setFloat(107, totalAnalysisResponseData.getVA().getMin());
			cs.setFloat(108, totalAnalysisResponseData.getVB().getValue());
			cs.setFloat(109, totalAnalysisResponseData.getVB().getMax());
			cs.setFloat(110, totalAnalysisResponseData.getVB().getMin());
			cs.setFloat(111, totalAnalysisResponseData.getVC().getValue());
			cs.setFloat(112, totalAnalysisResponseData.getVC().getMax());
			cs.setFloat(113, totalAnalysisResponseData.getVC().getMin());
			cs.setString(114, totalAnalysisResponseData.getVMaxString());
			cs.setString(115, totalAnalysisResponseData.getVMinString());
			
			cs.setFloat(125, totalAnalysisResponseData.getRunFrequency().getValue());
			cs.setFloat(126, totalAnalysisResponseData.getRunFrequency().getMax());
			cs.setFloat(127, totalAnalysisResponseData.getRunFrequency().getMin());
			cs.setFloat(128, totalAnalysisResponseData.getRPM().getValue());
			cs.setFloat(129, totalAnalysisResponseData.getRPM().getMax());
			cs.setFloat(130, totalAnalysisResponseData.getRPM().getMin());
			
			cs.setString(135, tatalDate);//汇总日期
			cs.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}finally{
			if(cs!=null)
				cs.close();
			conn.close();
		}
		return true;
	}
	
	public Boolean SaveEquipmentDataHYTXGridData(JSONObject jsonObject,String oraNames) throws SQLException {
		Connection conn=OracleJdbcUtis.getOuterConnection();
		CallableStatement cs=null;
		Connection conn2=SessionFactoryUtils.getDataSource(getSessionFactory()).getConnection();
		CallableStatement cs2=null;
		try {
			cs = conn.prepareCall("{call PRO_SAVE_EQUIPMENT_INFO(?,?,?,?,?,?,?,?)}");
			cs.setString(1, StringManagerUtils.getJSONObjectString(jsonObject, "areaName"));
			cs.setString(2, StringManagerUtils.getJSONObjectString(jsonObject, "oilstationName"));
			cs.setString(3, StringManagerUtils.getJSONObjectString(jsonObject, "equipmentName"));
			cs.setString(4, StringManagerUtils.getJSONObjectString(jsonObject, "equipmentNumber"));
			cs.setString(5, StringManagerUtils.getJSONObjectString(jsonObject, "totalrunningbase"));
			cs.setString(6, StringManagerUtils.getJSONObjectString(jsonObject, "firstmaintainbase"));
			cs.setString(7, StringManagerUtils.getJSONObjectString(jsonObject, "secondmaintainbase"));
			cs.setString(8, oraNames);
			cs.executeUpdate();
			
			cs2 = conn2.prepareCall("{call PRO_UPDATEEQUIPDAILYREPORTDATA(?,?,?,?,?,?,?)}");
			cs2.setString(1, StringManagerUtils.getJSONObjectString(jsonObject, "areaName"));
			cs2.setString(2, StringManagerUtils.getJSONObjectString(jsonObject, "oilstationName"));
			cs2.setString(3, StringManagerUtils.getJSONObjectString(jsonObject, "equipmentName"));
			cs2.setString(4, StringManagerUtils.getJSONObjectString(jsonObject, "equipmentNumber"));
			cs2.setString(5, StringManagerUtils.getJSONObjectString(jsonObject, "totalrunningbase"));
			cs2.setString(6, StringManagerUtils.getJSONObjectString(jsonObject, "firstmaintainbase"));
			cs2.setString(7, StringManagerUtils.getJSONObjectString(jsonObject, "secondmaintainbase"));
			cs2.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			if(cs!=null){
				cs.close();
			}
			if(cs2!=null){
				cs2.close();
			}
			conn.close();
			conn2.close();
		}
		return true;
	}
	
	public int SetWellProductionCycle(String sql) throws SQLException, ParseException {
		Connection conn=SessionFactoryUtils.getDataSource(getSessionFactory()).getConnection();
		CallableStatement cs=null;
		Statement st=null; 
		int result=0;
		try {
			st=conn.createStatement(); 
			result=st.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			if(cs!=null)
				cs.close();
			if(st!=null)
				st.close();
			conn.close();
		}
		return result;
	}
	
	public Boolean savePSToFSCalaulateResult(String wellName,String cjsj,String ElectricData,
			String StartPoint,String EndPoint,String FSDiagramId,
			String Stroke,String SPM,String CNT,String FSDiagram) throws SQLException {
		Connection conn=SessionFactoryUtils.getDataSource(getSessionFactory()).getConnection();
		CallableStatement cs=null;
		
		
		CLOB ElectricDataClob=new CLOB((OracleConnection) conn);
		ElectricDataClob = oracle.sql.CLOB.createTemporary(conn,false,1);
		ElectricDataClob.putString(1, ElectricData);
		
		CLOB FSDiagramClob=new CLOB((OracleConnection) conn);
		FSDiagramClob = oracle.sql.CLOB.createTemporary(conn,false,1);
		FSDiagramClob.putString(1, FSDiagram);
		try {
			cs = conn.prepareCall("{call SAVE_T303InversionData(?,?,?,?,?,?,?,?,?,?)}");
			cs.setString(1, wellName);
			cs.setString(2, cjsj);
			cs.setClob(3, ElectricDataClob);
			cs.setString(4, StartPoint);
			cs.setString(5, EndPoint);
			cs.setString(6, FSDiagramId);
			cs.setString(7, Stroke);
			cs.setString(8, SPM);
			cs.setString(9, CNT);
			cs.setClob(10, FSDiagramClob);
			cs.executeUpdate();
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(cs!=null)
				cs.close();
			conn.close();
		}
		return true;
	}
}
