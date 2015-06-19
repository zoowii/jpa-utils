package com.zoowii.jpa_utils.test;

import com.zoowii.jpa_utils.core.Session;
import com.zoowii.jpa_utils.core.impl.EntitySession;
import com.zoowii.jpa_utils.query.Query;
import com.zoowii.jpa_utils.test.models.Employee;
import com.zoowii.jpa_utils.util.Logger;
import com.zoowii.jpa_utils.util.StringUtil;
import junit.framework.TestCase;

import java.util.List;
import java.util.Random;

/**
 * Created by zoowii on 15/6/19.
 */
public class JpaDaoTest extends TestCase {

    public void testCreate() {
        Session session = EntitySession.getSession("persistenceUnit"); // persistenceUnit/mysql/etc.
        session.begin();
        try {
            for (int i = 0; i < 10; ++i) {
                Employee employee = new Employee();
                employee.setName("employee_" + StringUtil.randomString(10));
                employee.setAge(new Random().nextInt(100));
                employee.save(session);
                Logger.info("new employee " + employee.getId());
            }
            List<Employee> employeesFromInQuery = Employee.find.where(session).in("id", "select id from Employee").all();
            assertTrue(employeesFromInQuery.size() > 0);
            session.commit();
        } catch (Exception e) {
            e.printStackTrace();
            session.rollback();
        } finally {
            session.close();
        }
    }

    public void testQuery() {
        Session session = EntitySession.getSession("persistenceUnit"); // persistenceUnit/mysql/etc.
        session.begin();
        try {
//            session.delete(Employee.class, Expr.createGT("age", 50));
            Query<Employee> query = Employee.find.where().gt("age", 50);
            query = query.limit(8);
            List<Employee> employees = query.all(session);
            for (int i = 0; i < employees.size(); ++i) {
                Employee employee = employees.get(i);
                Logger.info((i + 1) + ". employee " + employee.getId());
            }
            Logger.info("total: " + query.count(session));
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
