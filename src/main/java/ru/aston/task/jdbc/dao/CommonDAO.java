package ru.aston.task.jdbc.dao;

import java.sql.SQLException;
import java.util.List;

public interface CommonDAO<T> {

    void insert(T model) throws SQLException;

    T findById(int id) throws SQLException;

    List<T> selectAll() throws SQLException;

    boolean delete(int id) throws SQLException;

    boolean update(T model) throws SQLException;

}
