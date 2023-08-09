/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.cloudserver;

import java.io.BufferedReader;

/**
 *
 * @author ntu-user
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileManagement {

    private Connection connection;

    public FileManagement(Connection connection) {
        this.connection = connection;
    }

    DbConnection con = new DbConnection();

    /**
     * Method to create file and append content to the file.
     * @param fileName The name of the file to be created.
     * @param username The username of user trying to create the file.
     * @param content The content that will be written to the file.
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void createFile(String fileName, String username, String content) throws SQLException, IOException, ClassNotFoundException {
        PreparedStatement stmt = null;
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:comp20081.db");

            //Specify the path for saving files
            String filePath = "/home/ntu-user/userFiles/";

            //Checking if user is logged in or not
            stmt = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.getInt("isLoggedIn") == 0) {
                throw new IllegalArgumentException("User is not loggedin. Cannot create a File.");
            }

            int userId = rs.getInt("id");

            //Checking if the file already exists for this user
            stmt = connection.prepareStatement("SELECT * FROM files WHERE file_name = ? AND user_id = ?");
            stmt.setString(1, fileName);
            stmt.setInt(2, userId);
            ResultSet rs2 = stmt.executeQuery();
            if (rs2.next()) {
                throw new IllegalArgumentException("File already exists. Please choose a different file name.");
            }

            //If file extension is not included, append it.
            if (!fileName.contains(".txt")) {
                fileName = fileName + ".txt";
            }

            //Creating empty file with specified name.
            File file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();

                int fileSize = (int) file.length();

                //Adding information about created file to database.
                stmt = connection.prepareStatement("INSERT INTO files (file_name, file_path, file_size, user_id) VALUES (?, ?, ?, ?)");
                stmt.setString(1, fileName);
                stmt.setString(2, filePath);
                stmt.setInt(3, fileSize);
                stmt.setInt(4, userId);
                stmt.executeUpdate();
                stmt.close();
            }

            //Appending content to the file
            this.appendToFile(fileName, filePath, content);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            stmt.close();
            connection.close();
        }
    }

    /**
     * Method to append given text to a file.
     * @param fileName The name of the file where we need to append the content.
     * @param filePath The path of file.
     * @param newContent The content that needs to be added to the file.
     * @throws IOException
     * @throws SQLException
     */
    public void appendToFile(String fileName, String filePath, String newContent) throws IOException, SQLException {

        if (!fileName.contains(".txt")) {
            fileName = fileName + ".txt";
        }

        try (FileWriter fileWriter = new FileWriter(filePath + fileName, true)) {

            fileWriter.write(newContent);
        }

        File file = new File(filePath + fileName);
        long fileSize = file.length();

        PreparedStatement stmt = connection.prepareStatement("UPDATE files SET file_size = ? WHERE file_name = ?");
        stmt.setLong(1, fileSize);
        stmt.setString(2, fileName);
        stmt.executeUpdate();
        stmt.close();
    }

    /**
     * Method to get all the files of a user.
     * @param username The username for which we need to retrieve the list of files.
     * @return Returns List<String> containing names of all the files.
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public List<String> getFilesForUser(String username) throws SQLException, ClassNotFoundException {
        PreparedStatement stmt = null;
        List<String> fileNames = new ArrayList<>();

        try {
            stmt = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            if (rs.getInt("isLoggedIn") == 0) {
                throw new IllegalArgumentException("User is not loggedin. Cannot retrieve list of files.");
            }

            int userId = rs.getInt("id");

            //Fetching all the files of given user.
            stmt = connection.prepareStatement("SELECT file_name FROM files WHERE user_id = ?");
            stmt.setInt(1, userId);
            ResultSet rs2 = stmt.executeQuery();

            while (rs2.next()) {
                fileNames.add(rs2.getString("file_name"));
            }

            if (fileNames.isEmpty()) {
                throw new IllegalArgumentException("No files found for the user.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            connection.close();
        }

        return fileNames;
    }


/**
 * @brief This method uploads a file to containers.
 * @param fileName Name of the file to be uploaded
 * @param username Username of the user
 * @throws IOException
 * @throws ClassNotFoundException
 */
