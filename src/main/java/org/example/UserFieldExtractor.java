package org.example;

import org.springframework.batch.item.file.transform.FieldExtractor;

/**
 * Extracts field values from User objects for writing to a flat file.
 * Used by FlatFileItemWriter to determine which fields from the User object
 * should be written to the output file and in what order.
 */
public class UserFieldExtractor implements FieldExtractor<User> {
    
    /**
     * Extracts an array of field values from a User object.
     * The values will be written as columns in the output file.
     * 
     * @param user The User object to extract field values from
     * @return An array of Object values representing the User's fields
     */
    @Override
    public Object[] extract(User user) {
        return new Object[] {
            user.getId(),
            user.getName()
        };
    }
}
