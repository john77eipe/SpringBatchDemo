package org.example;

/**
 * Data model class representing a user entity in the database.
 * This class is used as the domain object for the batch processing operation,
 * mapping database rows to Java objects.
 */
public class User {
    /** The unique identifier of the user */
    private Long id;
    
    /** The name of the user */
    private String name;

    /**
     * Default no-args constructor required for bean instantiation
     */
    public User() {
    }

    /**
     * Constructor with all fields
     * 
     * @param id The user's ID
     * @param name The user's name
     */
    public User(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Gets the user's ID
     * 
     * @return The user's ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the user's ID
     * 
     * @param id The user's ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the user's name
     * 
     * @return The user's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the user's name
     * 
     * @param name The user's name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns a string representation of the User object
     * 
     * @return String representation of the user
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
