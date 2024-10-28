package ru.aston.task.jdbc.dao;

import ru.aston.task.jdbc.DatabaseConnectionProvider;
import ru.aston.task.jdbc.models.Order;
import ru.aston.task.jdbc.models.OrderEntry;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OrderDAO extends SimpleDao<Order> {
    private final Logger log = Logger.getLogger(this.getClass().getName());
    private OrderEntryDAO orderEntryDAO;
    private DataSource dataSource;

    public OrderDAO(DataSource dataSource, OrderEntryDAO orderEntryDAO) {
        super(dataSource);
        this.dataSource = dataSource;
        this.orderEntryDAO = orderEntryDAO;
    }

    /**
     * Находит заказ по его идентификатору
     *
     * @param clazz класс, по полям которого производится поиск
     * @param id    идентификатор записи в таблице
     * @return заказ обернутый Optional, либо пусто Optional
     * @throws SQLException если произошла ошибка базы данных
     */
    @Override
    public Optional<Order> findById(Class<Order> clazz, Integer id) throws SQLException {
        Optional<Order> order = super.findById(Order.class, id);
        if (order.isPresent()) {
            Order foundOrder = order.get();
            List<OrderEntry> entries = orderEntryDAO.findByField(OrderEntry.class, "orderId", order.get().getId());
            log.info(entries.size() + " entries found for order " + foundOrder.getId());
            foundOrder.setOrderEntries(entries);
        }
        return order;
    }

    /**
     * Создает новый заказ в БД со всеми записями.
     *
     * @param order класс для сохранения в БД
     * @throws SQLException если произошла ошибка базы данных
     */
    @Override
    public void insert(Order order) throws SQLException {
        String orderQuery = "INSERT INTO \"Order\" (customerId) VALUES (?)";
        String orderEntryQuery = "INSERT INTO \"OrderEntry\" (orderId, productId, quantity, price) VALUES (?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement orderStmt = connection.prepareStatement(orderQuery, Statement.RETURN_GENERATED_KEYS)) {
                orderStmt.setInt(1, order.getCustomerId());
                orderStmt.executeUpdate();

                try (ResultSet generatedKeys = orderStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        order.setId(generatedKeys.getInt(1));
                        log.info("Order entry generated: " + order.getId());
                    }
                }
            }

            try (PreparedStatement entryStmt = connection.prepareStatement(orderEntryQuery)) {
                for (OrderEntry entry : order.getOrderEntries()) {
                    entryStmt.setInt(1, order.getId());
                    entryStmt.setInt(2, entry.getProductId());
                    entryStmt.setInt(3, entry.getQuantity());
                    entryStmt.setBigDecimal(4, BigDecimal.valueOf(entry.getPrice()));
                    entryStmt.addBatch();
                }
                entryStmt.executeBatch();
            }

            connection.commit();
        } catch (SQLException e) {
            throw new SQLException("Failed to create order", e);
        }
    }

    /**
     * Обновление заказа. Позиции заказа удаляются и снова создаются.
     * @param order класс для обновления в БД
     * @throws SQLException если произошла ошибка базы данных
     */
    @Override
    public void update(Order order) throws SQLException {
        String orderQuery = "UPDATE \"Order\" SET customerId = ? WHERE id = ?";
        String deleteEntriesQuery = "DELETE FROM \"OrderEntry\" WHERE orderId = ?";
        String insertEntryQuery = "INSERT INTO \"OrderEntry\" (orderId, productId, quantity, price) VALUES (?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement orderStmt = connection.prepareStatement(orderQuery)) {
                orderStmt.setInt(1, order.getCustomerId());
                orderStmt.setInt(2, order.getId());
                orderStmt.executeUpdate();
            }

            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteEntriesQuery)) {
                deleteStmt.setInt(1, order.getId());
                deleteStmt.executeUpdate();
            }

            try (PreparedStatement insertStmt = connection.prepareStatement(insertEntryQuery)) {
                for (OrderEntry entry : order.getOrderEntries()) {
                    insertStmt.setInt(1, order.getId());
                    insertStmt.setInt(2, entry.getProductId());
                    insertStmt.setInt(3, entry.getQuantity());
                    insertStmt.setBigDecimal(4, BigDecimal.valueOf(entry.getPrice()));
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
            }

            connection.commit();
            log.log(Level.INFO, "Update successful for order " + order.getId());
        } catch (SQLException e) {
            throw new SQLException("Failed to update order", e);
        }
    }

    /**
     * Находит все заказы. Для каждой позиции в заказе создается отдельный запрос.
     * @param clazz класс заказа
     * @return список найденных заказов
     * @throws SQLException если произошла ошибка базы данных
     */
    @Override
    public List<Order> selectAll(Class<Order> clazz) throws SQLException {
        List<Order> orders = super.selectAll(clazz);
        for (Order order : orders) {
            List<OrderEntry> entries = orderEntryDAO.findByField(OrderEntry.class, "orderId", order.getId());
            order.setOrderEntries(entries);
        }

        return orders;
    }

    public static void main(String[] args) throws SQLException {
        DataSource dataSource = DatabaseConnectionProvider.getInstance().getDataSource();
        OrderEntryDAO orderEntryDAO = new OrderEntryDAO(dataSource);
        OrderDAO orderDAO = new OrderDAO(dataSource, orderEntryDAO);
        for (Order order : orderDAO.selectAll(Order.class)) {
            System.out.println(order);
        }
        Optional<Order> order = orderDAO.findById(Order.class, 1);
        System.out.println("Order found: " + order.get().getId());
        orderDAO.insert(order.get());
        for (OrderEntry entry : order.get().getOrderEntries()) {
            double price = entry.getPrice();
            entry.setPrice(price * 2);
        }
        orderDAO.update(order.get());
        orderDAO.delete(Order.class, order.get().getId());

    }
}

