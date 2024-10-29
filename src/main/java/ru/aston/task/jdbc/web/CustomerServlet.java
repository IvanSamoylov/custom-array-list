package ru.aston.task.jdbc.web;

import ru.aston.task.jdbc.DatabaseConnectionProvider;
import ru.aston.task.jdbc.dao.CustomerDAO;
import ru.aston.task.jdbc.models.Customer;

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

@WebServlet("/customers")
public class CustomerServlet extends AbstractRestServlet<Customer> {
    private CustomerDAO customerDao;

    @Override
    public void init() {
        DataSource dataSource = DatabaseConnectionProvider.getInstance().getDataSource();
        customerDao = new CustomerDAO(dataSource);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String customerId = request.getParameter("id");
        try {
            if (customerId == null) {
                List<Customer> customers = customerDao.selectAll(Customer.class);
                writeObjectsToResponse(customers, response);
            } else {
                showCustomer(customerId, response);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        try {
            if (Objects.isNull(action)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
            if ("create".equals(action)) {
                createCustomer(request, response);
            } else if ("update".equals(action)) {
                updateCustomer(request, response);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String customeId = request.getParameter("id");
        if (Objects.isNull(customeId)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            deleteDelete(customeId, response);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private void showCustomer(String customeId, HttpServletResponse response) throws SQLException, IOException {
        int id;
        try {
            id = Integer.parseInt(customeId);
        } catch (NumberFormatException e) {
            String errorMessage = String.format("{\"message\": \"Invalid customer id [%s]\"}", customeId);
            setErrorMessageToResponse(response, errorMessage);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Customer customer = customerDao.findById(Customer.class, id).orElse(null);

        if (Objects.isNull(customer)) {
            String errorMsg = "{\"message\": \"Customer not found\"}";
            setErrorMessageToResponse(response, errorMsg);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        writeObjectsToResponse(customer, response);
    }

    private void createCustomer(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        Customer customer = getModelFromJson(request, Customer.class);
        if(customer == null || customer.getName() == null) {
            String errorMsg = String.format("{\"message\": \"Invalid customer [%s]\"}", customer);
            setErrorMessageToResponse(response, errorMsg);
            return;
        }
        customerDao.insert(customer);
        response.setStatus(HttpServletResponse.SC_CREATED);
    }

    private void updateCustomer(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        Customer customer = getModelFromJson(request, Customer.class);
        if(customer == null || customer.getId() == 0) {
            String errorMsg = String.format("{\"message\": \"Invalid customer [%s]\"}", customer);
            setErrorMessageToResponse(response, errorMsg);
            return;
        }

        customerDao.update(customer);
        writeObjectsToResponse(customer, response);
    }

    private void deleteDelete(String customerId, HttpServletResponse response) throws SQLException, IOException {
        int id;
        try {
            id = Integer.parseInt(customerId);
        } catch (NumberFormatException e) {
            String errorMessage = String.format("{\"message\": \"Invalid customer id [%s]\"}", customerId);
            setErrorMessageToResponse(response, errorMessage);
            return;
        }
        boolean deleted = customerDao.delete(Customer.class, id);

        if (deleted) {
            PrintWriter writer = getConfiguredPrintWriter(response);
            writer.print(String.format("{\"message\": \"Customer [%d] deleted successfully\"}", id));
            writer.flush();
            return;
        }

        String errorMsg = String.format("{\"message\": \"Customer [%d] not found\"}", id);
        setErrorMessageToResponse(response, errorMsg);
    }

    /**
     * Устанавливает класс доступа к данным. Используется для тестирования класса.
     * @param customerDao класс доступа к данным
     */
    public void setCustomerDao(CustomerDAO customerDao) {
        this.customerDao = customerDao;
    }
}

