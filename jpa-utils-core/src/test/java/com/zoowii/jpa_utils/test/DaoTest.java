package com.zoowii.jpa_utils.test;

import com.alibaba.fastjson.JSONArray;
import com.google.common.base.Function;
import com.zoowii.jpa_utils.core.impl.JdbcSession;
import com.zoowii.jpa_utils.core.impl.JdbcSessionFactory;
import com.zoowii.jpa_utils.enums.SqlTypes;
import com.zoowii.jpa_utils.jdbcorm.sqlmapper.PgSQLMapper;
import com.zoowii.jpa_utils.query.Expr;
import com.zoowii.jpa_utils.query.ParameterBindings;
import com.zoowii.jpa_utils.query.Query;
import com.zoowii.jpa_utils.test.models.TestJsonb;
import com.zoowii.jpa_utils.test.models.User;
import com.zoowii.jpa_utils.util.ListUtil;
import com.zoowii.jpa_utils.util.ModelUtils;
import com.zoowii.jpa_utils.util.StringUtil;
import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;

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

    private Connection getPgsqlJdbcTestConnection() {
        try {
            Class.forName("org.postgresql.Driver");
            String jdbcUrl = "jdbc:postgresql://localhost:5432/test";
            return DriverManager.getConnection(jdbcUrl, "postgres", "postgres");
        } catch (Exception e) {
            LOG.error("get pgsql jdbc conn error", e);
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

    private JdbcSessionFactory getPgsqlJdbcTestSessionFactory() {
        final Connection conn = getPgsqlJdbcTestConnection();
        return new JdbcSessionFactory(new JdbcSessionFactory.JdbcConnectionSource() {
            @Override
            public Connection get() {
                return conn;
            }
        }, new PgSQLMapper());
    }

    public void testJdbcDeleteBatch() {
        try {
            JdbcSessionFactory sessionFactory = getJdbcTestSessionFactory();
            JdbcSession session = (JdbcSession) sessionFactory.createSession().asThreadLocal();
            session.begin();
            try {
                session.executeNativeSql("drop table if exists jpa_user");
                session.executeNativeSql("create table if not exists jpa_user (id varchar(50) primary key, name varchar(500), test_age bigint, random_number bigint)");
                session.startBatch();
                try {
                    for (int i = 0; i < 10; ++i) {
                        User user = new User();
                        user.setAge(999);
                        user.setName(StringUtil.randomString(10));
                        user.setRandomNumber(new Random().nextInt(100));
                        user.save();
                    }
                    session.executeBatch();
                } finally {
                    session.endBatch();
                }
                int count = session.delete(User.class, Expr.createEQ("age", 999));
                Assert.assertEquals(count, 10);
                session.commit();
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

    public void testJdbc() {
        try {
            JdbcSessionFactory sessionFactory = getJdbcTestSessionFactory();
            JdbcSession session = (JdbcSession) sessionFactory.createSession().asThreadLocal();
            session.begin();
            try {
                session.executeNativeSql("drop table if exists jpa_user");
                session.executeNativeSql("create table if not exists jpa_user (id varchar(50) primary key, name varchar(500), test_age bigint, random_number bigint)");
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
                List<User> usersFromLike = User.find.where(session).gt("test_age", 0).like("name", "%test%").gt("test_age", 0).offset(0).limit(10).all();
                Assert.assertTrue(usersFromLike.size() > 0);
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
                if (users.size() > 0) {
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

    public void testPgsqlFeatures() {
        if (true) {
            return;
        }
        JdbcSessionFactory sessionFactory = getPgsqlJdbcTestSessionFactory();
        try {
            JdbcSession session = (JdbcSession) sessionFactory.createSession().asThreadLocal();
            session.begin();
            try {
                session.executeNativeSql("drop table if exists test_jsonb");
                session.executeNativeSql("create table if not exists test_jsonb (id varchar(50) primary key, name varchar(255), tags jsonb)");
                TestJsonb testRecord1 = new TestJsonb();
                testRecord1.setId(UUID.randomUUID().toString());
                testRecord1.setName("test_name_" + testRecord1.getId());
                Map<String, Object> testTags = new HashMap<String, Object>();
                testTags.put("country", "China");
                testTags.put("age", 24);
                testTags.put("colors", ListUtil.seq("red", "green"));
                testRecord1.setTags(testTags);
                testRecord1.save();
                List<String> countries = TestJsonb.getSession().findListByRawQuery(String.class, "select distinct tags->>'country' as country from test_jsonb");
                Assert.assertEquals(countries.size(), 1);
                testRecord1.setName("test2");
                testTags.put("country", "America");
                testRecord1.update();
                TestJsonb testJsonb2 = TestJsonb.find.byId(testRecord1.getId());
                assertEquals(((JSONArray) testJsonb2.getTags().get("colors")).get(1), "green");
                List<String> countriesBySelect = TestJsonb.find.where().select("distinct tags->>'country'", "country").limit(10).allSelected(String.class);
                String countryBySelectForTestRecord1 = TestJsonb.find.where().eq("id", testRecord1.getId())
                        .select("tags->>'country'", "country").firstSelected(String.class);
                String countryBySelectForComplexQuery = TestJsonb.find.where().eq("tags#>'{colors}'", "[\"red\", \"green\"]", SqlTypes.JSONB)
                        .select("tags->>'country'", "country").firstSelected(String.class);
                String countryBySelectForComplexQuery2 = TestJsonb.find.where().eq("tags#>>'{colors,0}'", "red")
                        .select("tags->>'country'", "country").firstSelected(String.class);
                Assert.assertEquals(countriesBySelect.get(0), "America");
                Assert.assertEquals(countryBySelectForTestRecord1, "America");
                Assert.assertEquals(countryBySelectForComplexQuery, "America");
                Assert.assertEquals(countryBySelectForComplexQuery2, "America");
                Query<TestJsonb> testJsonbQuery = TestJsonb.find.where().eq("tags#>>'{colors,0}'", "red");
                // change pgsql's jsonb column will create new version each time, so just replace with new jsonb data, or you can create some pgsql functions to do this work
                int queryInfoForManuallySql1Executed = testJsonbQuery.sqlBuilder()
                        .update("name = :authorName").set("authorName", "zoowii").where().executeUpdate();
                Assert.assertTrue(queryInfoForManuallySql1Executed > 0);
                int deletedCountOfJsonbQuery = testJsonbQuery.sqlBuilder()
                        .delete().where().executeUpdate();
                Assert.assertTrue(deletedCountOfJsonbQuery > 0);
                long remainingAfterDeleteOfJsonbQuery = testJsonbQuery.count();
                Assert.assertEquals(remainingAfterDeleteOfJsonbQuery, 0);
                TestJsonb.getSession().executeNativeSql(ModelUtils.findRawQuerySqlByName(TestJsonb.class, "insertWithDefaultTags"), new ParameterBindings(UUID.randomUUID().toString(), "test_name"));
                Assert.assertTrue(TestJsonb.find.where().count() > 0);
                session.commit();
            } catch (Exception e) {
                e.printStackTrace();
                session.rollback();
            } finally {
                session.close();
            }
        } catch (Exception e) {
            LOG.error("test pgsql jdbc error", e);
        }
    }
}
