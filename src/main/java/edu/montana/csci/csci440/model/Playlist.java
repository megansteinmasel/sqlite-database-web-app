package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;
import redis.clients.jedis.Jedis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Playlist extends Model {

    Long playlistId;
    String name;

    public Playlist() {
    }

    Playlist(ResultSet results) throws SQLException {
        name = results.getString("Name");
        playlistId = results.getLong("PlaylistId");
    }

    @Override
    public void delete() {
        String query = "DELETE FROM playlist_track WHERE PlaylistId=?";
        try (Connection conn = DB.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, this.getPlaylistId());
            stmt.executeUpdate();
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    @Override
    public boolean update() {
        if (verify()) {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE playlists SET Name=? WHERE PlaylistId=?")) {
                stmt.setString(1, this.getName());
                stmt.setLong(2, this.getPlaylistId());
                stmt.executeUpdate();
                return true;
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean create(){
        if (verify()) {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO playlists (Name) VALUES (?)")) {
                stmt.setString(1, this.getName());
                stmt.executeUpdate();
                this.playlistId = DB.getLastID(conn); // helper method
                return true;
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        } else {
            return false;
        }
    }

    public List<Track> getTracks(){
        // order by track name
        try {
            try (Connection connect = DB.connect();
                 PreparedStatement stmt = connect.prepareStatement("SELECT tracks.TrackId, tracks.Name, tracks.AlbumId, tracks.MediaTypeId," +
                         " tracks.GenreId, tracks.Composer, tracks.Milliseconds, tracks.Bytes, tracks.UnitPrice," +
                         " artists.Name AS ArtistName, albums.Title AS AlbumTitle" +
                         " FROM tracks" +
                         " JOIN albums ON tracks.AlbumId = albums.AlbumId" +
                         " JOIN artists ON albums.ArtistId = artists.ArtistId" +
                         " JOIN playlist_track ON tracks.TrackId = playlist_track.TrackId" +
                         " JOIN playlists ON playlist_track.PlaylistId = playlists.PlaylistId" +
                         " WHERE playlists.PlaylistId = ? ORDER BY tracks.Name;")) {
                stmt.setLong(1, this.getPlaylistId());
                ArrayList<Track> result = new ArrayList();
                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    result.add(new Track(resultSet));
                }
                return result;
            }
        }catch (SQLException e){
            throw new RuntimeException(e);
        }

    }

    public Long getPlaylistId() {
        return playlistId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static List<Playlist> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Playlist> all(int page, int count) {
        int offset = (page-1)*count;
        try {
            try (Connection connect = DB.connect();
                 PreparedStatement stmt = connect.prepareStatement("SELECT * FROM playlists LIMIT ? OFFSET ?")) {
                ArrayList<Playlist> result = new ArrayList();
                stmt.setInt(1, count);
                stmt.setInt(2, offset);
                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    result.add(new Playlist(resultSet));
                }
                return result;
            }
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    public static Playlist find(int i) {
        try{
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT * FROM playlists WHERE PlaylistId = ?")) {
                stmt.setLong(1, i);
                ResultSet resultSet = stmt.executeQuery();
                if(resultSet.next()){
                    return new Playlist(resultSet);
                }else{
                    return null;
                }
            }
        }catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
