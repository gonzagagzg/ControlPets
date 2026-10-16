package com.itq.util;

import java.sql.SQLException;

public final class SqlErrorUtil {
    private SqlErrorUtil() {}
    public static int estadoHttp(SQLException e) {
        String s = e.getSQLState();
        if ("23505".equals(s) || "23503".equals(s) || "23502".equals(s)) return 409;
        if ("22P02".equals(s) || (s != null && s.startsWith("22"))) return 400;
        return 500;
    }
}
