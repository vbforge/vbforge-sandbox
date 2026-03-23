package com.vbforge.jwtsecurity.repository;

import com.vbforge.jwtsecurity.entity.User;
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

        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";

        try(Connection connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)
        ){
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);
            ps.execute();

        } catch (Exception e){
           throw  new RuntimeException(e);
        }
    }


    public boolean existUser(String login, String password) {

        String sql = "SELECT count(*) as count FROM users WHERE username = ? AND password = ?";

        try(Connection connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)
        ){
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

        String sql = "SELECT password FROM users WHERE username = ?";

        try(Connection connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)
        ){
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getString("password");

        }catch (Exception e){
            throw  new RuntimeException(e);
        }
    }

    public User findUserByUsername(String username){
        String sql = "SELECT * FROM users WHERE username = ?";

        try(Connection connection = dataSource.getConnection();
        PreparedStatement ps = connection.prepareStatement(sql)
        ){
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            User user = new User();
            while(rs.next()){
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
            }
            return user;

        }catch (Exception e){
            throw  new RuntimeException(e);
        }
    }


}
















