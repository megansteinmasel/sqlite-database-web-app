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

public class Playlist extends Model {

    Long playlistId;
    String name;

    public Playlist() {
    }

    private Playlist(ResultSet results) throws SQLException {
        name = results.getString("Name");
        playlistId = results.getLong("PlaylistId");
    }


    public List<Track> getTracks(){
        // TODO implement, order by track name
        try {
            try (Connection connect = DB.connect();
                 // join tracks to playlists, where clause use playlist id
                 PreparedStatement stmt = connect.prepareStatement("SELECT tracks.name FROM tracks" +
                         " JOIN playlists ON tracks.trackid = playlists.playlistid" +
                         " WHERE playlistid =?" +
                         " ORDER BY tracks.name;")) {
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
        //return Collections.emptyList();
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

        //return new Playlist();
    }

}
