package ru.aston.task.jdbc.web;

import ru.aston.task.jdbc.DatabaseConnectionProvider;
import ru.aston.task.jdbc.dao.ProductDAO;
import ru.aston.task.jdbc.models.Product;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@WebServlet("/products")
public class ProductServlet extends AbstractRestServlet<Product> {
    private ProductDAO productDAO;

    @Override
    public void init() {
        DataSource dataSource = DatabaseConnectionProvider.getInstance().getDataSource();
        productDAO = new ProductDAO(dataSource);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String productId = request.getParameter("id");
        try {
            if (productId == null) {
                List<Product> products = productDAO.selectAll(Product.class);
                writeObjectsToResponse(products, response);
            } else {
                showProduct(productId, response);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        try {
            if (Objects.isNull(action)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
            if ("create".equals(action)) {
                createProduct(request, response);
            } else if ("update".equals(action)) {
                updateProduct(request, response);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }


    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String productId = request.getParameter("id");
        if (Objects.isNull(productId)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            deleteProduct(productId, response);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private void showProduct(String productId, HttpServletResponse response) throws SQLException, IOException {
        int id;
        try {
            id = Integer.parseInt(productId);
        } catch (NumberFormatException e) {
            String errorMessage = String.format("{\"message\": \"Invalid product id [%s]\"}", productId);
            setErrorMessageToResponse(response, errorMessage);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Product product = productDAO.findById(Product.class, id).orElse(null);

        if (Objects.isNull(product)) {
            String errorMsg = "{\"message\": \"Product not found\"}";
            setErrorMessageToResponse(response, errorMsg);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        writeObjectsToResponse(product, response);
    }

    private void createProduct(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        Product product = getModelFromJson(request, Product.class);
        if(product == null || product.getCode() == null) {
            String errorMsg = String.format("{\"message\": \"Invalid product [%s]\"}", product);
            setErrorMessageToResponse(response, errorMsg);
            return;
        }
        productDAO.insert(product);
        response.setStatus(HttpServletResponse.SC_CREATED);
    }

    private void updateProduct(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        Product product = getModelFromJson(request, Product.class);
        if(product == null || product.getId() == 0) {
            String errorMsg = String.format("{\"message\": \"Invalid product [%s]\"}", product);
            setErrorMessageToResponse(response, errorMsg);
            return;
        }

        productDAO.update(product);
        writeObjectsToResponse(product, response);
    }

    private void deleteProduct(String productId, HttpServletResponse response) throws SQLException, IOException {
        int id;
        try {
            id = Integer.parseInt(productId);
        } catch (NumberFormatException e) {
            String errorMessage = String.format("{\"message\": \"Invalid product id [%s]\"}", productId);
            setErrorMessageToResponse(response, errorMessage);
            return;
        }
        boolean deleted = productDAO.delete(Product.class, id);

        if (deleted) {
            PrintWriter writer = getConfiguredPrintWriter(response);
            writer.print(String.format("{\"message\": \"Product [%d] deleted successfully\"}", id));
            writer.flush();
            return;
        }

        String errorMsg = String.format("{\"message\": \"Product [%d] not found\"}", id);
        setErrorMessageToResponse(response, errorMsg);

    }

    /**
     * Устанавливает класс доступа к данным. Используется только для тестов.
     * @param productDAO класс доступа к данным
     */
    public void setProductDAO(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

}

