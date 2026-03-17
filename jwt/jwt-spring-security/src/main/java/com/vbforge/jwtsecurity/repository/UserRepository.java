package com.vbforge.jwtsecurity.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Repository
@AllArgsConstructor
public class UserRepository {

    private DataSource dataSource;

    public void createUser(String username, String password, String role) {
        try(Connection connection = dataSource.getConnection()){

            PreparedStatement ps = connection.prepareStatement("INSERT INTO users (username, password, role) VALUES (?, ?, ?)");
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);
            ps.execute();

        } catch (Exception e){
           throw  new RuntimeException(e);
        }

    }


    public boolean existUser(String login, String password) {

        try(Connection connection = dataSource.getConnection()){

            PreparedStatement ps = connection.prepareStatement("SELECT count(*) as count FROM users WHERE username = ? AND password = ?");
            ps.setString(1, login);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int count = rs.getInt("count");
            return count > 0;

        }catch (Exception e){
            throw  new RuntimeException(e);
        }
    }

    public String findPasswordByUsername(String login){
        try(Connection connection = dataSource.getConnection()){
            PreparedStatement ps = connection.prepareStatement("SELECT password FROM users WHERE username = ?");
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getString("password");

        }catch (Exception e){
            throw  new RuntimeException(e);
        }
    }

}
















