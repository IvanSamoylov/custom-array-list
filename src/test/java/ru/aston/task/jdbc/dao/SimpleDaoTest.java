package ru.aston.task.jdbc.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.aston.task.jdbc.models.Product;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SimpleDaoTest {

    private static final String TEST_CODE = "code1";
    private static final String TEST_NAME = "Test Name";
    private static final double TEST_PRICE = 19.99;
    private static final int TEST_ID = 1;
    private static final String FIELD_ID = "id";
    private static final String FILED_CODE = "code";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_PRICE = "price";
    private SimpleDao<Product> simpleDao;
    private DataSource mockDataSource;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private Product product;

    @BeforeEach
    public void setUp() throws SQLException {
        mockDataSource = mock(DataSource.class);
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockDataSource.getConnection()).thenReturn(mockConnection);


        simpleDao = new SimpleDao<>(mockDataSource);
        product = new Product(TEST_CODE, TEST_NAME, TEST_PRICE);
    }

    @Test
    public void testInsert() throws SQLException {
        simpleDao.insert(product);

        verify(mockPreparedStatement, times(1)).setObject(1, "code1");
        verify(mockPreparedStatement, times(1)).setObject(2, "Test Name");
        verify(mockPreparedStatement, times(1)).setObject(3, BigDecimal.valueOf(19.99));
        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    public void testInsertSQLException() throws SQLException {
        doThrow(new SQLException()).when(mockPreparedStatement).executeUpdate();

        assertThrows(SQLException.class, () -> simpleDao.insert(product));
    }

    @Test
    public void testUpdateSuccess() throws SQLException {
        product.setId(TEST_ID);
        simpleDao.update(product);

        verify(mockPreparedStatement, times(1)).setObject(1, TEST_CODE);
        verify(mockPreparedStatement, times(1)).setObject(2, TEST_NAME);
        verify(mockPreparedStatement, times(1)).setObject(3, BigDecimal.valueOf(TEST_PRICE));
        verify(mockPreparedStatement, times(1)).setObject(4, TEST_ID);
        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    public void testUpdateThrowsSQLException() throws SQLException {
        doThrow(new SQLException()).when(mockPreparedStatement).executeUpdate();

        assertThrows(SQLException.class, () -> simpleDao.update(product));
    }

    @Test
    public void testFindByIdReturnProduct() throws SQLException {
        product.setId(TEST_ID);
        ResultSet resultSet = mock(ResultSet.class);
        when(mockPreparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getObject(FIELD_ID)).thenReturn(TEST_ID);
        when(resultSet.getObject(FILED_CODE)).thenReturn(TEST_CODE);
        when(resultSet.getObject(FIELD_NAME)).thenReturn(TEST_NAME);
        when(resultSet.getObject(FIELD_PRICE)).thenReturn(BigDecimal.valueOf(TEST_PRICE));

        Optional<Product> returnedProduct = simpleDao.findById(Product.class, Integer.valueOf(TEST_ID));

        assertTrue(returnedProduct.isPresent());
        assertEquals(product.getId(), returnedProduct.get().getId());

    }

    @Test
    public void testFindByIdReturnEmpty() throws SQLException {
        product.setId(TEST_ID);
        ResultSet resultSet = mock(ResultSet.class);
        when(mockPreparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(false);

        Optional<Product> returnedProduct = simpleDao.findById(Product.class, TEST_ID);

        assertFalse(returnedProduct.isPresent());
    }

    @Test
    public void testFindByIdThrowsSQLException() throws SQLException {
        doThrow(new SQLException()).when(mockPreparedStatement).executeQuery();

        assertThrows(SQLException.class, () -> simpleDao.findById(Product.class, TEST_ID));
    }

    @Test
    public void testSelectAllReturnEmpty() throws SQLException {
        product.setId(TEST_ID);
        ResultSet resultSet = mock(ResultSet.class);
        when(mockPreparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getObject(FIELD_ID)).thenReturn(TEST_ID, TEST_ID + 1);
        when(resultSet.getObject(FILED_CODE)).thenReturn(TEST_CODE, TEST_CODE);
        when(resultSet.getObject(FIELD_NAME)).thenReturn(TEST_NAME, TEST_NAME);
        when(resultSet.getObject(FIELD_PRICE)).thenReturn(BigDecimal.valueOf(TEST_PRICE), BigDecimal.valueOf(TEST_PRICE));

        List<Product> products = simpleDao.selectAll(Product.class);

        assertEquals(products.size(), 2);
        assertEquals(products.getFirst().getId(), TEST_ID);
        assertEquals(products.get(1).getId(), TEST_ID + 1);
    }

    @Test
    public void testSelectAllThrowsSQLException() throws SQLException {
        doThrow(new SQLException()).when(mockPreparedStatement).executeQuery();

        assertThrows(SQLException.class, () -> simpleDao.selectAll(Product.class));
    }

    @Test
    public void tesDeleteReturnSuccess() throws SQLException {
        product.setId(TEST_ID);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = simpleDao.delete(Product.class, TEST_ID);

        verify(mockPreparedStatement, times(1)).setInt(1, TEST_ID);
        assertTrue(result);
    }

    @Test
    public void tesDeleteFailed() throws SQLException {
        product.setId(TEST_ID);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        boolean result = simpleDao.delete(Product.class, TEST_ID);

        verify(mockPreparedStatement, times(1)).setInt(1, TEST_ID);
        assertFalse(result);
    }

    @Test
    public void testDeleteThrowsSQLException() throws SQLException {
        doThrow(new SQLException()).when(mockPreparedStatement).executeUpdate();

        assertThrows(SQLException.class, () -> simpleDao.delete(Product.class, TEST_ID));
    }
}