package app.services;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

public class GenericClient {

    public static <T> T convertJsonGeneric(String json, Class<T> tClass){
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            return objectMapper.readValue(json, tClass);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }
}
