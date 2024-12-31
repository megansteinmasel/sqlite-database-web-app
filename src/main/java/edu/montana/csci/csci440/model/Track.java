package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Track extends Model {

    private Long trackId;
    private Long albumId;
    private Long mediaTypeId;
    private Long genreId;
    private String name;
    private Long milliseconds;
    private Long bytes;
    private BigDecimal unitPrice;
    private String artistName;
    private String albumTitle;
    private String composer;

    public static final String REDIS_CACHE_KEY = "cs440-tracks-count-cache";

    public Track() {
        mediaTypeId = 1l;
        genreId = 1l;
        milliseconds  = 0l;
        bytes  = 0l;
        unitPrice = new BigDecimal("0");
    }

    Track(ResultSet results) throws SQLException {
        name = results.getString("Name");
        milliseconds = results.getLong("Milliseconds");
        bytes = results.getLong("Bytes");
        unitPrice = results.getBigDecimal("UnitPrice");
        trackId = results.getLong("TrackId");
        albumId = results.getLong("AlbumId");
        mediaTypeId = results.getLong("MediaTypeId");
        genreId = results.getLong("GenreId");

        // additions
        artistName = results.getString("ArtistName");
        albumTitle = results.getString("AlbumTitle");
        composer = results.getString("Composer");


    }

    @Override
    public boolean verify(){
        _errors.clear(); // clear any existing errors
        if (name == null || "".equals(name) ) {
            addError("Name can't be null or blank!");
        }
        if (albumId == null || "".equals(albumId) ) {
            addError("Album can't be null or blank!");
        }
        return !hasErrors();
    }

    @Override
    public void delete() {
        if (verify()) {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "DELETE FROM tracks WHERE TrackId=?;")) {
                stmt.setLong(1, this.getTrackId());
                stmt.executeUpdate();
                return;
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        } else {
            return;
        }
    }



    @Override
    public boolean create(){
        //Jedis jedis = new Jedis();
        //jedis.del("csci-440-track-count-cache");
        if (verify()) {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO tracks (Name, AlbumId, MediaTypeId, GenreId, Composer, Milliseconds, Bytes, UnitPrice) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                stmt.setString(1, this.getName());
                stmt.setLong(2, this.getAlbumId());
                stmt.setLong(3, this.getMediaTypeId());
                stmt.setLong(4, this.getGenreId());
                stmt.setString(5, this.getComposer());
                stmt.setLong(6, this.getMilliseconds());
                stmt.setLong(7, this.getBytes());
                stmt.setBigDecimal(8, this.getUnitPrice());
                stmt.executeUpdate();
                this.trackId = DB.getLastID(conn); // helper method
                return true;
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean update() {

        if (verify()) {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE tracks SET Name=?, AlbumId=? WHERE TrackId=?")) {
                stmt.setString(1, this.getName());
                stmt.setLong(2, this.getAlbumId());
                stmt.setLong(3, this.getTrackId());

                stmt.executeUpdate();
                return true;
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        } else {
            return false;
        }
    }

    public static Track find(long i) {
        try{
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT tracks.TrackId, tracks.Name, tracks.AlbumId, tracks.MediaTypeId," +
                                 " tracks.GenreId, tracks.Composer, tracks.Milliseconds, tracks.Bytes, tracks.UnitPrice," +
                                 " artists.Name AS ArtistName, albums.Title AS AlbumTitle" +
                                 " FROM tracks" +
                                 " JOIN albums ON tracks.AlbumId = albums.AlbumId" +
                                 " JOIN artists ON albums.ArtistId = artists.ArtistId" +
                                 " WHERE tracks.TrackId =?;")) {
                stmt.setLong(1, i);
                ResultSet resultSet = stmt.executeQuery();
                if(resultSet.next()){
                    return new Track(resultSet);
                }else{
                    return null;
                }
            }
        }catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static Long count() {
        return 0l;
    }


    public Album getAlbum() {
        return Album.find(albumId);
    }

    public MediaType getMediaType() {
        return null;
    }
    public Genre getGenre() {
        return null;
    }
    public List<Playlist> getPlaylists(){
        try {
            try (Connection connect = DB.connect();
                 PreparedStatement stmt = connect.prepareStatement("SELECT *" +
                         " FROM playlists" +
                         " JOIN playlist_track ON playlists.PlaylistId = playlist_track.PlaylistId" +
                         " JOIN tracks ON playlist_track.TrackId = tracks.TrackId" +
                         " WHERE tracks.TrackId = ? ORDER BY playlists.Name;")) {
                stmt.setLong(1, this.getTrackId());
                ArrayList<Playlist> result = new ArrayList();
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

    public Long getTrackId() {
        return trackId;
    }

    public void setTrackId(Long trackId) {
        this.trackId = trackId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getMilliseconds() {
        return milliseconds;
    }

    public void setMilliseconds(Long milliseconds) {
        this.milliseconds = milliseconds;
    }

    public Long getBytes() {
        return bytes;
    }

    public void setBytes(Long bytes) {
        this.bytes = bytes;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(Long albumId) {
        this.albumId = albumId;
    }

    public void setAlbum(Album album) {
        albumId = album.getAlbumId();
    }

    public Long getMediaTypeId() {
        return mediaTypeId;
    }

    public void setMediaTypeId(Long mediaTypeId) {
        this.mediaTypeId = mediaTypeId;
    }

    public Long getGenreId() {
        return genreId;
    }

    public void setGenreId(Long genreId) {
        this.genreId = genreId;
    }

    public String getComposer(){return composer;}

    public void setComposer(String composer){ this.composer = composer;}

    public String getArtistName() {
        return artistName;
    }

    public String getAlbumTitle() {
        return albumTitle;
    }

    public static List<Track> advancedSearch(int page, int count,
                                             String search, Integer artistId, Integer albumId, Integer maxRuntime, Integer minRuntime) {


        LinkedList<Object> args = new LinkedList<>();

        String query = "SELECT tracks.*, albums.ArtistId, artists.Name AS ArtistName, albums.Title AS AlbumTitle " +
                " FROM tracks" +
                " JOIN albums ON tracks.AlbumId = albums.AlbumId" +
                " JOIN artists ON artists.ArtistId=albums.ArtistId" +
                " WHERE tracks.name LIKE ?";

        args.add("%" + search + "%");

        // Conditionally include the query and argument
        if (artistId != null) {
            query += " AND artists.ArtistId=? ";
            args.add(artistId);
        }

        if (albumId != null) {
            query += " AND albums.AlbumId=? ";
            args.add(albumId);
        }
        query += " LIMIT ?";
        args.add(count);

        try (Connection conn = DB.connect();  PreparedStatement stmt = conn.prepareStatement(query)) {
            for (int i = 0; i < args.size(); i++) {
                Object arg = args.get(i);
                stmt.setObject(i + 1, arg);
            }

            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results));
            }

            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }

    }


    public static List<Track> search(int page, int count, String orderBy, String search) {
        String query = "SELECT tracks.*, albums.Title as AlbumTitle, artists.Name as ArtistName " +
                "FROM tracks " +
                "JOIN albums ON albums.AlbumId=tracks.AlbumId " +
                "JOIN artists ON artists.ArtistId=albums.ArtistId " +
                "WHERE tracks.Name LIKE ? OR AlbumTitle LIKE ? OR ArtistName LIKE ? " +
                "LIMIT ? " +
                "OFFSET ?";

        search = "%" + search + "%";

        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, search);
            stmt.setString(2, search);
            stmt.setString(3, search);
            stmt.setInt(4, count);
            stmt.setInt(5, count * (page - 1));

            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static List<Track> forAlbum(Long albumId) {
        try {
            try (Connection connect = DB.connect();
                 PreparedStatement stmt = connect.prepareStatement("SELECT tracks.*," +
                         " artists.Name AS ArtistName, albums.Title AS AlbumTitle" +
                         " FROM tracks" +
                         " JOIN albums ON tracks.AlbumId = albums.AlbumId" +
                         " JOIN artists ON albums.ArtistId = artists.ArtistId" +
                         " WHERE artists.ArtistId =?;")) {
                stmt.setLong(1, albumId);
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



    // sure would be nice if java supported default parameter values
    public static List<Track> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Track> all(int page, int count) {
        return all(page, count, "TrackId");
    }

    public static List<Track> all(int page, int count, String orderBy) {
        int offset = (page-1)*count;
        try {
            try (Connection connect = DB.connect();
                 PreparedStatement stmt = connect.prepareStatement("SELECT tracks.TrackId, tracks.Name, tracks.AlbumId, tracks.MediaTypeId," +
                         " tracks.GenreId, tracks.Composer, tracks.Milliseconds, tracks.Bytes, tracks.UnitPrice," +
                         " artists.Name AS ArtistName, albums.Title AS AlbumTitle" +
                         " FROM tracks" +
                         " JOIN albums ON tracks.AlbumId = albums.AlbumId" +
                         " JOIN artists ON albums.ArtistId = artists.ArtistId" +
                         " ORDER BY Milliseconds LIMIT ? OFFSET ?;")) {
                ArrayList<Track> result = new ArrayList();
                stmt.setInt(1, count);
                stmt.setInt(2, offset);
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

}