public void uploadFile(String fileName, String username) throws IOException, ClassNotFoundException {
    try {
        // Validate user access details
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.getInt("isLoggedIn") == 0){
            throw new IllegalArgumentException("User is not logged in. Cannot upload file.");
        }

        stmt = connection.prepareStatement("SELECT * FROM files WHERE file_name = ?");
        stmt.setString(1, fileName);
        ResultSet rs2 = stmt.executeQuery();

        if (rs2.next()) {
            throw new IllegalArgumentException("File already exists. Please choose a different file name.");
        }

        int userId = rs.getInt("id");

        // Checking and modifying filename properly
        if (!fileName.contains(".txt")) {
            fileName = fileName + ".txt";
        }

        // Splitting into chunks for parallel processing
        Path filePath = Paths.get("/home/ntu-user/userFiles/" + fileName);
        String content = Files.readString(filePath);

        List<String> chunks = splitIntoChunks(content);
        List<String> chunkFilenames = new ArrayList<>();

        // Creating thread pool with fixed number of threads
        ExecutorService executor = Executors.newFixedThreadPool(4);

        // Processing each chunk in parallel
        for (int i = 0; i < chunks.size(); i++) {
            final int chunkIndex = i;
            final String chunk = fileName + "_chunk_" + chunkIndex + ".txt";
            chunkFilenames.add(chunk);
            final String fileName1 = fileName;

            // Executing each chunk in parallel
            executor.execute(() -> {
                try {
                    Path chunkPath = Paths.get("./uploads/" + chunk);
                    Files.writeString(chunkPath, chunks.get(chunkIndex));

                    final String containerIpAddress;

                    // Choosing correct container for storing chunk based on container index
                    switch (chunkIndex) {
                        case 1:
                            containerIpAddress = "172.17.0.4";
                            break;
                        case 2:
                            containerIpAddress = "172.17.0.3";
                            break;
                        case 3:
                            containerIpAddress = "172.17.0.2";
                            break;
                        default:
                            containerIpAddress = "172.17.0.1";
                            break;
                    }

                    // Copying our chunk file to the container
                    String chunkFilePath = "./uploads/" + chunk;
                    String containerFilePath = "/userFiles";

                    ProcessBuilder pb = new ProcessBuilder("scp", "-P", "22", chunkFilePath, "root@" + containerIpAddress + ":" + containerFilePath);
                    pb.inheritIO();
                    Process p = pb.start();
                    p.waitFor();

                    // Storing chunk details in database
                    try (PreparedStatement stmt2 = connection.prepareStatement("INSERT INTO chunks (file_name, chunk_filename, user_Id, container_index) VALUES (?, ?, ?, ?)")) {
                        stmt2.setString(1, fileName1);
                        stmt2.setString(2, chunkFilenames.get(chunkIndex));
                        stmt2.setInt(3, userId);
                        stmt2.setInt(4, chunkIndex);
                        stmt2.executeUpdate();
                    }
                } catch (IOException | InterruptedException | SQLException e) {
                    System.out.println(e.getMessage());
                }
            });
        }

        executor.shutdown();

        // Make sure all threads complete within given time frame
        try {
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }

        // Deleting the original file from host
        String filePath2 = "/home/ntu-user/userFiles/";

        File file = new File(filePath2 + fileName);
        if (file.exists()) {
            file.delete();
        }
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
}

/**
 * @brief This method splits a file into chunks for parallel processing.
 * @param content Content of the file
 * @return List of all the chunks as strings
 */
private List<String> splitIntoChunks(String content) {
    List<String> chunks = new ArrayList<>();

    int chunkSize = content.length() / 4;

    for (int i = 0; i < 4; i++) {
        int startIndex = i * chunkSize;
        int endIndex = startIndex + chunkSize;
        if (i == 3) {
            endIndex = content.length();
        }
        chunks.add(content.substring(startIndex, endIndex));
    }

    return chunks;
}

/**
 * @brief This method downloads a file from containers.
 * @param fileName Name of the file to be downloaded
 * @param username Username of the user
 * @return The downloaded file
 * @throws IOException
 * @throws SQLException
 */
public File fileDownload(String fileName, String username) throws IOException, SQLException {
    try {
        // Validate user access details
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
        stmt.setString(1, username);

        ResultSet rs = stmt.executeQuery();

        if (rs.getInt("isLoggedIn") == 0){
            throw new IllegalArgumentException("User is not logged in. Cannot download file.");
        }

        // Checking and modifying filename properly
        if (!fileName.contains(".txt")) {
            fileName = fileName + ".txt";
        }

        stmt = connection.prepareStatement("SELECT * FROM files WHERE file_name = ?");
        stmt.setString(1, fileName);
        ResultSet rs2 = stmt.executeQuery();

        if (!rs2.next()) {
            throw new IllegalArgumentException("File does not exist. Please choose a different file name.");
        }

        stmt = connection.prepareStatement("SELECT * FROM chunks WHERE file_name = ?");
        stmt.setString(1, fileName);
        ResultSet rs3 = stmt.executeQuery();

        List<String> chunkFilenames = new ArrayList<>();

        // Downloading each chunk from container and storing it in string builder
        StringBuilder sb = new StringBuilder();
        while (rs3.next()) {
            String chunk = rs3.getString("chunk_filename");
            String containerIndex = rs3.getString("container_index");
            chunkFilenames.add(chunk);

            String chunkContent;

            if (containerIndex != null) {
                final String containerIpAddress;
                // Choosing correct container for downloading chunk based on container index
                switch (containerIndex) {
                    case "1":
                        containerIpAddress = "172.17.0.1";
                        break;
                    case "2":
                        containerIpAddress = "172.17.0.2";
                        break;
                    case "3":
                        containerIpAddress = "172.17.0.3";
                        break;
                    default:
                        containerIpAddress = "172.17.0.4";
                        break;
                }
                String containerFilePath = "/userFiles/" + chunk;
                ProcessBuilder pb = new ProcessBuilder("ssh", "-p", "22", "root@" + containerIpAddress + ":" + containerFilePath);
                pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
                Process p = pb.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                StringBuilder output = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
                chunkContent = output.toString();
            } else {
                throw new IllegalArgumentException("The file wasn't uploaded.");
            }

            sb.append(chunkContent);
        }

        Collections.sort(chunkFilenames);

        // Sorting chunks by filename and storing it in string builder
        StringBuilder sb1 = new StringBuilder();
        for (String chunk : chunkFilenames) {
            String chunkFilePath = "./uploads/" + chunk;
            String chunkContent = Files.readString(Paths.get(chunkFilePath));
            sb1.append(chunkContent);
        }
        String combinedContent = sb1.toString();

        // Writing combined content into file and returning the file path
        Path filePath = Paths.get("/home/ntu-user/userFiles/Downloads/" + fileName);
        Files.writeString(filePath, combinedContent);

        return filePath.toFile();
    } finally {
        if (connection != null) {
            connection.close();
        }
    }
}


