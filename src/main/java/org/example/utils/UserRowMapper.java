package org.example.utils;

import org.example.model.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Maps database rows from a ResultSet to User objects.
 * Used by the JdbcPagingItemReader to convert database query results
 * into domain objects that can be processed by the batch job.
 */
public class UserRowMapper implements RowMapper<User> {
    
    /**
     * Maps a single row in a ResultSet to a User object.
     * 
     * @param rs The ResultSet containing the database row
     * @param rowNum The number of the current row
     * @return A User object populated with data from the current row
     * @throws SQLException If a database access error occurs or this method is called on a closed ResultSet
     */
    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        return user;
    }
}
