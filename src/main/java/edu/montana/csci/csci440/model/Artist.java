package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Artist extends Model {

    Long artistId;
    String name;
    String oldName;
    Long oldArtistId;

    public Artist() {
    }

    private Artist(ResultSet results) throws SQLException {
        name = results.getString("Name");
        oldName = name;
        artistId = results.getLong("ArtistId");
        oldArtistId = artistId;
    }

    @Override
    public void delete() {
        if (verify()) {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "DELETE FROM artists WHERE ArtistId=?;")) {
                stmt.setLong(1, this.getArtistId());
                stmt.execute();
                return;
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        } else {
            return;
        }
    }

    @Override
    public boolean verify() {
        _errors.clear(); // clear any existing errors
        if (name == null || "".equals(name)) {
            addError("Name can't be null or blank!");
        }
        return !hasErrors();
    }

    @Override
    public boolean create(){
        if (verify()) {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO artists (Name) VALUES (?)")) {
                stmt.setString(1, this.getName());
                stmt.executeUpdate();
                this.artistId = DB.getLastID(conn); // helper method
                return true;
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        } else {
            return false;
        }
    }

    // Optimistic Concurrency
    @Override
    public boolean update() {
        if (verify()) {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE artists SET Name=? WHERE ArtistId=? AND Name=?")) {
                String newName = this.getName();
                stmt.setString(1, newName);
                stmt.setLong(2, artistId);
                stmt.setString(3, oldName);
                int updated = stmt.executeUpdate();

                if(updated == 1){
                    return true;
                }else{
                    return false;
                }

            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        } else {
            return false;
        }
    }

    public List<Album> getAlbums(){
        return Album.getForArtist(artistId);
    }

    public Long getArtistId() {
        return artistId;
    }

    public void setArtist(Artist artist) {
        this.artistId = artist.getArtistId();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static List<Artist> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Artist> all(int page, int count) {
        int offset = (page-1)*count;
        try {
            try (Connection connect = DB.connect();
                 PreparedStatement stmt = connect.prepareStatement("SELECT * FROM artists LIMIT ? OFFSET ?")) {
                ArrayList<Artist> result = new ArrayList();
                stmt.setInt(1, count);
                stmt.setInt(2, offset);
                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    result.add(new Artist(resultSet));
                }
                return result;
            }
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    public static Artist find(long i) {
        try{
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT * FROM artists WHERE ArtistId = ?")) {
                stmt.setLong(1, i);
                ResultSet resultSet = stmt.executeQuery();
                if(resultSet.next()){
                    return new Artist(resultSet);
                }else{
                    return null;
                }
            }
        }catch(SQLException e) {
            throw new RuntimeException(e);
        }

    }



}