/**
 * @brief A method to share a file among other users, specifying the read and write permissions.
 * @param fileName name of the file 
 * @param ownerUsername username of the owner of the file
 * @param recipientUsernames list of usernames of the recipients
 * @param readPermission boolean indicating whether the recipients should have read permission 
 * @param writePermission boolean indicating whether the recipients should have write permission 
 * @throws SQLException
 */
public void shareFile(String fileName, String ownerUsername, List<String> recipientUsernames, boolean readPermission, boolean writePermission) throws SQLException {

    // Preparing the SQL statement to check if the owner is logged in
    PreparedStatement stmt;
    stmt = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
    stmt.setString(1, ownerUsername);
    
    // Executing the SQL statement
    ResultSet rs = stmt.executeQuery();

    // Checking if the owner is logged in
    if (rs.getInt("isLoggedIn") == 0) {
        throw new IllegalArgumentException("User is not logged in. Cannot download file.");
    }

    // Preparing the SQL statement to check if the recipient user exists
    String recipientUsername1 = String.join(",", Collections.nCopies(recipientUsernames.size(), "?"));
    String sql = "SELECT * FROM users WHERE username IN (" + recipientUsername1 + ")";
    stmt = connection.prepareStatement(sql);
    for (int i = 0; i < recipientUsernames.size(); i++) {
        stmt.setString(i + 1, recipientUsernames.get(i));
    }
    
    // Executing the SQL statement
    ResultSet rs2 = stmt.executeQuery();

    // Checking if the recipient user exists
    if (!rs2.next()) {
        throw new IllegalArgumentException("Recipient User does not exist");
    }

    // Adding .txt extension if file name doesn't already have it
    if (!fileName.contains(".txt")) {
        fileName = fileName + ".txt";
    }

    // Preparing the SQL statement to check if the file exists
    stmt = connection.prepareStatement("SELECT * FROM files WHERE file_name = ?");
    stmt.setString(1, fileName);
    ResultSet rs3 = stmt.executeQuery();

    // Checking if the file exists
    if (!rs3.next()) {
        throw new IllegalArgumentException("File does not exist. Please choose a different file name.");
    }

    // Getting the ID of the owner user
    int ownerUserId = rs.getInt("id");

    // Iterating over the recipient usernames and adding permission in the database for each one
    for (String recipientUsername : recipientUsernames) {
        // Preparing the SQL statement to check if the recipient user exists
        stmt = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
        stmt.setString(1, recipientUsername);
        ResultSet rs4 = stmt.executeQuery();

        // Checking if the recipient user exists
        if (!rs4.next()) {
            throw new IllegalArgumentException("Recipient user does not exist. Please choose a different recipient.");
        }

        // Getting the ID of the recipient user
        int recipientUserId = rs4.getInt("id");
        int readPermissionInt = readPermission ? 1 : 0;
        int writePermissionInt = writePermission ? 1 : 0;

        // Preparing the SQL statement to add permission for the recipient user
        stmt = connection.prepareStatement("INSERT INTO file_permission (file_name, owner_user_id, recipient_user_id, read_permission, write_permission) VALUES (?, ?, ?, ?, ?)");
        stmt.setString(1, fileName);
        stmt.setInt(2, ownerUserId);
        stmt.setInt(3, recipientUserId);
        stmt.setInt(4, readPermissionInt);
        stmt.setInt(5, writePermissionInt);

        // Executing the SQL statement
        stmt.executeUpdate();
    }
}

/**
 * @brief A method to delete files from the containers. Files will be deleted permanently after 30 days of being deleted.
 * @param fileName name of the file
 * @param username username of the user requesting the file deletion 
 * @throws SQLException
 * @throws IOException
 * @throws InterruptedException
 */
