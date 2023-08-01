package MLMS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class JdbcConnector {
    private static Map<String, Contact> songs = new HashMap<>();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        loadContactsFromDatabase();
        while (true) {
            showMenu();
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    addSong();
                    break;
                case 2:
                    viewSongs();
                    break;
                case 3:
                    searchSong();
                    break;
                case 4:
                    deleteSong();
                    break;
                case 5:
                    System.out.println("Exiting the Songs. Goodbye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void showMenu() {
        System.out.println("\n--- Song Menu ---");
        System.out.println("1. Add Song");
        System.out.println("2. View Songs");
        System.out.println("3. Search Song");
        System.out.println("4. Delete Song");
        System.out.println("5. Exit");
        System.out.print("Enter your choice: ");
    }

    private static Connection getConnection() throws SQLException {
        // Replace with your MySQL database credentials and connection string
        String url = "jdbc:mysql://localhost:3306/music";
        String username = "root";
        String password = "root";
        return DriverManager.getConnection(url, username, password);
    }

    private static void saveContactToDatabase(Contact song) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO songs (name, Artist, Album) VALUES (?, ?, ?)")) {
            statement.setString(1, song.getName());
            statement.setString(2, song.getArtist());
            statement.setString(3, song.getAlbum());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void loadContactsFromDatabase() {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM songs");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String artist = resultSet.getString("Artist");
                String album = resultSet.getString("Album");
                Contact song = new Contact();
                song.setName(name);
                song.setArtist(artist);
                song.setAlbum(album);
                songs.put(name, song);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void addSong() {
        System.out.print("Enter name: ");
        String name = scanner.nextLine();
        if (songs.containsKey(name)) {
            System.out.println(name + " is already in the Songs.");
        } else {
            System.out.print("Enter Artist: ");
            String artist = scanner.nextLine();
            System.out.print("Enter Album: ");
            String album = scanner.nextLine();
            Contact song = new Contact();
            song.setName(name);
            song.setArtist(artist);
            song.setAlbum(album);
            songs.put(name, song);
            saveContactToDatabase(song);
            System.out.println(name + " added to Songs.");
        }
    }

    private static void viewSongs() {
        if (songs.isEmpty()) {
            System.out.println("No Songs found.");
        } else {
            System.out.println("Songs:");
            for (String name : songs.keySet()) {
                Contact song = songs.get(name);
                System.out.println("Name: " + name + ", Artist: " + song.getArtist() + ", Album: " + song.getAlbum());
            }
        }
    }

    private static void searchSong() {
        System.out.print("Enter the name to search for: ");
        String name = scanner.nextLine();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM songs WHERE name LIKE ?")) {
            stmt.setString(1, name + "%");
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                System.out.println("Song not found.");
            } else {
                System.out.println("Search results for '" + name + "':");
                do {
                    System.out.println("Name: " + rs.getString("name") + ", Artist: " + rs.getString("Artist") + ", Album: " + rs.getString("Album"));
                } while (rs.next());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void deleteSong() {
        System.out.print("Enter the name of the Song to delete: ");
        String name = scanner.nextLine();
        if (songs.containsKey(name)) {
            songs.remove(name);
            deleteContactFromDatabase(name);
            System.out.println(name + " has been deleted from Songs.");
        } else {
            System.out.println("Song not found.");
        }
    }

    private static void deleteContactFromDatabase(String name) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM songs WHERE name = ?")) {
            statement.setString(1, name);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

class Contact {
    private String name;
    private String artist;
    private String album;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }
}
