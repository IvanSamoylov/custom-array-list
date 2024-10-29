package ru.aston.task.jdbc.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.aston.task.jdbc.models.Order;
import ru.aston.task.jdbc.models.OrderEntry;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderDAOTest {

    private static final int CUSTOMER_ID = 1;
    private static final String ID_FIELD = "id";
    private static final String CUSTOMER_ID_FIELD = "customerId";
    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @Mock
    private OrderEntryDAO orderEntryDAO;

    @InjectMocks
    private OrderDAO orderDAO;

    @BeforeEach
    public void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(dataSource.getConnection()).thenReturn(connection);
    }

    @Test
    public void testInsertOrder_Success() throws SQLException {
        Order order = createTestOrder();
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1);

        orderDAO.insert(order);

        verify(preparedStatement, times(1)).executeUpdate();
        verify(preparedStatement, times(1)).getGeneratedKeys();
        verify(preparedStatement, times(2)).addBatch();
        verify(preparedStatement, times(1)).executeBatch();
        assertEquals(1, order.getId());
    }

    @Test
    public void testCreateOrder_SQLException() throws SQLException {
        Order order = createTestOrder();

        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenThrow(new SQLException("Database error"));

        assertThrows(SQLException.class, () -> orderDAO.insert(order));
    }

    @Test
    public void testGetOrderById_Success() throws SQLException {
        int orderId = 1;

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getObject(ID_FIELD)).thenReturn(orderId);
        when(resultSet.getObject(CUSTOMER_ID_FIELD)).thenReturn(1);

        Optional<Order> order = orderDAO.findById(Order.class, orderId);

        assertTrue(order.isPresent());
        assertEquals(orderId, order.get().getId());
        assertEquals(1, order.get().getCustomerId());
    }

    @Test
    public void testGetOrderById_NotFound() throws SQLException {
        int orderId = 1;

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(false);

        Optional<Order> order = orderDAO.findById(Order.class, orderId);

        assertFalse(order.isPresent());
    }

    @Test
    public void testUpdateOrder_Success() throws SQLException {
        Order order = createTestOrder();
        order.setId(1);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        orderDAO.update(order);

        verify(preparedStatement, times(2)).executeUpdate();
        verify(preparedStatement, times(2)).addBatch();
        verify(preparedStatement, times(1)).executeBatch();
    }

    @Test
    public void testUpdateOrder_SQLException() throws SQLException {
        Order order = createTestOrder();
        order.setId(1);

        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        assertThrows(SQLException.class, () -> orderDAO.update(order));
    }

    @Test
    public void testDeleteOrder_Success() throws SQLException {
        int orderId = 1;

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = orderDAO.delete(Order.class, orderId);

        assertTrue(result);
        verify(preparedStatement, times(1)).executeUpdate();
    }

    @Test
    public void testDeleteOrder_NotFound() throws SQLException {
        int orderId = 1;

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        boolean result = orderDAO.delete(Order.class, orderId);

        assertFalse(result);
        verify(preparedStatement, times(1)).executeUpdate();
    }

    @Test
    public void testGetAllOrders_Success() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getObject("id")).thenReturn(1, 2);
        when(resultSet.getObject("customerId")).thenReturn(1, 2);
        List<OrderEntry> orderEntries = createTestOrder().getOrderEntries();
        when(orderEntryDAO.findByField(OrderEntry.class, "orderId", 1)).thenReturn(orderEntries);

        List<Order> orders = orderDAO.selectAll(Order.class);

        assertNotNull(orders);
        assertEquals(2, orders.size());
    }

    @Test
    public void testGetAllOrders_SQLException() throws SQLException {
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        assertThrows(SQLException.class, () -> orderDAO.selectAll(Order.class));
    }

    private static Order createTestOrder() {
        List<OrderEntry> entries = Arrays.asList(
                new OrderEntry(1, 2, new BigDecimal("100.00"), 2),
                new OrderEntry(2, 3, new BigDecimal("50.00"), 1)
        );
        Order order = new Order(CUSTOMER_ID, entries);
        return order;
    }
}
