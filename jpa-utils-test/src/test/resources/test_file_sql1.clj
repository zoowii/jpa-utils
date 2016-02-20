(ns test_file_sql1)

(def user-table-columns "u.id, u.name, u.test_age, u.random_number")

(def from-user-table "jpa_user u")

(def select-max-age-user (str
    "SELECT "
    user-table-columns
    " FROM "
    from-user-table
    " WHERE u.test_age>= (select max(u2.test_age) from jpa_user u2)"
  ))

(def sqls
    {"select-max-age-user" select-max-age-user})