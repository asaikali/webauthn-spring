package com.example.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import java.io.IOException;

/**
 * A collection of utility methods for working with JSON using the Jackson libraries. Dates and
 * times are configured to work with java.time package to nanosecond precision.
 */
public class JsonUtils {
  private static final ObjectMapper mapper;

  static {
    mapper = new ObjectMapper();

    mapper.registerModule(new ParameterNamesModule());
    mapper.registerModule(new Jdk8Module());
    mapper.registerModule(new JavaTimeModule());

    mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  }

  private JsonUtils() {}

  /**
   * Formats a JSON String by adding indentation to the string. This works by converting the JSON
   * string to a Java object and back to a string with indentation turned on.
   *
   * @param json the string json object
   * @return the formatted json string
   */
  public static String format(String json) {
    return toJson(fromJson(json, Object.class));
  }

  /**
   * Converts a string to an instance of a Java object of the specified type.
   *
   * @param json the json string to convert to a java object
   * @param type the class of the java object that the json string be converted to
   * @param <T> the tye of the java object to convert the json string to
   * @return an instance of the java object of the specified type
   */
  public static <T> T fromJson(String json, Class<T> type) {
    try {
      return mapper.readValue(json, type);
    } catch (IOException e) {
      throw new JsonUtilsException(
          String.format(
              "Unable to parse json value into java object of type '%s' using jackson ObjectMapper",
              type.getName()),
          e);
    }
  }

  /**
   * Returns a string json string from a java object.
   *
   * @param object the java object to turn into a json string
   * @return a string json representation of the java object
   */
  public static String toJson(Object object) {
    try {
      return mapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new JsonUtilsException(
          String.format(
              "Unable to convert Java object of type '%s' to json using jackson ObjectMapper",
              object.getClass().getName()),
          e);
    }
  }
}