public void deleteFile(String fileName, String username) throws SQLException, IOException, InterruptedException {
    PreparedStatement stmt = null;
    try {
        // Preparing the SQL statement to check if the user is logged in
        stmt = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
        stmt.setString(1, username);

        // Executing the SQL statement
        ResultSet rs = stmt.executeQuery();

        // Checking if the user is logged in
        if (rs.getInt("isLoggedIn") == 0) {
            throw new IllegalArgumentException("User is not logged in. Cannot delete a file.");
        }

        // Getting the ID of the user
        int userId = rs.getInt("id");

        // Adding .txt extension if file name doesn't already have it
        if (!fileName.contains(".txt")) {
            fileName = fileName + ".txt";
        }

        // Preparing the SQL statement to check if the file exists and the user has permission to delete it
        stmt = connection.prepareStatement("SELECT * FROM files WHERE file_name = ? AND user_id = ?");
        stmt.setString(1, fileName);
        stmt.setInt(2, userId);
        ResultSet rs2 = stmt.executeQuery();

        // Checking if the file exists and the user has permission to delete it
        if (!rs2.next()) {
            throw new IllegalArgumentException("User does not have permission to delete the file/Incorrect file name.");
        }

        // Getting the file path and deletion time 
        String filePath = rs2.getString("file_path");
        Timestamp deletedAt = rs2.getTimestamp("deleted_at");

        // Deleting the file
        File file = new File(filePath + fileName);
        if (file.exists()) {
            file.delete();
        }

        // Updating the deletion timestamp of the file and deleting old chunks and permissions
        stmt = connection.prepareStatement("UPDATE files SET deleted_at = CURRENT_TIMESTAMP WHERE file_name = ? AND user_id = ?");
        stmt.setString(1, fileName);
        stmt.setInt(2, userId);
        stmt.executeUpdate();

        stmt = connection.prepareStatement("DELETE FROM chunks WHERE file_name = ? AND user_Id = ? AND deleted_at < DATETIME('now', '-30 days')");
        stmt.setString(1, fileName);
        stmt.setInt(2, userId);
        stmt.executeUpdate();

        stmt = connection.prepareStatement("DELETE FROM files WHERE file_name = ? AND user_Id = ? AND deleted_at < DATETIME('now', '-30 days')");
        stmt.setString(1, fileName);
        stmt.setInt(2, userId);
        stmt.executeUpdate();

        stmt = connection.prepareStatement("UPDATE chunks SET deleted_at = CURRENT_TIMESTAMP WHERE file_name = ? AND user_id = ?");
        stmt.setString(1, fileName);
        stmt.setInt(2, userId);
        stmt.executeUpdate();

        stmt = connection.prepareStatement("DELETE FROM file_permission WHERE file_name = ? AND owner_user_id = ?");
        stmt.setString(1, fileName);
        stmt.setInt(2, userId);
        stmt.executeUpdate();

        // Finding all the chunks of the file
        stmt = connection.prepareStatement("SELECT * FROM chunks WHERE file_name = ? AND user_Id = ? AND deleted_at > DATETIME('now', '-30 days')");
        stmt.setString(1, fileName);
        stmt.setInt(2, userId);
        ResultSet rs3 = stmt.executeQuery();

        // Iterating over the chunks and deleting them from the containers
        while (rs3.next()) {
            String chunk = rs3.getString("chunk_filename");
            int containerIndex = rs3.getInt("container_index");
            final String containerIpAddress;

            // Setting the IP address of the container based on the index
            switch (containerIndex) {
                case 1:
                    containerIpAddress = "172.17.0.1";
                    break;
                case 2:
                    containerIpAddress = "172.17.0.2";
                    break;
                case 3:
                    containerIpAddress = "172.17.0.3";
                    break;
                default:
                    containerIpAddress = "172.17.0.4";
                    break;
            }

            // Deleting the chunk using SSH command
            ProcessBuilder pb = new ProcessBuilder("ssh", "-p", "22", "root@" + containerIpAddress, "rm", "/userFiles/" + chunk);

            Process p = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            p.waitFor();
        }

    } finally {
        if (stmt != null) {
            stmt.close();
        }
    }
}



