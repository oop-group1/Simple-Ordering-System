package com.ordering.system.dao;

import com.ordering.system.model.User;
import java.sql.*;

public class UserDAO {
    private SQLConn db = SQLConn.getInstance();

    /**
     * Finds a user by email in the staff table.
     * @param email The staff email used for login.
     * @return User object if found, else null.
     */
    public User findUserByUsername(String email) {
        String sql = "SELECT usrs_id, staff_email, staff_password FROM tbluserstaff WHERE staff_email = ?";
        try {
            ResultSet rs = db.executeQuery(sql, email);
            if (rs.next()) {
                return new User(
                    rs.getInt("usrs_id"),
                    rs.getString("staff_email"),
                    rs.getString("staff_password"),
                    "Admin" // anyone in tbluserstaff is an Admin
                );
            }
        } catch (SQLException e) {
            System.err.println("[DB ERROR] User lookup failed: " + e.getMessage());
        }
        return null;
    }
}