package org.example.utils;

import org.example.model.User;
import org.springframework.batch.item.file.transform.FieldExtractor;

import java.util.Arrays;
import java.util.List;

public class UserFieldExtractor implements FieldExtractor<User> {
    
    // Define the fields to extract in order
    private final List<String> fieldNames = Arrays.asList("id", "name", "email");
    
    @Override
    public Object[] extract(User user) {
        return new Object[] {
            user.getId(),
            user.getName(),
            user.getEmail()
        };
    }
    
    /**
     * Returns the names of fields that this extractor handles
     * @return List of field names in extraction order
     */
    public List<String> getFieldNames() {
        return fieldNames;
    }
    
    /**
     * Returns the header line for the CSV file
     * @param delimiter The delimiter to use between fields
     * @return A string containing the header line
     */
    public String getHeaderLine(String delimiter) {
        return String.join(delimiter, fieldNames);
    }
}