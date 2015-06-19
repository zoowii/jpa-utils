package com.zoowii.jpa_utils.test;

import com.google.common.base.Function;
import com.zoowii.jpa_utils.core.impl.EntitySession;
import com.zoowii.jpa_utils.core.Session;
import com.zoowii.jpa_utils.core.impl.JdbcSession;
import com.zoowii.jpa_utils.core.impl.JdbcSessionFactory;
import com.zoowii.jpa_utils.query.Expr;
import com.zoowii.jpa_utils.query.ParameterBindings;
import com.zoowii.jpa_utils.test.models.Employee;
import com.zoowii.jpa_utils.query.Query;
import com.zoowii.jpa_utils.test.models.User;
import com.zoowii.jpa_utils.util.ListUtil;
import com.zoowii.jpa_utils.util.StringUtil;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class DaoTest extends TestCase {
    private static final Logger LOG = LoggerFactory.getLogger(DaoTest.class);

    private Connection getJdbcTestConnection() {
        try {
            Class.forName("org.h2.Driver");
//            Class.forName("com.mysql.jdbc.Driver");
            String jdbcUrl = "jdbc:h2:mem:jpa_utils";
//            String jdbcUrl = "jdbc:mysql://127.0.0.1:3306/test";
//            return DriverManager.getConnection(jdbcUrl, "root", "");
            return DriverManager.getConnection(jdbcUrl);
        } catch (Exception e) {
            LOG.error("get jdbc conn error", e);
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
            JdbcSession session = (JdbcSession) sessionFactory.createSession().asThreadLocal();
            session.begin();
            try {
                session.executeNativeSql("drop table if exists jpa_user");
                session.executeNativeSql("create table if not exists jpa_user (id varchar(50) primary key, name varchar(500), test_age int(11), random_number int(11))");
                for (int i = 0; i < 10; ++i) {
                    User user = new User();
                    user.setName("test_user_" + UUID.randomUUID().toString());
                    user.setAge(new Random().nextInt(100));
                    user.setRandomNumber(new Random().nextInt(100));
                    user.save();
                    LOG.info("new user's id is " + user.getId());
                    if (new Random().nextInt(10) > 5) {
                        LOG.info("test to delete the just inserted record");
                        user.delete();
                    }
                }
                List<User> users = session.findListByQuery(User.class, "select * from jpa_user");
                LOG.info("there are " + users.size() + " records now");
                for (User user1 : users) {
                    user1.setName("updated_user_name_" + UUID.randomUUID().toString());
                    user1.update();
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

                List<User> users1 = User.find.where(session).gt("test_age", 50).all();
                LOG.info(users1.size() + "");
                List<User> usersOfLimitAndOffset = User.find.where(session).limit(3).offset(2).all();
                assertTrue(usersOfLimitAndOffset.size() <= 3);
                List<String> idsForInQuery = ListUtil.map(usersOfLimitAndOffset, new Function<User, String>() {
                    @Override
                    public String apply(User user) {
                        return user.getId();
                    }
                });
                List<User> usersFromIn = User.find.where(session).in("id", idsForInQuery).all();
                assertTrue(usersFromIn.size() > 0);
                List<User> usersFromInSubQuery = User.find.where(session).in("id", "select id from jpa_user limit 4").all();
                assertTrue(usersFromInSubQuery == null || usersFromInSubQuery.size() == 4);
                List<User> usersFromParamQuery = session.findListByRawQuery(User.class,
                        "select * from jpa_user where test_age > ?",
                        new ParameterBindings(50));
                assertEquals(usersFromParamQuery.size(), users1.size());
                if(users.size()>0) {
                    User userFromParamQuery = (User) session.findFirstByRawQuery(User.class,
                            "select * from jpa_user where id=?", new ParameterBindings(users.get(0).getId()));
                    assertNotNull(userFromParamQuery);
                    List<User> usersFromOrColumnsQuery = User.find.where(session)
                            .or(Expr.createLE("randomNumber", 40),
                                    Expr.createGE("randomNumber", 41),
                                    Expr.createEQ("age", 100)).all();
                    assertTrue(usersFromOrColumnsQuery.size() > 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
                session.rollback();
            } finally {
                session.close();
            }
        } catch (Exception e) {
            LOG.error("test jdbc error", e);
        }
    }
}
