package ru.aston.task.jdbc.dao;

import ru.aston.task.jdbc.models.ModelRecord;
import ru.aston.task.jdbc.models.annotations.Id;
import ru.aston.task.jdbc.models.annotations.OneToMany;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Класс реализует основные операции работы с СУБД.
 *
 * @param <T> параметр класса
 */
public class SimpleDao<T> {
    private static final String ID_COLUMN_NAME = "id";
    private final Logger log = Logger.getLogger(this.getClass().getName());
    private static final String SELECT_FIELDS_BY_TEMPLATE = "SELECT %s FROM \"%s\" WHERE %s = ?";
    private static final String INSERT_SQL_TEMPLATE = "INSERT INTO \"%s\" (%s) VALUES (?, ?, ?)";
    private static final String DELETE_SQL_TEMPLATE = "DELETE FROM \"%s\" WHERE id = ?";
    private static final String SELECT_ALL_TEMPLATE = "SELECT * FROM \"%s\"";
    private static final String UPDATE_TEMPLATE = "UPDATE \"%s\" SET %s  WHERE id = ?";

    private final DataSource dataSource;

    public SimpleDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Создает запись в таблице базы данных. Записываются все поля класса, за исключением отмеченных
     * аннотациями {@link Id} и {@link OneToMany}.
     *
     * @param model класс для сохранения в БД
     * @throws SQLException исключение, если произошла ошибка записи
     */
    public void insert(T model) throws SQLException {
        List<ModelRecord> fields = createFieldsList(model, List.of(Id.class, OneToMany.class));
        String insertSql = buildInsertSql(model, fields);

        executeSqlForModel(insertSql, fields);
    }

    private void executeSqlForModel(String sql, List<ModelRecord> fields) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            int index = 1;
            for (ModelRecord field : fields) {
                preparedStatement.setObject(index++, field.value());
            }
            preparedStatement.executeUpdate();
        }
    }

    private List<ModelRecord> createFieldsList(T model, List<Class<? extends Annotation>> excludeAnnotations) {
        return Arrays.stream(model.getClass().getDeclaredFields())
                .filter(field -> excludeAnnotations.stream().noneMatch(field::isAnnotationPresent))
                .map(field -> {
                    Class<?> type = field.getType();
                    field.setAccessible(true);
                    try {
                        return new ModelRecord(type.getSimpleName(), field.getName(), field.get(model));
                    } catch (IllegalAccessException e) {
                        log.log(Level.SEVERE, "Failed to get value for field " + field.getName(), e);
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }

    private String buildInsertSql(T model, List<ModelRecord> fields) {
        String fieldNames = fields.stream().map(ModelRecord::name).collect(Collectors.joining(","));
        return String.format(INSERT_SQL_TEMPLATE, model.getClass().getSimpleName(), fieldNames.trim());
    }

    /**
     * Обновляет данные модели.
     *
     * @param model класс для обновления в БД
     * @throws SQLException исключение, если произошла ошибка записи
     */
    public void update(T model) throws SQLException {
        List<ModelRecord> fields = createFieldsList(model, List.of(OneToMany.class));
        String updateSql = buildUpdateSql(model, fields);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(updateSql)) {
            int index = 1;
            for (ModelRecord field : fields) {
                if (field.name().equalsIgnoreCase(ID_COLUMN_NAME)) {
                    continue;
                }
                preparedStatement.setObject(index++, field.value());
            }

            Object idValue = fields.stream().filter(record -> ID_COLUMN_NAME.equals(record.name()))
                    .findAny().map(ModelRecord::value).orElseThrow();
            preparedStatement.setObject(index, idValue);
            preparedStatement.executeUpdate();
        }
    }

    private String buildUpdateSql(T model, List<ModelRecord> fields) {
        String fieldNames = fields.stream()
                .map(ModelRecord::name)
                .filter(name -> !ID_COLUMN_NAME.equals(name))
                .map(name -> name + " = ?")
                .collect(Collectors.joining(","));
        return String.format(UPDATE_TEMPLATE, model.getClass().getSimpleName(), fieldNames.trim());
    }


    /**
     * Метод для поиска строки в таблице по идентификатору.
     *
     * @param clazz класс, по полям которого производится поиск
     * @param id    идентификатор записи в таблице
     * @return возвращает класс в обертке Optional, либо Optiona.empty()
     * @throws SQLException исключение выбрасывается, если произошла ошибка доступа к данным
     */
    public Optional<T> findById(Class<T> clazz, Integer id) throws SQLException {
        return findByField(clazz, "id", id);
    }

    /*
     * Метод для поиска строки в таблице по идентификатору.
     *
     * @param clazz      класс, по полям которого производится поиск
     * @param fieldName  название столбца в таблице
     * @param fieldValue значение поля для поиска
     * @return возвращает класс в обертке Optional, либо Optiona.empty()
     * @throws SQLException исключение выбрасывается, если произошла ошибка доступа к данным
     */
    private Optional<T> findByField(Class<T> clazz, String fieldName, Object fieldValue) throws SQLException {
        String fieldsToSearch = Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> !field.isAnnotationPresent(OneToMany.class))
                .map(Field::getName)
                .collect(Collectors.joining(","));

        String selectBy = String.format(SELECT_FIELDS_BY_TEMPLATE, fieldsToSearch, clazz.getSimpleName(), fieldName);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(selectBy)) {
            preparedStatement.setObject(1, fieldValue);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return Optional.of(createEntity(rs, clazz));
            }
            return Optional.empty();
        }
    }

    public List<T> selectAll(Class<T> clazz) throws SQLException {
        List<T> models = new ArrayList<>();
        String selectAll = String.format(SELECT_ALL_TEMPLATE, clazz.getSimpleName());
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(selectAll)) {
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                models.add(createEntity(rs, clazz));
            }
        }
        return models;
    }

    /**
     * Метод для создания объекта класса из полученных данных. Класс создается через рефлексию.
     *
     * @param rs    набор полученных данных
     * @param clazz класс объекта для создания
     * @return созданный класс
     */
    protected T createEntity(ResultSet rs, Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            T entity = constructor.newInstance();

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                String columnName = field.getName();
                Object value = rs.getObject(columnName);
                field.set(entity, value);
            }
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create entity", e);
        }
    }

    /**
     * Метод для удаления записей по идентификатору
     *
     * @param clazz класс записи для удаления
     * @param id    идентификатор записи
     * @return true если запись была удалена, false если не было удаления
     * @throws SQLException исключение выбрасывается, если произошла ошибка доступа к данным
     */
    public boolean delete(Class<T> clazz, int id) throws SQLException {
        String deleteSql = String.format(DELETE_SQL_TEMPLATE, clazz.getSimpleName());
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(deleteSql)) {
            preparedStatement.setInt(1, id);
            return preparedStatement.executeUpdate() > 0;
        }
    }
}