// A method to restore a deleted file with the given filename for the given user
public void fileStore(String fileName, String username) throws InterruptedException, FileNotFoundException, IOException, SQLException {
    try {
        // Prepare SQL statement to select user from DB
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
        stmt.setString(1, username);

        // Execute query to get result set of the user
        ResultSet rs = stmt.executeQuery();

        // If no results found, throw exception that user does not exist
        if (!rs.next()) {
            throw new IllegalArgumentException("User does not exist. Cannot restore file.");
        }

        // If user is not logged in, throw exception that user is not logged in and cannot restore file
        if (rs.getInt("isLoggedIn") == 0) {
            throw new IllegalArgumentException("User is not logged in. Cannot restore file.");
        }

        // If the file name does not contain a .txt extension, add it to the end of the filename
        if (!fileName.contains(".txt")) {
            fileName = fileName + ".txt";
        }

        // Prepare SQL statement to select the deleted file from the files table
        stmt = connection.prepareStatement("SELECT * FROM files WHERE file_name = ? AND deleted_at IS NOT NULL");
        stmt.setString(1, fileName);

        // Execute query to get result set of the deleted file
        ResultSet rs2 = stmt.executeQuery();

        // If no results found, throw exception that file does not exist or has not been deleted
        if (!rs2.next()) {
            throw new IllegalArgumentException("File does not exist or has not been deleted. Cannot restore file.");
        }

        // Prepare SQL statement to select the deleted_at timestamp of the file
        stmt = connection.prepareStatement("SELECT deleted_at FROM files WHERE file_name = ?");
        stmt.setString(1, fileName);

        // Execute query to get result set of the deleted file's timestamp
        ResultSet rs3 = stmt.executeQuery();

        // If result set has a value, this means the file was deleted and is eligible for restoration
        if (rs3.next()) {
            // Get the deleted_at timestamp from the result set
            Timestamp deletedAt = rs3.getTimestamp("deleted_at");

            // Check if the timestamp is older than 30 days - if so, restore the file by setting its deleted_at value to null
            if (deletedAt.before(new Timestamp(System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000))) {
                stmt = connection.prepareStatement("UPDATE files SET deleted_at = null WHERE file_name = ?");
                stmt.setString(1, fileName);
                stmt.executeUpdate();
            } else {
                // If the file was deleted more than 30 days ago, throw an exception that the file cannot be restored
                throw new SQLException("File cannot be restored because it has been deleted over 30 days.");
            }

        } else {
            // If no result set was returned, this means the file was not found
            throw new SQLException("File not found.");
        }

        // Get the user ID from the initial user query result set
        int userId = rs.getInt("id");
        
        // Set the local filePath variable to the default download directory and add the filename
        String filePath = "/home/ntu-user/Downloads/" + fileName;

        // Create two empty lists, one for the file chunks and one for their corresponding chunk filenames
        List<String> chunks = new ArrayList<>();
        List<String> chunkFilenames = new ArrayList<>();

        // Save the filename as a final string so it can be used in a lambda expression below
        final String filename = fileName;

        // Prepare SQL statement to select all chunk filenames for the given file name and user ID
        stmt = connection.prepareStatement("SELECT chunk_filename FROM chunks WHERE file_name = ? AND user_id = ?");
        stmt.setString(1, fileName);
        stmt.setInt(2, userId);

        // Execute query to get result set of all chunk filenames that match the search
        ResultSet rs4 = stmt.executeQuery();

        // Loop through the result set and add the chunk filenames to the chunkFilenames list
        while (rs4.next()) {
            chunkFilenames.add(rs4.getString("chunk_filename"));
        }

        // Create a new ExecutorService with a fixed thread pool size of 4
        ExecutorService executor = Executors.newFixedThreadPool(4);

        // Loop through each chunk filename and run a separate thread for each to retrieve it from its designated container
        for (int i = 0; i < chunkFilenames.size(); i++) {
            int chunkIndex = i;
            String chunk = chunkFilenames.get(i);
            executor.execute(() -> {
                try {
                    // Prepare SQL statement to select the deleted_at timestamp of the chunk
                    PreparedStatement stmt2 = connection.prepareStatement("SELECT deleted_at FROM chunks WHERE file_name = ? AND user_id = ? AND deleted_at IS NOT NULL");
                    stmt2.setString(1, filename);
                    stmt2.setInt(2, userId);

                    // Execute query to get result set of the deleted chunk's timestamp
                    ResultSet rs5 = stmt2.executeQuery();
                    if (rs5.next()) {
                        // Get the deleted_at timestamp from the result set
                        Timestamp chunkDeletedAt = rs5.getTimestamp("deleted_at");

                        // Check if the timestamp is older than 30 days - if so, restore the chunk by setting its deleted_at value to null
                        if (chunkDeletedAt.before(new Timestamp(System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000))) {
                            // Assign the container IP address based on the chunk index value
                            final String containerIpAddress;
                            switch (chunkIndex) {
                                case 1:
                                    containerIpAddress = "172.17.0.5";
                                    break;
                                case 2:
                                    containerIpAddress = "172.17.0.6";
                                    break;
                                case 3:
                                    containerIpAddress = "172.17.0.3";
                                    break;
                                default:
                                    containerIpAddress = "172.17.0.4";
                                    break;
                            }
                            // Set the container file path and local chunk file path
                            String containerFilePath = "/userFiles/" + chunk;
                            String chunkFilePath = "./uploads/" + chunk;

                            // Use a ProcessBuilder to run an SCP command to copy the chunk from the container to the local file system
                            ProcessBuilder pb = new ProcessBuilder("scp", "-P", "22", chunkFilePath, "root@" + containerIpAddress + ":" + containerFilePath);
                            pb.inheritIO();
                            Process p = pb.start();
                            p.waitFor();

                            // Prepare SQL statement to update the deleted_at timestamp of the chunk to null
                            stmt2 = connection.prepareStatement("UPDATE chunks SET deleted_at = null WHERE file_name = ? AND user_id = ? AND chunk_filename = ?");
                            stmt2.setString(1, filename);
                            stmt2.setInt(2, userId);
                            stmt2.setString(3, chunk);

                            // Execute the update statement
                            stmt2.executeUpdate();

                            // Add the chunk filename to the list of restored chunks
                            chunks.add(chunk);
                        } else {
                            // If the chunk was deleted more than 30 days ago, throw an exception that the chunk cannot be restored
                            throw new SQLException("File chunk cannot be restored because it has been deleted over 30 days.");
                        }
                    }
                } catch (SQLException e) {
                } catch (IOException | InterruptedException ex) {
                    Logger.getLogger(FileManagement.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }

        // Shut down the ExecutorService and wait for all threads to finish processing
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        // Sort the list of restored chunks by filename
        Collections.sort(chunkFilenames);

        // Create a new StringBuilder
        StringBuilder sb = new StringBuilder();

        // Loop through each chunk file name and read its contents using Java's Files class, then append them to the StringBuilder
        for (String chunk : chunkFilenames) {
            String chunkFilePath = "./uploads/" + chunk;
            String chunkContent = Files.readString(Paths.get(chunkFilePath));
            sb.append(chunkContent);
        }

        // Convert the StringBuilder to a single combined string of all chunk contents
        String combinedContent = sb.toString();

        // Set the path to the restored file, then use Java's Files class to write the combined content to that file
        String restoredFilePath = "/home/ntu-user/Downloads/" + fileName;
        Files.writeString(Paths.get(restoredFilePath), combinedContent);

        // Prepare SQL statement to update the deleted_at timestamp of the file to null
        stmt = connection.prepareStatement("UPDATE files SET deleted_at = null WHERE file_name = ?");
        stmt.setString(1, fileName);

        // Execute the update statement
        stmt.executeUpdate();

    } catch (SQLException e) {
    }
}


/**
 * @brief This method renames a file.
 * @param oldName the old name of the file
 * @param newName the new desired name of the file
 * @param username the username of the user making the request
 * @throws IOException if there is an issue with IO
 * @throws SQLException if there is an issue with SQL
 * @throws InterruptedException if there is an interruption error
 */
public void renameFile(String oldName, String newName, String username) throws IOException, SQLException, InterruptedException {

    // Check if the user is logged in
    PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
    stmt.setString(1, username);
    ResultSet rs = stmt.executeQuery();
    if (rs.getInt("isLoggedIn") == 0) {
        throw new IllegalArgumentException("User is not logged in. Cannot rename file.");
    }

    // Ensure that the file names have .txt extension
    if (!newName.contains(".txt")) {
        newName = newName + ".txt";
    }
    if (!oldName.contains(".txt")) {
        oldName = oldName + ".txt";
    }

    // Check if the old file exists and new file name does not exist
    stmt = connection.prepareStatement("SELECT * FROM files WHERE file_name = ?");
    stmt.setString(1, oldName);
    ResultSet rs2 = stmt.executeQuery();
    if (!rs2.next()) {
        throw new IllegalArgumentException("File does not exist. Cannot rename file.");
    }
    stmt = connection.prepareStatement("SELECT * FROM files WHERE file_name = ?");
    stmt.setString(1, newName);
    ResultSet rs3 = stmt.executeQuery();
    if (rs3.next()) {
        throw new IllegalArgumentException("New file name is already in use. Please choose a different file name.");
    }

    // Get all chunk filenames for this file and execute the renaming operation using threads
    stmt = connection.prepareStatement("SELECT chunk_filename FROM chunks WHERE file_name = ?");
    stmt.setString(1, oldName);
    ResultSet rs4 = stmt.executeQuery();
    List<String> chunkFilenames = new ArrayList<>();
    while (rs4.next()) {
        chunkFilenames.add(rs4.getString("chunk_filename"));
    }

    ExecutorService executor = Executors.newFixedThreadPool(4);
    for (int i = 0; i < chunkFilenames.size(); i++) {
        final int chunkIndex = i;
        final String chunk = chunkFilenames.get(i);
        final String newChunkFilename = newName + "_chunk_" + chunkIndex + ".txt";
        final String containerIpAddress;
        final String newname = newName;

        // Get the IP address of the container for this chunk filename
        stmt = connection.prepareStatement("SELECT container_index FROM chunks WHERE chunk_filename = ?");
        stmt.setString(1, chunk);
        ResultSet rs5 = stmt.executeQuery();
        int containerIndex = rs5.getInt("container_index");
        switch (containerIndex) {
            case 1:
                containerIpAddress = "172.18.0.5";
                break;
            case 2:
                containerIpAddress = "172.18.0.6";
                break;
            case 3:
                containerIpAddress = "172.18.0.3";
                break;
            default:
                containerIpAddress = "172.18.0.4";
                break;
        }

        // Execute each chunk renaming operation in a separate thread
        executor.execute(() -> {
            try {
                Path file = Paths.get("./uploads/" + chunk);
                Path newFile = Paths.get("./uploads/" + newChunkFilename);
                Files.move(file, newFile, StandardCopyOption.REPLACE_EXISTING);

                String chunkFilePath = "./uploads/" + newChunkFilename;
                String containerFilePath = "/userFiles/" + newChunkFilename;

                // Copy the renamed chunk file to the container where it belongs
                ProcessBuilder pb = new ProcessBuilder("scp", "-P", "22", chunkFilePath, "root@" + containerIpAddress + ":" + containerFilePath);
                pb.inheritIO();
                Process p = pb.start();
                p.waitFor();

                // Remove the old chunk file from its original container
                ProcessBuilder pb2 = new ProcessBuilder("ssh", "-p", "22", "root@" + containerIpAddress, "rm", "/userFiles/" + chunk);
                Process p2 = pb2.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(p2.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                p2.waitFor();

                // Update the filename in the chunks table in the database
                PreparedStatement stmt2 = connection.prepareStatement("UPDATE chunks SET file_name = ?, chunk_filename = ? WHERE chunk_filename = ?");
                stmt2.setString(1, newname);
                stmt2.setString(2, newChunkFilename);
                stmt2.setString(3, chunk);
                stmt2.executeUpdate();
            } catch (IOException | InterruptedException | SQLException e) {
                // Handle exceptions
            }
        });
    }

    // Wait for all threads to complete before updating main file name in the database
    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.MINUTES);
    PreparedStatement stmt2 = connection.prepareStatement("UPDATE files SET file_name = ? WHERE file_name = ?");
    stmt2.setString(1, newName);
    stmt2.setString(2, oldName);
    stmt2.executeUpdate();
}

/**
 * @brief method to copy file from a source path to a destination path.
 * @param fileName name of the file that is to be copied
 * @param destinationPath path of the destination where file is to be copied
 * @param username name of the user trying to copy the file
 * @throws SQLException when there is an issue with SQL database
 * @throws IOException when there is an IO error
 */
public void copyFile(String fileName, String destinationPath, String username) throws SQLException, IOException {
    PreparedStatement stmt = null;
    try {

        //Check if user is logged in or not
        stmt = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
        stmt.setString(1, username);

        ResultSet rs = stmt.executeQuery();

        if (rs.getInt("isLoggedIn") == 0) {
            throw new IllegalArgumentException("User is not loggedin. Cannot copy the file.");
        }

        //Make a copy of the file and rename it for duplication check
        String des_fileName = fileName + "-copy";

        if (!fileName.contains(".txt")) {
            fileName = fileName + ".txt";
        }

        if (!des_fileName.contains(".txt")) {
            des_fileName = des_fileName + ".txt";
        }

        //Get id of the user
        int userId = rs.getInt("id");

        //Check if user has permission to copy the file
        stmt = connection.prepareStatement("SELECT * FROM files WHERE file_name = ? AND user_id = ?");
        stmt.setString(1, fileName);
        stmt.setInt(2, userId);

        ResultSet rs2 = stmt.executeQuery();

        if (!rs2.next()) {
            throw new IllegalArgumentException("User does not have permission to copy the file/Incorrect file name.");
        }

        stmt = connection.prepareStatement("SELECT file_path, file_size FROM files WHERE file_name = ?");
        stmt.setString(1, fileName);
        ResultSet rs3 = stmt.executeQuery();

        if (!rs3.next()) {
            throw new IllegalArgumentException("The file does not exist.");
        }

        String filePath1 = "/home/ntu-user/userFiles/Downloads/" + fileName;

        //Get the size of the file
        int fileSize = rs3.getInt("file_size");

        //Create the full destination path for the copied file
        String destinationPath2 = destinationPath + des_fileName;

        try {
            //Copy the file from source to the destination path
            File sourceFile = new File(filePath1);
            File destinationFile = new File(destinationPath2);
            Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            //Read contents of the file and write it to the new copied file
            String fileContent = Files.readString(sourceFile.toPath());
            Files.writeString(destinationFile.toPath(), fileContent);
        } catch (IOException e) {
            e.printStackTrace();
        }

        stmt = connection.prepareStatement("INSERT INTO files (file_name, file_path, file_size, user_id) VALUES (?, ?, ?, ?)");
        stmt.setString(1, des_fileName);
        stmt.setString(2, destinationPath);
        stmt.setInt(3, fileSize);
        stmt.setInt(4, userId);

        stmt.executeUpdate();

        //Split the contents of the file in chunks and create individual threads to upload each chunk to a separate container
        Path filePath = Paths.get("/home/ntu-user/userFiles/Downloads/" + fileName);
        String content = Files.readString(filePath);

        List<String> chunks = splitIntoChunks(content);
        List<String> chunkFilenames = new ArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(4);
        for (int i = 0; i < chunks.size(); i++) {
            final int chunkIndex = i;
            final String chunk = des_fileName + "_chunk_" + chunkIndex + ".txt";
            chunkFilenames.add(chunk);
            final String fileName1 = des_fileName;

            executor.execute(() -> {
                try {
                    //Write the chunk to a file in upload folder
                    Path chunkPath = Paths.get("./uploads/" + chunk);
                    Files.writeString(chunkPath, chunks.get(chunkIndex));

                    final String containerIpAddress;

                    switch (chunkIndex) {
                        case 1:
                            containerIpAddress = "172.17.0.4";
                            break;
                        case 2:
                            containerIpAddress = "172.17.0.3";
                            break;
                        case 3:
                            containerIpAddress = "172.17.0.2";
                            break;
                        default:
                            containerIpAddress = "172.17.0.1";
                            break;
                    }

                    String chunkFilePath = "./uploads/" + chunk;
                    String containerFilePath = "/userFiles";

                    //Upload the file to a container using scp command
                    ProcessBuilder pb = new ProcessBuilder("scp", "-P", "22", chunkFilePath, "root@" + containerIpAddress + ":" + containerFilePath);
                    pb.inheritIO();
                    Process p = pb.start();
                    p.waitFor();

                    //Insert the details of the chunk into the database
                    try (PreparedStatement stmt2 = connection.prepareStatement("INSERT INTO chunks (file_name, chunk_filename, user_Id, container_index) VALUES (?, ?, ?, ?)")) {
                        stmt2.setString(1, fileName1);
                        stmt2.setString(2, chunkFilenames.get(chunkIndex));
                        stmt2.setInt(3, userId);
                        stmt2.setInt(4, chunkIndex);
                        stmt2.executeUpdate();
                    }
                } catch (IOException | InterruptedException | SQLException e) {
                    System.out.println("Exception in thread \"" + Thread.currentThread().getName() + "\" " + chunk + ": " + e.getMessage());
                }
            });
        }
        executor.shutdown();
        try {
            //Wait for all the threads to complete execution
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
        }

    } catch (SQLException e) {
        e.printStackTrace();
    } finally {
        if (stmt != null) {
            stmt.close();
        }
    }
}

/**
 * @brief method to move a file from a source path to a destination path.
 * @param fileName name of the file that is to be moved
 * @param destinationPath path of the destination where file is to be moved
 * @param username name of the user trying to move the file
 * @throws SQLException when there is an issue with SQL database
 * @throws IOException when there is an IO error
 */
public void fileMove(String fileName, String destinationPath, String username) throws SQLException, IOException {
    PreparedStatement stmt = null;
    try {

        //Check if user is logged in or not
        stmt = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
        stmt.setString(1, username);

        ResultSet rs = stmt.executeQuery();

        if (rs.getInt("isLoggedIn") == 0) {
            throw new IllegalArgumentException("User is not loggedin. Cannot move the file.");
        }

        //Make a copy of the file and rename it for duplication check
        String des_fileName = fileName + "-copy";

        if (!fileName.contains(".txt")) {
            fileName = fileName + ".txt";
        }

        if (!des_fileName.contains(".txt")) {
            des_fileName = des_fileName + ".txt";
        }

        //Get id of the user
        int userId = rs.getInt("id");

        //Check if user has permission to move the file
        stmt = connection.prepareStatement("SELECT * FROM files WHERE file_name = ? AND user_id = ?");
        stmt.setString(1, fileName);
        stmt.setInt(2, userId);

        ResultSet rs2 = stmt.executeQuery();

        if (!rs2.next()) {
            throw new IllegalArgumentException("User does not have permission to move the file/Incorrect file name.");
        }

        stmt = connection.prepareStatement("SELECT file_path, file_size FROM files WHERE file_name = ?");
        stmt.setString(1, fileName);
        ResultSet rs3 = stmt.executeQuery();

        if (!rs3.next()) {
            throw new IllegalArgumentException("The file does not exist.");
        }

        String filePath1 = "/home/ntu-user/userFiles/Downloads/" + fileName;

        //Create the full destination path for the copied file
        String destinationPath2 = destinationPath + des_fileName;

        try {
            //Move the file from source to the destination path
            File sourceFile = new File(filePath1);
            File destinationFile = new File(destinationPath2);
            Files.move(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            e.printStackTrace();
        }

        //Update the database records with the new location of the file
        stmt = connection.prepareStatement("UPDATE files SET file_path = ? WHERE file_name = ?");
        stmt.setString(1, destinationPath);
        stmt.setString(2, fileName);

        stmt.executeUpdate();

    } catch (SQLException e) {
        e.printStackTrace();
    } finally {
        if (stmt != null) {
            stmt.close();
        }
    }
}
}
