package com.zoowii.jpa_utils.test;

import com.zoowii.jpa_utils.core.impl.EntitySession;
import com.zoowii.jpa_utils.core.Session;
import com.zoowii.jpa_utils.core.impl.JdbcSession;
import com.zoowii.jpa_utils.core.impl.JdbcSessionFactory;
import com.zoowii.jpa_utils.query.Expr;
import com.zoowii.jpa_utils.test.models.Employee;
import com.zoowii.jpa_utils.query.Query;
import com.zoowii.jpa_utils.test.models.User;
import com.zoowii.jpa_utils.util.StringUtil;
import junit.framework.TestCase;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class DaoTest extends TestCase {
    private static final Logger LOG = Logger.getLogger(DaoTest.class);

    private Connection getJdbcTestConnection() {
        try {
            Class.forName("org.h2.Driver");
//            Class.forName("com.mysql.jdbc.Driver");
            String jdbcUrl = "jdbc:h2:mem:jpa_utils";
//            String jdbcUrl = "jdbc:mysql://127.0.0.1:3306/test";
//            return DriverManager.getConnection(jdbcUrl, "root", "");
            return DriverManager.getConnection(jdbcUrl);
        } catch (Exception e) {
            LOG.error(e);
            throw new RuntimeException(e);
        }
    }

    private JdbcSessionFactory getJdbcTestSessionFactory() {
        final Connection conn = getJdbcTestConnection();
        return new JdbcSessionFactory(new JdbcSessionFactory.JdbcConnectionSource() {
            @Override
            public Connection get() {
                return conn;
            }
        });
    }


    public void testJdbc() {
        try {
            JdbcSessionFactory sessionFactory = getJdbcTestSessionFactory();
            JdbcSession session = (JdbcSession) sessionFactory.createSession();
            session.begin();
            try {
                session.executeNativeSql("create table if not exists jpa_user (id bigint auto_increment primary key, name varchar(500))");
                for (int i = 0; i < 10; ++i) {
                    User user = new User();
                    user.setName("test_user_" + UUID.randomUUID().toString());
                    user.save(session);
                    LOG.info("new user's id is " + user.getId());
                    if (new Random().nextInt(10) > 5) {
                        LOG.info("test to delete the just inserted record");
                        user.delete(session);
                    }
                }
                List<User> users = session.findListByQuery(User.class, "select * from jpa_user");
                LOG.info("there are " + users.size() + " records now");
                for (User user1 : users) {
                    user1.setName("updated_user_name_" + UUID.randomUUID().toString());
                    user1.update(session);
                }
                if (users.size() > 0) {
                    User userToRefresh = new User();
                    userToRefresh.setId(users.get(0).getId());
                    userToRefresh.refresh(session);
                    LOG.info(userToRefresh.getName());
                    assertTrue(userToRefresh.getName() != null);

                    User userFound = (User) session.find(User.class, users.get(users.size() - 1).getId());
                    LOG.info(userFound.getName());
                }
            } catch (Exception e) {
                session.rollback();
            } finally {
                session.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error(e);
        }
    }

    public void testCreate() {
        Session session = EntitySession.getSession("persistenceUnit");
        session.begin();
        try {
            for (int i = 0; i < 10; ++i) {
                Employee employee = new Employee();
                employee.setName("employee_" + StringUtil.randomString(10));
                employee.setAge(new Random().nextInt(100));
                employee.save(session);
                LOG.info("new employee " + employee.getId());
            }
            session.commit();
        } catch (Exception e) {
            e.printStackTrace();
            session.rollback();
        } finally {
            session.close();
        }
    }

    public void testQuery() {
        Session session = EntitySession.getSession("persistenceUnit");
        session.begin();
        try {
            session.delete(Employee.class, Expr.createGT("age", 50));
            Query<Employee> query = Employee.find.where().gt("age", 50);
            query = query.limit(8);
            List<Employee> employees = query.all(session);
            for (int i = 0; i < employees.size(); ++i) {
                Employee employee = employees.get(i);
                LOG.info((i + 1) + ". employee " + employee.getId());
            }
            LOG.info("total: " + query.count(session));
            session.commit();
        } catch (Exception e) {
            e.printStackTrace();
            session.rollback();
        } finally {
            session.close();
        }
    }

    public void testCreateAndQuery() {
        testCreate();
        testQuery();
    }
}
