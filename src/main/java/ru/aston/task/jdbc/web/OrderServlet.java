package ru.aston.task.jdbc.web;

import ru.aston.task.jdbc.DatabaseConnectionProvider;
import ru.aston.task.jdbc.dao.OrderDAO;
import ru.aston.task.jdbc.dao.OrderEntryDAO;
import ru.aston.task.jdbc.models.Order;
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

@WebServlet("/orders")
public class OrderServlet extends AbstractRestServlet<Product> {
    private OrderDAO orderDao;

    @Override
    public void init() {
        DataSource dataSource = DatabaseConnectionProvider.getInstance().getDataSource();
        OrderEntryDAO orderEntryDao = new OrderEntryDAO(dataSource);
        orderDao = new OrderDAO(dataSource, orderEntryDao);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String orderId = request.getParameter("id");
        try {
            if (orderId == null) {
                List<Order> orders = orderDao.selectAll(Order.class);
                writeObjectsToResponse(orders, response);
            } else {
                showOrder(orderId, response);
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
                createOrder(request, response);
            } else if ("update".equals(action)) {
                updateOrder(request, response);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String orderId = request.getParameter("id");
        if (Objects.isNull(orderId)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            deleteOrder(orderId, response);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private void showOrder(String productId, HttpServletResponse response) throws SQLException, IOException {
        int id;
        try {
            id = Integer.parseInt(productId);
        } catch (NumberFormatException e) {
            String errorMessage = String.format("{\"message\": \"Invalid order id [%s]\"}", productId);
            setErrorMessageToResponse(response, errorMessage);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Order order = orderDao.findById(Order.class, id).orElse(null);

        if (Objects.isNull(order)) {
            String errorMsg = "{\"message\": \"Order not found\"}";
            setErrorMessageToResponse(response, errorMsg);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        writeObjectsToResponse(order, response);
    }

    private void createOrder(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        Order order = getModelFromJson(request, Order.class);
        if (order == null) {
            String errorMsg = String.format("{\"message\": \"Invalid order [%s]\"}", order);
            setErrorMessageToResponse(response, errorMsg);
            return;
        }
        orderDao.insert(order);
        response.setStatus(HttpServletResponse.SC_CREATED);
    }

    private void updateOrder(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        Order order = getModelFromJson(request, Order.class);
        if (order == null || order.getId() == 0) {
            String errorMsg = String.format("{\"message\": \"Invalid order [%s]\"}", order);
            setErrorMessageToResponse(response, errorMsg);
            return;
        }

        orderDao.update(order);
        writeObjectsToResponse(order, response);
    }

    private void deleteOrder(String orderId, HttpServletResponse response) throws SQLException, IOException {
        int id;
        try {
            id = Integer.parseInt(orderId);
        } catch (NumberFormatException e) {
            String errorMessage = String.format("{\"message\": \"Invalid order id [%s]\"}", orderId);
            setErrorMessageToResponse(response, errorMessage);
            return;
        }
        boolean deleted = orderDao.delete(Order.class, id);

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
     *
     * @param orderDao класс доступа к данным
     */
    public void setOrderDao(OrderDAO orderDao) {
        this.orderDao = orderDao;
    }

}

