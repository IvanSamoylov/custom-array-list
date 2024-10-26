package ru.aston.task.jdbc.dao;

import ru.aston.task.jdbc.models.Customer;

import javax.sql.DataSource;


public class CustomerDAO extends SimpleDao<Customer> {

   public CustomerDAO(DataSource dataSource) {
       super(dataSource);
   }

}
