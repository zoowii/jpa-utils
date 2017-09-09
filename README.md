JPAUtils
=====

ActiveRecord-like implementation based on JPA(eg. Hibernate) or direct hibernate session

2014七夕献礼,庆祝自己向证明注孤生迈出了更加坚实的一步

## Author

* zoowii(https://github.com/zoowii)

## Features

* 底层基于JPA或者Hibernate的Session/SessionFactory，基于HQL/SQL，比自己再轮一个类HQL稳定,但是也提供直接基于JDBC的封装
* 也提供直接基于jdbc的支持,从而可以不依赖Hibernate等ORM,也方便直接和jdbc Connection, MyBatis, DbUtils, 数据库连接池等库直接集成使用
* 直接基于jdbc Connection的话,ORM映射部分目前只支持MySQL和H2数据库,其他数据库待支持
* 可以自动从JPA配置创建管理session,也可以手动指定EntityManagerFactory/EntityManager/SessionFactory(hibernate)/Session(hibernate)来构造jpa-utils中的Session来使用,还可以直接从jdbc Connection构造Session
* 提供类似ActiveRecord的使用方便友好的API，特别是查询API
* 查询的核心Finder类可以单独使用，直接使用到现有的使用JPA或Hibernate的代码中，只需要根据现有EntityManager/Session(hibernate)构造一个jpa-utils的session，然后使用Finder类来查询就好了
* 支持类似MyBatis的执行编程式XML中的SQL(目前使用的是Clojure脚本,从而可以更灵活配置SQL)
* 通过entity model类注解支持剥离部分SQL，比如@Query, @Sql, @SubSql, @Select, @Update, @Insert, @Delete等（DOING）
* 通过query::select支持手动控制选择数据, 通过query::update支持对满足条件的记录执行update指定字段的操作, 通过query::join支持关联表查询
* union操作(TODO)
* 提供一个底层为mongodb的provider,并实现SQL/HQL to MongoDB-API parser(TODO)
* 支持PostgreSQL，目前只支持jsonb类型，不支持json,hstore等类型
* 支持根据Query对象构造部分sql，然后使用时另外补全sql及其他参数
* 根据新功能重构（TODO)
* 添加可选的一级缓存和可选的二级缓存(如果二级缓存使用ehcache, cacheName是jpa_utils_cache)

## Usages

    // maven
    !!! First deploy it to you local maven nexus, then.
    <dependency>
         <groupId>com.zoowii</groupId>
         <artifactId>jpa-utils</artifactId>
         <version>x.y.z</version>
    </dependency>

    // create
    Session session = EntitySession.currentSession(); // or Session.getSession(persistentUnitName);
    session.begin();
    try {
        for (int i = 0; i < 10; ++i) {
            Employee employee = new Employee();
            employee.setName("employee_" + StringUtil.randomString(10));
            employee.setAge(new Random().nextInt(100));
            employee.save(); // or employee.save(session);
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
        DbMigrationContext dbMigrationContext = new DbMigrationContext(session);
        dbMigrationContext.loadAndApplyMigrations(Arrays.asList(
                CreateUserTableMigration.class,
                AddAgeToUserTableMigration.class
        ));
        Long lastDbMigrationVersion = DbVersionEntity.find.where(session).orderBy("version", false).select("version").firstSelected(Long.class);
        LOG.info("last db migration version is " + lastDbMigrationVersion);
    
        Query<Employee> query = Employee.find.where().gt("age", 50);
        query = query.limit(8);
        List<Employee> employees = query.all(); // or query.all(session);
        for (int i = 0; i < employees.size(); ++i) {
            Employee employee = employees.get(i);
            logger.info((i + 1) + ". employee " + employee.getId());
        }
        logger.info("total: " + query.count()); // or query.count(session);
        session.commit();
    } catch (Exception e) {
        e.printStackTrace();
        session.rollback();
    }
