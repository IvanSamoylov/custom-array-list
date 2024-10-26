package ru.aston.task.jdbc.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.aston.task.jdbc.dao.CustomerDAO;
import ru.aston.task.jdbc.models.Customer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class CustomerServletTest {

    private static final int ID_VALUE = 1;
    private static final String TEST_NAME = "Customer1";
    private static final String TEST_ADDRESS = "Address";
    private static final String EXPECTED_JSON_CUSTOMER = String.format("{\"id\":%d,\"name\":\"%s\",\"address\":\"%s\"}",
            ID_VALUE, TEST_NAME, TEST_ADDRESS);

    private static final String ID_STRING_VALUE = "1";
    private static final String ID = "id";
    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private CustomerDAO customerDAO;

    @InjectMocks
    private CustomerServlet servlet;

    private StringWriter responseWriter;
    private Customer mockCustomer;

    @BeforeEach
    public void setup() throws IOException {
        MockitoAnnotations.openMocks(this);
        mockCustomer = new Customer(1, TEST_NAME, TEST_ADDRESS);
        servlet = new CustomerServlet() {
            @Override
            protected <T> T getModelFromJson(HttpServletRequest request, Class<T> clazz) {
                return (T) mockCustomer;
            }
        };

        servlet.setCustomerDao(customerDAO);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    public void testDoGetWithId_Success() throws ServletException, IOException, SQLException {
        when(request.getParameter(ID)).thenReturn(ID_STRING_VALUE);
        when(customerDAO.findById(Customer.class, ID_VALUE)).thenReturn(Optional.of(mockCustomer));

        servlet.doGet(request, response);

        assertEquals(EXPECTED_JSON_CUSTOMER, responseWriter.toString().replaceAll("\\s", ""));
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDoGet_ThrowsException() throws SQLException {
        when(request.getParameter(ID)).thenReturn(ID_STRING_VALUE);
        when(customerDAO.findById(Customer.class, ID_VALUE)).thenThrow(new SQLException("Database error"));

        assertThrows(ServletException.class, () -> servlet.doGet(request, response));
    }

    @Test
    public void testDoGetWithId_NotFound() throws ServletException, IOException, SQLException {
        when(request.getParameter("id")).thenReturn("99");
        when(customerDAO.findById(Customer.class, 99)).thenReturn(Optional.empty());

        servlet.doGet(request, response);

        assertEquals("{\"message\": \"Customer not found\"}", responseWriter.toString().trim());
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testDoGetWithInvalidId() throws ServletException, IOException {
        when(request.getParameter("id")).thenReturn("invalid");

        servlet.doGet(request, response);

        assertEquals("{\"message\": \"Invalid customer id [invalid]\"}", responseWriter.toString().trim());
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testDoGetWithoutId() throws ServletException, IOException, SQLException {
        when(request.getParameter("id")).thenReturn(null);
        when(customerDAO.selectAll(Customer.class)).thenReturn(Collections.singletonList(mockCustomer));

        servlet.doGet(request, response);

        assertEquals("[" + EXPECTED_JSON_CUSTOMER + "]", responseWriter.toString().replaceAll("\\s", ""));
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDoPostCreate_Success() throws ServletException, IOException, SQLException {
        when(request.getParameter("action")).thenReturn("create");

        servlet.doPost(request, response);

        verify(customerDAO).insert(mockCustomer);
        verify(response).setStatus(HttpServletResponse.SC_CREATED);
    }

    @Test
    public void testDoPostCreate_InvalidProduct() throws ServletException, IOException {
        when(request.getParameter("action")).thenReturn("create");

        servlet = new CustomerServlet() {
            @Override
            protected <T> T getModelFromJson(HttpServletRequest request, Class<T> clazz) {
                return null;
            }
        };
        servlet.setCustomerDao(customerDAO);

        servlet.doPost(request, response);

        assertEquals("{\"message\": \"Invalid customer [null]\"}", responseWriter.toString().trim());
        verify(response, never()).setStatus(HttpServletResponse.SC_CREATED);
    }

    @Test
    public void testDoPostUpdate_Success() throws ServletException, IOException, SQLException {
        when(request.getParameter("action")).thenReturn("update");
        mockCustomer.setId(ID_VALUE);
        servlet.doPost(request, response);

        verify(customerDAO).update(mockCustomer);
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDoPostUpdate_InvalidProduct() throws ServletException, IOException {
        when(request.getParameter("action")).thenReturn("update");

        servlet = new CustomerServlet() {
            @Override
            protected <T> T getModelFromJson(HttpServletRequest request, Class<T> clazz) {
                return null;
            }
        };
        servlet.setCustomerDao(customerDAO);

        servlet.doPost(request, response);

        assertEquals("{\"message\": \"Invalid customer [null]\"}", responseWriter.toString().trim());
    }

    @Test
    public void testDoPost_BadRequest() throws ServletException, IOException {
        when(request.getParameter("action")).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST);
    }


    @Test
    public void testDoDelete_Success() throws ServletException, IOException, SQLException {
        when(request.getParameter("id")).thenReturn("1");
        when(customerDAO.delete(Customer.class, 1)).thenReturn(true);

        servlet.doDelete(request, response);

        assertEquals("{\"message\": \"Customer [1] deleted successfully\"}", responseWriter.toString().trim());
        verify(response, never()).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testDoDelete_NotFound() throws ServletException, IOException, SQLException {
        when(request.getParameter("id")).thenReturn("99");
        when(customerDAO.delete(Customer.class, 99)).thenReturn(false);

        servlet.doDelete(request, response);

        assertEquals("{\"message\": \"Customer [99] not found\"}", responseWriter.toString().trim());
        verify(response, never()).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDoDelete_InvalidId() throws ServletException, IOException, SQLException {
        when(request.getParameter("id")).thenReturn("a");

        servlet.doDelete(request, response);

        assertEquals("{\"message\": \"Invalid customer id [a]\"}", responseWriter.toString().trim());
        verify(response, never()).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDoDelete_ThrowsException() throws SQLException {
        when(request.getParameter(ID)).thenReturn(ID_STRING_VALUE);
        when(customerDAO.delete(Customer.class, ID_VALUE)).thenThrow(new SQLException("Database error"));

        assertThrows(ServletException.class, () -> servlet.doDelete(request, response));
    }
}