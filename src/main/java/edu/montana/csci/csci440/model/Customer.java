package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Customer extends Model {

    private Long customerId;
    private Long supportRepId;
    private String firstName;
    private String lastName;
    private String email;

    public Employee getSupportRep() {
         return Employee.find(supportRepId);
    }

    public List<Invoice> getInvoices(){
        return Collections.emptyList();
    }

    public Customer(){
    }
    private Customer(ResultSet results) throws SQLException {
        firstName = results.getString("FirstName");
        lastName = results.getString("LastName");
        customerId = results.getLong("CustomerId");
        supportRepId = results.getLong("SupportRepId");
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Long getSupportRepId() {
        return supportRepId;
    }

    public static List<Customer> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Customer> all(int page, int count) {
        int offset = (page-1)*count;
        try {
            try (Connection connect = DB.connect();
                 PreparedStatement stmt = connect.prepareStatement("SELECT * FROM customers LIMIT ? OFFSET ?")) {
                ArrayList<Customer> result = new ArrayList();
                stmt.setInt(1, count);
                stmt.setInt(2, offset);
                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    result.add(new Customer(resultSet));
                }
                return result;
            }
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        //return Collections.emptyList();
    }

    public static Customer find(long customerId) {
        return new Customer();
    }

    public static List<Customer> forEmployee(long employeeId) {
        return Collections.emptyList();
    }

}
