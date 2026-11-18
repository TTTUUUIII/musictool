package com.outlook.wn123o.mediadl.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class Settings {
    private static final Properties props = new Properties();
    private static final Path propsPath = Paths.get(System.getProperty("user.dir"), "app.properties");

    static {
        if (propsPath.toFile().exists()) {
            try(FileInputStream is = new FileInputStream("app.properties")) {
                props.load(is);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void store() {
        try(FileOutputStream os = new FileOutputStream(propsPath.toFile())) {
            props.store(os, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getString(String k, String v) {
        return props.getProperty(k, v);
    }

    public static String getString(String k) {
        return getString(k, null);
    }

    public static void putString(String k, String v) {
        putString(Map.of(k, v));
    }

    public static void putString(Map<String, String> items) {
        items.forEach(props::setProperty);
        store();
    }

    public static int getInt(String k) {
        return getInt(k, -1);
    }

    public static int getInt(String k, int v) {
        if (getString(k) == null) return v;
        try {
            return Integer.parseInt(getString(k));
        } catch (NumberFormatException e) {
            return v;
        }
    }

    public static void putInt(String k, int v) {
        putString(k, String.valueOf(v));
    }

    public static boolean getBoolean(String k) {
        return getBoolean(k, false);
    }

    public static boolean getBoolean(String k, boolean v) {
        if (getString(k) == null) return v;
        return Boolean.parseBoolean(getString(k));
    }

    public static void putBoolean(String k, boolean v) {
        putString(k, Boolean.toString(v));
    }

    public static Map<String, String> getAll() {
         return props.stringPropertyNames().stream()
                .collect(Collectors.toMap(k -> k, props::getProperty));
    }

    public static void remove(String k) {
        synchronized (props) {
            Object previous = props.remove(k);
            if (previous == null) return;
            try {
                // 删除沿用新增/更新的保存路径，防止重启后从旧文件恢复已删除的配置。
                store();
            } catch (RuntimeException e) {
                // 保存失败则恢复内存并向调用方报错，不能出现界面显示删除成功但磁盘仍保留。
                props.put(k, previous);
                throw e;
            }
        }
    }
}
