package ru.aston.task.jdbc.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.aston.task.jdbc.dao.ProductDAO;
import ru.aston.task.jdbc.models.Product;

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

public class ProductServletTest {

    private static final String TEST_CODE = "code1";
    private static final String TEST_NAME = "Product1";
    private static final double TEST_PRICE = 19.99;
    private static final String EXPECTED_JSON_PRODUCT = String.format("{\"id\":0,\"code\":\"%s\",\"name\":\"%s\",\"price\":%s}"
            , TEST_CODE, TEST_NAME, TEST_PRICE);
    private static final int ID_VALUE = 1;
    private static final String ID_STRING_VALUE = "1";
    private static final String ID = "id";
    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ProductDAO productDAO;

    @InjectMocks
    private ProductServlet servlet;

    private StringWriter responseWriter;
    private Product mockProduct;

    @BeforeEach
    public void setup() throws IOException {
        MockitoAnnotations.openMocks(this);
        mockProduct = new Product(TEST_CODE, TEST_NAME, TEST_PRICE);
        servlet = new ProductServlet() {
            @Override
            protected <T> T getModelFromJson(HttpServletRequest request, Class<T> clazz) throws IOException {
                return (T) mockProduct;
            }
        };

        servlet.setProductDAO(productDAO);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    public void testDoGetWithId_Success() throws ServletException, IOException, SQLException {
        when(request.getParameter(ID)).thenReturn(ID_STRING_VALUE);
        when(productDAO.findById(Product.class, ID_VALUE)).thenReturn(Optional.of(mockProduct));

        servlet.doGet(request, response);

        assertEquals(EXPECTED_JSON_PRODUCT, responseWriter.toString().replaceAll("\\s", ""));
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDoGet_ThrowsException() throws SQLException {
        when(request.getParameter(ID)).thenReturn(ID_STRING_VALUE);
        when(productDAO.findById(Product.class, ID_VALUE)).thenThrow(new SQLException("Database error"));

        assertThrows(ServletException.class, () -> servlet.doGet(request, response));
    }

    @Test
    public void testDoGetWithId_NotFound() throws ServletException, IOException, SQLException {
        when(request.getParameter("id")).thenReturn("99");
        when(productDAO.findById(Product.class, 99)).thenReturn(Optional.empty());

        servlet.doGet(request, response);

        assertEquals("{\"message\": \"Product not found\"}", responseWriter.toString().trim());
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testDoGetWithInvalidId() throws ServletException, IOException {
        when(request.getParameter("id")).thenReturn("invalid");

        servlet.doGet(request, response);

        assertEquals("{\"message\": \"Invalid product id [invalid]\"}", responseWriter.toString().trim());
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testDoGetWithoutId() throws ServletException, IOException, SQLException {
        when(request.getParameter("id")).thenReturn(null);
        when(productDAO.selectAll(Product.class)).thenReturn(Collections.singletonList(mockProduct));

        servlet.doGet(request, response);

        assertEquals("[" + EXPECTED_JSON_PRODUCT + "]", responseWriter.toString().replaceAll("\\s", ""));
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDoPostCreate_Success() throws ServletException, IOException, SQLException {
        when(request.getParameter("action")).thenReturn("create");

        servlet.doPost(request, response);

        verify(productDAO).insert(mockProduct);
        verify(response).setStatus(HttpServletResponse.SC_CREATED);
    }

    @Test
    public void testDoPostCreate_InvalidProduct() throws ServletException, IOException {
        when(request.getParameter("action")).thenReturn("create");

        servlet = new ProductServlet() {
            @Override
            protected <T> T getModelFromJson(HttpServletRequest request, Class<T> clazz) {
                return null;
            }
        };
        servlet.setProductDAO(productDAO);

        servlet.doPost(request, response);

        assertEquals("{\"message\": \"Invalid product [null]\"}", responseWriter.toString().trim());
        verify(response, never()).setStatus(HttpServletResponse.SC_CREATED);
    }

    @Test
    public void testDoPostUpdate_Success() throws ServletException, IOException, SQLException {
        when(request.getParameter("action")).thenReturn("update");
        mockProduct.setId(ID_VALUE);
        servlet.doPost(request, response);

        verify(productDAO).update(mockProduct);
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDoPostUpdate_InvalidProduct() throws ServletException, IOException {
        when(request.getParameter("action")).thenReturn("update");

        servlet = new ProductServlet() {
            @Override
            protected <T> T getModelFromJson(HttpServletRequest request, Class<T> clazz) {
                return null;
            }
        };
        servlet.setProductDAO(productDAO);

        servlet.doPost(request, response);

        assertEquals("{\"message\": \"Invalid product [null]\"}", responseWriter.toString().trim());
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
        when(productDAO.delete(Product.class, 1)).thenReturn(true);

        servlet.doDelete(request, response);

        assertEquals("{\"message\": \"Product [1] deleted successfully\"}", responseWriter.toString().trim());
        verify(response, never()).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testDoDelete_NotFound() throws ServletException, IOException, SQLException {
        when(request.getParameter("id")).thenReturn("99");
        when(productDAO.delete(Product.class, 99)).thenReturn(false);

        servlet.doDelete(request, response);

        assertEquals("{\"message\": \"Product [99] not found\"}", responseWriter.toString().trim());
        verify(response, never()).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDoDelete_ThrowsException() throws SQLException {
        when(request.getParameter(ID)).thenReturn(ID_STRING_VALUE);
        when(productDAO.delete(Product.class, ID_VALUE)).thenThrow(new SQLException("Database error"));

        assertThrows(ServletException.class, () -> servlet.doDelete(request, response));
    }
}