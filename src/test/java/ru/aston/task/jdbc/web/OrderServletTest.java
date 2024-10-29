package ru.aston.task.jdbc.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.aston.task.jdbc.dao.OrderDAO;
import ru.aston.task.jdbc.models.Order;
import ru.aston.task.jdbc.models.OrderEntry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class OrderServletTest {

    private static final int CUSTOMER_ID = 1;
    private static final String EXPECTED_JSON_ORDER = "{\"id\":1,\"customerId\":1,\"orderEntries\":[{\"id\":0," +
            "\"orderId\":1,\"productId\":2,\"price\":100.00,\"quantity\":2},{\"id\":0,\"orderId\":2,\"productId\":3,\"price\":50.00,\"quantity\":1}]}";

    private static final int ID_VALUE = 1;
    private static final String ID_STRING_VALUE = "1";
    private static final String ID_FIELD = "id";
    private static final String ACTION_PARAMETER = "action";
    private static final String UPDATE_ACTION = "update";
    private static final String CREATE_ACTION = "create";
    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private OrderDAO orderDao;

    @InjectMocks
    private OrderServlet servlet;

    private StringWriter responseWriter;
    private Order mockOrder;

    @BeforeEach
    public void setup() throws IOException {
        MockitoAnnotations.openMocks(this);
        mockOrder = createTestOrder();
        servlet = createMockServlet(mockOrder);

        servlet.setOrderDao(orderDao);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    public void testDoGetWithId_Success() throws ServletException, IOException, SQLException {
        when(request.getParameter(ID_FIELD)).thenReturn(ID_STRING_VALUE);
        when(orderDao.findById(Order.class, ID_VALUE)).thenReturn(Optional.of(mockOrder));

        servlet.doGet(request, response);

        assertEquals(EXPECTED_JSON_ORDER, responseWriter.toString().replaceAll("\\s", ""));
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDoGet_ThrowsException() throws SQLException {
        when(request.getParameter(ID_FIELD)).thenReturn(ID_STRING_VALUE);
        when(orderDao.findById(Order.class, ID_VALUE)).thenThrow(new SQLException("Database error"));

        assertThrows(ServletException.class, () -> servlet.doGet(request, response));
    }

    @Test
    public void testDoGetWithId_NotFound() throws ServletException, IOException, SQLException {
        when(request.getParameter(ID_FIELD)).thenReturn("99");
        when(orderDao.findById(Order.class, 99)).thenReturn(Optional.empty());

        servlet.doGet(request, response);

        assertEquals("{\"message\": \"Order not found\"}", responseWriter.toString().trim());
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testDoGetWithInvalidId() throws ServletException, IOException {
        when(request.getParameter(ID_FIELD)).thenReturn("invalid");

        servlet.doGet(request, response);

        assertEquals("{\"message\": \"Invalid order id [invalid]\"}", responseWriter.toString().trim());
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testDoGetWithoutId() throws ServletException, IOException, SQLException {
        when(request.getParameter(ID_FIELD)).thenReturn(null);
        when(orderDao.selectAll(Order.class)).thenReturn(Collections.singletonList(mockOrder));

        servlet.doGet(request, response);

        assertEquals("[" + EXPECTED_JSON_ORDER + "]", responseWriter.toString().replaceAll("\\s", ""));
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDoPostCreate_Success() throws ServletException, IOException, SQLException {
        when(request.getParameter(ACTION_PARAMETER)).thenReturn(CREATE_ACTION);

        servlet.doPost(request, response);

        verify(orderDao).insert(mockOrder);
        verify(response).setStatus(HttpServletResponse.SC_CREATED);
    }

    @Test
    public void testDoPostCreate_InvalidProduct() throws ServletException, IOException {
        when(request.getParameter(ACTION_PARAMETER)).thenReturn(CREATE_ACTION);

        servlet = createMockServlet(null);
        servlet.setOrderDao(orderDao);

        servlet.doPost(request, response);

        assertEquals("{\"message\": \"Invalid order [null]\"}", responseWriter.toString().trim());
        verify(response, never()).setStatus(HttpServletResponse.SC_CREATED);
    }

    @Test
    public void testDoPostUpdate_Success() throws ServletException, IOException, SQLException {
        when(request.getParameter(ACTION_PARAMETER)).thenReturn(UPDATE_ACTION);
        mockOrder.setId(ID_VALUE);
        servlet.doPost(request, response);

        verify(orderDao).update(mockOrder);
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDoPostUpdate_InvalidProduct() throws ServletException, IOException {
        when(request.getParameter(ACTION_PARAMETER)).thenReturn(UPDATE_ACTION);

        servlet = createMockServlet(null);
        servlet.setOrderDao(orderDao);

        servlet.doPost(request, response);

        assertEquals("{\"message\": \"Invalid order [null]\"}", responseWriter.toString().trim());
    }

    @Test
    public void testDoPost_BadRequest() throws ServletException, IOException {
        when(request.getParameter(ACTION_PARAMETER)).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST);
    }


    @Test
    public void testDoDelete_Success() throws ServletException, IOException, SQLException {
        when(request.getParameter(ID_FIELD)).thenReturn("1");
        when(orderDao.delete(Order.class, 1)).thenReturn(true);

        servlet.doDelete(request, response);

        assertEquals("{\"message\": \"Product [1] deleted successfully\"}", responseWriter.toString().trim());
        verify(response, never()).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testDoDelete_NotFound() throws ServletException, IOException, SQLException {
        when(request.getParameter(ID_FIELD)).thenReturn("99");
        when(orderDao.delete(Order.class, 99)).thenReturn(false);

        servlet.doDelete(request, response);

        assertEquals("{\"message\": \"Product [99] not found\"}", responseWriter.toString().trim());
        verify(response, never()).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDoDelete_ThrowsException() throws SQLException {
        when(request.getParameter(ID_FIELD)).thenReturn(ID_STRING_VALUE);
        when(orderDao.delete(Order.class, ID_VALUE)).thenThrow(new SQLException("Database error"));

        assertThrows(ServletException.class, () -> servlet.doDelete(request, response));
    }

    private static Order createTestOrder() {
        List<OrderEntry> entries = Arrays.asList(
                new OrderEntry(1, 2, new BigDecimal("100.00"), 2),
                new OrderEntry(2, 3, new BigDecimal("50.00"), 1)
        );
        return new Order(1, CUSTOMER_ID, entries);
    }

    private OrderServlet createMockServlet(Order order) {
        return new OrderServlet() {
            @Override
            protected <T> T getModelFromJson(HttpServletRequest request, Class<T> clazz) {
                return (T) order;
            }
        };
    }
}