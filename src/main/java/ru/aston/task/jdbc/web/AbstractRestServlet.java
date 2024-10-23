package ru.aston.task.jdbc.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

public abstract class AbstractRestServlet<T> extends HttpServlet {

    private static final String CONTENT_TYPE = "application/json";
    private static final String UTF_8 = "UTF-8";
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();


    protected void writeObjectsToResponse(Object models, HttpServletResponse response) throws SQLException, IOException {
        String modelsJson = toJson(models);
        PrintWriter writer = getConfiguredPrintWriter(response);
        writer.print(modelsJson);
        writer.flush();
        response.setStatus(HttpServletResponse.SC_OK);
    }

    protected void setErrorMessageToResponse(HttpServletResponse response, String message) throws IOException {
        PrintWriter writer = getConfiguredPrintWriter(response);
        writer.print(message);
        writer.flush();
    }

    protected PrintWriter getConfiguredPrintWriter(HttpServletResponse response) throws IOException {
        response.setContentType(CONTENT_TYPE);
        response.setCharacterEncoding(UTF_8);
        PrintWriter writer = response.getWriter();
        return writer;
    }

    protected String toJson(Object obj) {
        return gson.toJson(obj);
    }

    protected <T> T getModelFromJson(HttpServletRequest request, Class<T> clazz) throws IOException {
        BufferedReader reader = request.getReader();
        T object = gson.fromJson(reader, clazz);
        return object;
    }
}
