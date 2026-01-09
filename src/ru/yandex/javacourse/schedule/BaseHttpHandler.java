package ru.yandex.javacourse.schedule;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler {

    protected void sendText(HttpExchange h, String text) throws IOException {
        sendResponse(h, text, 200);
    }

    protected void sendNotFound(HttpExchange h, String text) throws IOException {
        sendResponse(h, text, 404);
    }

    protected void sendHasInteractions(HttpExchange h, String text) throws IOException {
        sendResponse(h, text, 406);
    }

    protected void sendResponse(HttpExchange h, String text, int rCode) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(rCode, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }
}