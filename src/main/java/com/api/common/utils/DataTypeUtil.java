package com.api.common.utils;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import lombok.SneakyThrows;
import org.testng.log4testng.Logger;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;

public class DataTypeUtil {
    private static final Logger logger = Logger.getLogger(DataTypeUtil.class);

    public static List<String> splitStringToListByPunctuation(String str, String split) {
        return str == null ? null : Arrays.asList(str.split(split));
    }

    @SneakyThrows
    public static List<String> fetchColumnFromCSV(String outputFilePath, int columnIndex) {
        List<String> result = new ArrayList<>();
        String[] array;
        int rowIndex = -1;
        File file = new File(outputFilePath);
        if (file.exists()) {
            while (true) {
                if (file.length() != 0) {
                    break;
                }
            }
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            for (String line : lines) {
                array = line.split(",", -1);
                if (rowIndex != -1 && array[4].contains("EH_QLX")) {
                    result.add(array[columnIndex - 1]);
                }
                rowIndex++;
            }
        }
        return result;
    }

    public static List<String[]> convertCsvToStringArray(String csvContent, String csvName) {
        List<String[]> result = new ArrayList<>();
        int rowIndex = 0;
        String[] csvRows = csvContent.split("\r\n");
        String regex = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)";
        Pattern pattern = Pattern.compile(regex);
        if (csvName.contains("Mapping")) {
            rowIndex += 1;
        }
        for (; rowIndex < csvRows.length; rowIndex++) {
            String[] fields = pattern.split(csvRows[rowIndex], -1);
            for (int i = 0; i < fields.length; i++) {
                fields[i] = fields[i].replace("\"", "");
            }
            result.add(fields);
        }
        return result;
    }

    public static <T> List<T> convertCSVStringToObjectList(String csvString, Class<T> clazz) {
        String[] csvArr = csvString.split("\r\n");
        List<String[]> csvList = new ArrayList<>();
        List<T> result = new ArrayList<>();
        for (String s : csvArr) {
            String[] arr = s.split(",");
            for (int j = 0; j < arr.length; j++) {
                if (arr[j].length() > 0 && arr[j].charAt(0) == '\"' && arr[j].charAt(arr[j].length() - 1) == '\"') {
                    arr[j] = arr[j].replaceAll("\"", "");
                }
            }
            csvList.add(arr);
        }
        for (int i = 1; i < csvList.size(); i++) {
            try {
                T t = clazz.newInstance();
                if (csvList.get(i)[0].equalsIgnoreCase("")) continue;
                for (int j = 0; j < csvList.get(i).length - 1; j++) {
                    set(t, csvList.get(0)[j].replaceAll("\"", ""), csvList.get(i)[j]);
                }
                result.add(t);
            } catch (SecurityException | IllegalArgumentException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static <T> List<T> convertStringArrayListToObject(List<String[]> stringArray, Class<T> clazz) {
        String[] header = stringArray.get(0);
        Field[] declaredFields = clazz.getDeclaredFields();
        List<String> fieldNames = new ArrayList<>();
        for (Field field : declaredFields) {
            fieldNames.add(field.getName());
        }
        List<Integer> variableIndexList = new ArrayList<>();
        for (int i = 0; i < header.length; i++) {
            if (fieldNames.contains(header[i])) {
                variableIndexList.add(i);
            }
        }
        List<T> result = new ArrayList<>();
        for (int i = 1; i < stringArray.size(); i++) {
            boolean numericColumnContainBlank = false;
            try {
                T t = (T) clazz.newInstance();
                for (Integer index : variableIndexList) {
                    if (stringArray.get(i)[index] != null && stringArray.get(i)[index].isBlank()) {
                        Class<?> fieldType = clazz.getDeclaredField(header[index].replaceAll("\"", "")).getType();
                        if (fieldType.isPrimitive() || Number.class.isAssignableFrom(fieldType)) {
                            numericColumnContainBlank = true;
                            break;
                        }
                    }
                    set(t, header[index].replaceAll("\"", ""), stringArray.get(i)[index]);
                }
                if (!numericColumnContainBlank) {
                    result.add(t);
                }
            } catch (InstantiationException | IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    private static boolean set(Object object, String fieldName, Object fieldValue) {
        Class<?> clazz = object.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                if (field.getType() == int.class || field.getType() == Integer.class) {
                    field.set(object, Integer.parseInt((String) fieldValue));
                } else {
                    field.set(object, fieldValue);
                }
                return true;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return false;
    }

    public static <T> List<T> castListToSpecifyClass(List<?> list, Class<T> type) {
        List<T> result = new ArrayList<>();
        for (Object o : list) {
            if (type.isInstance(o)) {
                result.add(type.cast(o));
            }
        }
        return result;
    }

    public static HashMap<String, String> jsonObjectToHashMap(JSONObject jsonObj) {
        HashMap<String, String> data = new HashMap<>();
        jsonObj.keySet().forEach(key -> {
            String value = jsonObj.get(key) != null ? jsonObj.get(key).toString() : "";
            data.put(key, value);
        });
        return data;
    }

    public static <T> List<T> jsonArrayToObjectList(JSONArray array) {
        List<T> list = new ArrayList<>();
        for (Object o : array) {
            list.add((T) o);
        }
        return list;
    }

    public static Map<String, Object> getMapFromString(String str, String split1, String split2) {
        Map<String, Object> myMap = new HashMap<>();
        String[] pairs = str.split(split1);
        for (int i = 0; i < pairs.length; i++) {
            String pair = pairs[i];
            String[] keyValue = pair.split(split2);
            if (keyValue.length == 1) myMap.put(keyValue[0], "");
            else myMap.put(keyValue[0], keyValue[1]);
        }
        return myMap;
    }

    public static boolean isNumeric(String str) {
        String reg = "^[0-9]+(.|-[0-9]+)?$";
        return str.matches(reg);
    }

    public static boolean isValidJSON(String json) {
        try {
            JSONObject.parseObject(json);
        } catch (JSONException e) {
            return false;
        }
        return true;
    }

    public static boolean isDouble(String str) {
        return str.equalsIgnoreCase("NaN") || Pattern.compile("^[0-9]+[.,][0-9]+[dD](,[0,1])?$").matcher(str).matches();
    }

    public static String readResourceFile(String cfgFile) {
        try {
            InputStream in = DataTypeUtil.class.getClassLoader().getResource(cfgFile).openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        } catch (NullPointerException nullPointerException) {
            logger.error(nullPointerException.getMessage());
            return "";
        }
    }

    public static JSONObject convertHashMapToJSONObject(Map<String, String> token) {
        JSONObject result = new JSONObject();
        result.putAll(token);
        return result;
    }
}