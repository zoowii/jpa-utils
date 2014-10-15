JPAUtils
=====

ActiveRecord-like implementation based on JPA(eg. Hibernate)

2014七夕献礼,庆祝自己向证明注孤生迈出了更加坚实的一步

## Author

* zoowii(https://github.com/zoowii)

## Usages

    // maven
    !!! deploy it to you local maven nexus, then.
    <dependency>
         <groupId>com.zoowii</groupId>
         <artifactId>jpa-utils</artifactId>
         <version>1.8</version>
    </dependency>

    // create
    Session session = Session.currentSession();
    session.begin();
    try {
        for (int i = 0; i < 10; ++i) {
            Employee employee = new Employee();
            employee.setName("employee_" + StringUtil.randomString(10));
            employee.setAge(new Random().nextInt(100));
            employee.save();
            logger.info("new employee " + employee.getId());
        }
        session.commit();
    } catch (Exception e) {
        e.printStackTrace();
        session.rollback();
    }

    // query
    Session session = Session.currentSession();
    session.begin();
    try {
        Query<Employee> query = Employee.find.where().gt("age", 50);
        query = query.limit(8);
        List<Employee> employees = query.all();
        for (int i = 0; i < employees.size(); ++i) {
            Employee employee = employees.get(i);
            logger.info((i + 1) + ". employee " + employee.getId());
        }
        logger.info("total: " + query.count());
        session.commit();
    } catch (Exception e) {
        e.printStackTrace();
        session.rollback();
    }
