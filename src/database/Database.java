package database;

import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import entityClasses.User;

/*******
 * <p> Title: Database Class. </p>
 * 
 * <p> Description: This is an in-memory database built on H2.  Detailed documentation of H2 can
 * be found at https://www.h2database.com/html/main.html (Click on "PDF (2MP) for a PDF of 438 pages
 * on the H2 main page.)  This class leverages H2 and provides numerous special supporting methods.
 * </p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 2.00		2025-04-29 Updated and expanded from the version produce by on a previous
 * 							version by Pravalika Mukkiri and Ishwarya Hidkimath Basavaraj
 * @version 2.01		2025-12-17 Minor updates for Spring 2026
 */

/*
 * The Database class is responsible for establishing and managing the connection to the database,
 * and performing operations such as user registration, login validation, handling invitation 
 * codes, and numerous other database related functions.
 */
public class Database {

	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

	//  Shared variables used within this class
	private Connection connection = null;		// Singleton to access the database 
	private Statement statement = null;			// The H2 Statement is used to construct queries
	
	// These are the easily accessible attributes of the currently logged-in user
	// This is only useful for single user applications
	private String currentUsername;
	private String currentPassword;
	private String currentFirstName;
	private String currentMiddleName;
	private String currentLastName;
	private String currentPreferredFirstName;
	private String currentEmailAddress;
	private boolean currentAdminRole;
	private boolean currentNewRole1;
	private boolean currentNewRole2;

	/*******
	 * <p> Method: Database </p>
	 * 
	 * <p> Description: The default constructor used to establish this singleton object.</p>
	 * 
	 */
	
	public Database () {
		
	}
	
	
/*******
 * <p> Method: connectToDatabase </p>
 * 
 * <p> Description: Used to establish the in-memory instance of the H2 database from secondary
 *		storage.</p>
 *
 * @throws SQLException when the DriverManager is unable to establish a connection
 * 
 */
	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement(); 
			// You can use this command to clear the database and restart from fresh.
			//statement.execute("DROP ALL OBJECTS");

			createTables();  // Create the necessary tables if they don't exist
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}

	
/*******
 * <p> Method: createTables </p>
 * 
 * <p> Description: Used to create new instances of the two database tables used by this class.</p>
 * 
 */
	private void createTables() throws SQLException {
		// Create the user database
		String userTable = "CREATE TABLE IF NOT EXISTS userDB ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "userName VARCHAR(255) UNIQUE, "
				+ "password VARCHAR(255), "
				+ "firstName VARCHAR(255), "
				+ "middleName VARCHAR(255), "
				+ "lastName VARCHAR (255), "
				+ "preferredFirstName VARCHAR(255), "
				+ "emailAddress VARCHAR(255), "
				+ "adminRole BOOL DEFAULT FALSE, "
				+ "newRole1 BOOL DEFAULT FALSE, "
				+ "newRole2 BOOL DEFAULT FALSE, "
				+ "otpExpiry TIMESTAMP DEFAULT NULL)"; //Added OTP Expiry column
		statement.execute(userTable);
		
		try {
			//Ensure the new column is added even if the table was created previously
			statement.execute("ALTER TABLE userDB ADD COLUMN IF NOT EXISTS otpExpiry TIMESTAMP DEFAULT NULL");
		} catch (SQLException e) { }
		
		// Create the invitation codes table
	    String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
	            + "code VARCHAR(10) PRIMARY KEY, "
	    		+ "emailAddress VARCHAR(255), "
	            + "role VARCHAR(10))";
	    statement.execute(invitationCodesTable);
	    
	 // Create the Discussion Threads table
	 		String threadsTable = "CREATE TABLE IF NOT EXISTS DiscussionThreads ("
	 				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
	 				+ "title VARCHAR(255), "
	 				+ "topic VARCHAR(1000), "
	 				+ "createdBy VARCHAR(255))";
	 		statement.execute(threadsTable);
	 		
	 	// Create the Replies table for nested discussions
			String repliesTable = "CREATE TABLE IF NOT EXISTS Replies ("
					+ "id INT AUTO_INCREMENT PRIMARY KEY, "
					+ "threadId INT, "
					+ "parentReplyId INT, " // 0 if replying directly to the thread
					+ "content VARCHAR(2000), "
					+ "createdBy VARCHAR(255))";
			statement.execute(repliesTable);
			
			// Create the Direct Messages table
			String dmTable = "CREATE TABLE IF NOT EXISTS DirectMessages ("
					+ "id INT AUTO_INCREMENT PRIMARY KEY, "
					+ "sender VARCHAR(255), "
					+ "receiver VARCHAR(255), "
					+ "content VARCHAR(2000))";
			statement.execute(dmTable);
			
			// Questions Table
			String qTable = "CREATE TABLE IF NOT EXISTS Questions ("
					+ "id INT AUTO_INCREMENT PRIMARY KEY, "
					+ "title VARCHAR(255), "
					+ "topic VARCHAR(1000), "
					+ "createdBy VARCHAR(255))";
			statement.execute(qTable);

			// Question Replies Table
			String qrTable = "CREATE TABLE IF NOT EXISTS QuestionReplies ("
					+ "id INT AUTO_INCREMENT PRIMARY KEY, "
					+ "questionId INT, "
					+ "parentReplyId INT, "
					+ "content VARCHAR(2000), "
					+ "createdBy VARCHAR(255))";
			statement.execute(qrTable);

			// Read Receipts Table (Tracks if a user has seen a post)
			String rpTable = "CREATE TABLE IF NOT EXISTS ReadPosts ("
					+ "username VARCHAR(255), "
					+ "postId INT, "
					+ "postType VARCHAR(50), "
					+ "PRIMARY KEY(username, postId, postType))";
			statement.execute(rpTable);
		
			// Admin Requests Tables
			String adminReqTable = "CREATE TABLE IF NOT EXISTS AdminRequests ("
					+ "id INT AUTO_INCREMENT PRIMARY KEY, "
					+ "username VARCHAR(255), "
					+ "message VARCHAR(2000), "
					+ "status VARCHAR(50), "
					+ "admin_notes VARCHAR(2000), "
					+ "was_denied INT DEFAULT 0)";
			statement.execute(adminReqTable);

			// Temporary Admins Table
			String tempAdminsTable = "CREATE TABLE IF NOT EXISTS TempAdmins ("
					+ "username VARCHAR(255) PRIMARY KEY, "
					+ "expiry_time BIGINT)";
			statement.execute(tempAdminsTable);
			
	}


/*******
 * <p> Method: isDatabaseEmpty </p>
 * 
 * <p> Description: If the user database has no rows, true is returned, else false.</p>
 * 
 * @return true if the database is empty, else it returns false
 * 
 */
	public boolean isDatabaseEmpty() {
		String query = "SELECT COUNT(*) AS count FROM userDB";
		try {
			ResultSet resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				return resultSet.getInt("count") == 0;
			}
		}  catch (SQLException e) {
	        return false;
	    }
		return true;
	}
	
	
/*******
 * <p> Method: getNumberOfUsers </p>
 * 
 * <p> Description: Returns an integer .of the number of users currently in the user database. </p>
 * 
 * @return the number of user records in the database.
 * 
 */
	public int getNumberOfUsers() {
		String query = "SELECT COUNT(*) AS count FROM userDB";
		try {
			ResultSet resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				return resultSet.getInt("count");
			}
		} catch (SQLException e) {
	        return 0;
	    }
		return 0;
	}

/*******
 * <p> Method: register(User user) </p>
 * 
 * <p> Description: Creates a new row in the database using the user parameter. </p>
 * 
 * @throws SQLException when there is an issue creating the SQL command or executing it.
 * 
 * @param user specifies a user object to be added to the database.
 * 
 */
	public void register(User user) throws SQLException {
		String insertUser = "INSERT INTO userDB (userName, password, firstName, middleName, "
				+ "lastName, preferredFirstName, emailAddress, adminRole, newRole1, newRole2) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
			currentUsername = user.getUserName();
			pstmt.setString(1, currentUsername);
			
			currentPassword = user.getPassword();
			pstmt.setString(2, currentPassword);
			
			currentFirstName = user.getFirstName();
			pstmt.setString(3, currentFirstName);
			
			currentMiddleName = user.getMiddleName();			
			pstmt.setString(4, currentMiddleName);
			
			currentLastName = user.getLastName();
			pstmt.setString(5, currentLastName);
			
			currentPreferredFirstName = user.getPreferredFirstName();
			pstmt.setString(6, currentPreferredFirstName);
			
			currentEmailAddress = user.getEmailAddress();
			pstmt.setString(7, currentEmailAddress);
			
			currentAdminRole = user.getAdminRole();
			pstmt.setBoolean(8, currentAdminRole);
			
			currentNewRole1 = user.getNewRole1();
			pstmt.setBoolean(9, currentNewRole1);
			
			currentNewRole2 = user.getNewRole2();
			pstmt.setBoolean(10, currentNewRole2);
			
			pstmt.executeUpdate();
		}
		
	}
	
/*******
 *  <p> Method: List getUserList() </p>
 *  
 *  <P> Description: Generate an List of Strings, one for each user in the database,
 *  starting with "<Select User>" at the start of the list. </p>
 *  
 *  @return a list of userNames found in the database.
 */
	public List<String> getUserList () {
		List<String> userList = new ArrayList<String>();
		userList.add("<Select a User>");
		String query = "SELECT userName FROM userDB";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				userList.add(rs.getString("userName"));
			}
		} catch (SQLException e) {
	        return null;
	    }
//		System.out.println(userList);
		return userList;
	}

/*******
 * <p> Method: boolean loginAdmin(User user) </p>
 * 
 * <p> Description: Check to see that a user with the specified username, password, and role
 * 		is the same as a row in the table for the username, password, and role. </p>
 * 
 * @param user specifies the specific user that should be logged in playing the Admin role.
 * 
 * @return true if the specified user has been logged in as an Admin else false.
 * 
 */
	public boolean loginAdmin(User user){
		// Validates an admin user's login credentials. Expiry check removed from SQL to fix timezone bugs.
		String query = "SELECT * FROM userDB WHERE UPPER(userName) = UPPER(?) AND password = ? AND adminRole = TRUE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName().trim());
			pstmt.setString(2, user.getPassword().trim());
			ResultSet rs = pstmt.executeQuery();
			return rs.next();
		} catch (SQLException e) {
	        e.printStackTrace();
	    }
		return false;
	}
	
	
/*******
 * <p> Method: boolean loginRole1(User user) </p>
 * 
 * <p> Description: Check to see that a user with the specified username, password, and role
 * 		is the same as a row in the table for the username, password, and role. </p>
 * 
 * @param user specifies the specific user that should be logged in playing the Student role.
 * 
 * @return true if the specified user has been logged in as an Student else false.
 * 
 */
	public boolean loginRole1(User user) {
		// Validates a student user's login credentials. Expiry check removed from SQL to fix timezone bugs.
		String query = "SELECT * FROM userDB WHERE UPPER(userName) = UPPER(?) AND password = ? AND newRole1 = TRUE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName().trim());
			pstmt.setString(2, user.getPassword().trim());
			ResultSet rs = pstmt.executeQuery();
			return rs.next();
		} catch (SQLException e) {
		       e.printStackTrace();
		}
		return false;
	}

	/*******
	 * <p> Method: boolean loginRole2(User user) </p>
	 * 
	 * <p> Description: Check to see that a user with the specified username, password, and role
	 * 		is the same as a row in the table for the username, password, and role. </p>
	 * 
	 * @param user specifies the specific user that should be logged in playing the Reviewer role.
	 * 
	 * @return true if the specified user has been logged in as an Student else false.
	 * 
	 */
	public boolean loginRole2(User user) {
		// Validates a reviewer user's login credentials. Expiry check removed from SQL to fix timezone bugs.
		String query = "SELECT * FROM userDB WHERE UPPER(userName) = UPPER(?) AND password = ? AND newRole2 = TRUE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName().trim());
			pstmt.setString(2, user.getPassword().trim());
			ResultSet rs = pstmt.executeQuery();
			return rs.next();
		} catch (SQLException e) {
		       e.printStackTrace();
		}
		return false;
	}
	
	
	/*******
	 * <p> Method: boolean doesUserExist(User user) </p>
	 * 
	 * <p> Description: Check to see that a user with the specified username is  in the table. </p>
	 * 
	 * @param userName specifies the specific user that we want to determine if it is in the table.
	 * 
	 * @return true if the specified user is in the table else false.
	 * 
	 */
	// Checks if a user already exists in the database based on their userName.
	public boolean doesUserExist(String userName) {
	    String query = "SELECT COUNT(*) FROM userDB WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            // If the count is greater than 0, the user exists
	            return rs.getInt(1) > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false; // If an error occurs, assume user doesn't exist
	}

	
	/*******
	 * <p> Method: int getNumberOfRoles(User user) </p>
	 * 
	 * <p> Description: Determine the number of roles a specified user plays. </p>
	 * 
	 * @param user specifies the specific user that we want to determine if it is in the table.
	 * 
	 * @return the number of roles this user plays (0 - 5).
	 * 
	 */	
	// Get the number of roles that this user plays
	public int getNumberOfRoles (User user) {
		int numberOfRoles = 0;
		if (user.getAdminRole()) numberOfRoles++;
		if (user.getNewRole1()) numberOfRoles++;
		if (user.getNewRole2()) numberOfRoles++;
		return numberOfRoles;
	}	

	
	/*******
	 * <p> Method: String generateInvitationCode(String emailAddress, String role) </p>
	 * 
	 * <p> Description: Given an email address and a roles, this method establishes and invitation
	 * code and adds a record to the InvitationCodes table.  When the invitation code is used, the
	 * stored email address is used to establish the new user and the record is removed from the
	 * table.</p>
	 * 
	 * @param emailAddress specifies the email address for this new user.
	 * 
	 * @param role specified the role that this new user will play.
	 * 
	 * @return the code of six characters so the new user can use it to securely setup an account.
	 * 
	 */
	// Generates a new invitation code and inserts it into the database.
	public String generateInvitationCode(String emailAddress, String role) {
	    String code = UUID.randomUUID().toString().substring(0, 6); // Generate a random 6-character code
	    String query = "INSERT INTO InvitationCodes (code, emailaddress, role) VALUES (?, ?, ?)";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.setString(2, emailAddress);
	        pstmt.setString(3, role);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return code;
	}

	
	/*******
	 * <p> Method: int getNumberOfInvitations() </p>
	 * 
	 * <p> Description: Determine the number of outstanding invitations in the table.</p>
	 *  
	 * @return the number of invitations in the table.
	 * 
	 */
	// Number of invitations in the database
	public int getNumberOfInvitations() {
		String query = "SELECT COUNT(*) AS count FROM InvitationCodes";
		try {
			ResultSet resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				return resultSet.getInt("count");
			}
		} catch  (SQLException e) {
	        e.printStackTrace();
	    }
		return 0;
	}
	
	
	/*******
	 * <p> Method: boolean emailaddressHasBeenUsed(String emailAddress) </p>
	 * 
	 * <p> Description: Determine if an email address has been user to establish a user.</p>
	 * 
	 * @param emailAddress is a string that identifies a user in the table
	 *  
	 * @return true if the email address is in the table, else return false.
	 * 
	 */
	// Check to see if an email address is already in the database
	public boolean emailaddressHasBeenUsed(String emailAddress) {
	    String query = "SELECT COUNT(*) AS count FROM InvitationCodes WHERE emailAddress = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, emailAddress);
	        ResultSet rs = pstmt.executeQuery();
	 //     System.out.println(rs);
	        if (rs.next()) {
	            // Mark the code as used
	        	return rs.getInt("count")>0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return false;
	}
	
	
	/*******
	 * <p> Method: String getRoleGivenAnInvitationCode(String code) </p>
	 * 
	 * <p> Description: Get the role associated with an invitation code.</p>
	 * 
	 * @param code is the 6 character String invitation code
	 *  
	 * @return the role for the code or an empty string.
	 * 
	 */
	// Obtain the roles associated with an invitation code.
	public String getRoleGivenAnInvitationCode(String code) {
	    String query = "SELECT * FROM InvitationCodes WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("role");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return "";
	}

	
	/*******
	 * <p> Method: String getEmailAddressUsingCode (String code ) </p>
	 * 
	 * <p> Description: Get the email addressed associated with an invitation code.</p>
	 * 
	 * @param code is the 6 character String invitation code
	 *  
	 * @return the email address for the code or an empty string.
	 * 
	 */
	// For a given invitation code, return the associated email address of an empty string
	public String getEmailAddressUsingCode (String code ) {
	    String query = "SELECT emailAddress FROM InvitationCodes WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("emailAddress");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return "";
	}
	
	
	/*******
	 * <p> Method: void removeInvitationAfterUse(String code) </p>
	 * 
	 * <p> Description: Remove an invitation record once it is used.</p>
	 * 
	 * @param code is the 6 character String invitation code
	 *  
	 */
	// Remove an invitation using an email address once the user account has been setup
	public void removeInvitationAfterUse(String code) {
	    String query = "SELECT COUNT(*) AS count FROM InvitationCodes WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	        	int counter = rs.getInt(1);
	            // Only do the remove if the code is still in the invitation table
	        	if (counter > 0) {
        			query = "DELETE FROM InvitationCodes WHERE code = ?";
	        		try (PreparedStatement pstmt2 = connection.prepareStatement(query)) {
	        			pstmt2.setString(1, code);
	        			pstmt2.executeUpdate();
	        		}catch (SQLException e) {
	        	        e.printStackTrace();
	        	    }
	        	}
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return;
	}
	
	
	/*******
	 * <p> Method: String getFirstName(String username) </p>
	 * 
	 * <p> Description: Get the first name of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the first name of a user given that user's username 
	 *  
	 */
	// Get the First Name
	public String getFirstName(String username) {
		String query = "SELECT firstName FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("firstName"); // Return the first name if user exists
	        }
			
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}
	

	/*******
	 * <p> Method: void updateFirstName(String username, String firstName) </p>
	 * 
	 * <p> Description: Update the first name of a user given that user's username and the new
	 *		first name.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @param firstName is the new first name for the user
	 *  
	 */
	// update the first name
	public void updateFirstName(String username, String firstName) {
	    String query = "UPDATE userDB SET firstName = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, firstName);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentFirstName = firstName;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	
	/*******
	 * <p> Method: String getMiddleName(String username) </p>
	 * 
	 * <p> Description: Get the middle name of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the middle name of a user given that user's username 
	 *  
	 */
	// get the middle name
	public String getMiddleName(String username) {
		String query = "SELECT MiddleName FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("middleName"); // Return the middle name if user exists
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}

	
	/*******
	 * <p> Method: void updateMiddleName(String username, String middleName) </p>
	 * 
	 * <p> Description: Update the middle name of a user given that user's username and the new
	 * 		middle name.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param middleName is the new middle name for the user
	 *  
	 */
	// update the middle name
	public void updateMiddleName(String username, String middleName) {
	    String query = "UPDATE userDB SET middleName = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, middleName);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentMiddleName = middleName;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	/*******
	 * <p> Method: String getLastName(String username) </p>
	 * 
	 * <p> Description: Get the last name of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the last name of a user given that user's username 
	 *  
	 */
	// get he last name
	public String getLastName(String username) {
		String query = "SELECT LastName FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("lastName"); // Return last name role if user exists
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}
	
	
	/*******
	 * <p> Method: void updateLastName(String username, String lastName) </p>
	 * 
	 * <p> Description: Update the middle name of a user given that user's username and the new
	 * 		middle name.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param lastName is the new last name for the user
	 *  
	 */
	// update the last name
	public void updateLastName(String username, String lastName) {
	    String query = "UPDATE userDB SET lastName = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, lastName);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentLastName = lastName;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	/*******
	 * <p> Method: String getPreferredFirstName(String username) </p>
	 * 
	 * <p> Description: Get the preferred first name of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the preferred first name of a user given that user's username 
	 *  
	 */
	// get the preferred first name
	public String getPreferredFirstName(String username) {
		String query = "SELECT preferredFirstName FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("firstName"); // Return the preferred first name if user exists
	        }
			
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}
	
	
	/*******
	 * <p> Method: void updatePreferredFirstName(String username, String preferredFirstName) </p>
	 * 
	 * <p> Description: Update the preferred first name of a user given that user's username and
	 * 		the new preferred first name.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param preferredFirstName is the new preferred first name for the user
	 *  
	 */
	// update the preferred first name of the user
	public void updatePreferredFirstName(String username, String preferredFirstName) {
	    String query = "UPDATE userDB SET preferredFirstName = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, preferredFirstName);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentPreferredFirstName = preferredFirstName;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	/*******
	 * <p> Method: String getEmailAddress(String username) </p>
	 * 
	 * <p> Description: Get the email address of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the email address of a user given that user's username 
	 *  
	 */
	// get the email address
	public String getEmailAddress(String username) {
		String query = "SELECT emailAddress FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("emailAddress"); // Return the email address if user exists
	        }
			
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}
	
	
	/*******
	 * <p> Method: void updateEmailAddress(String username, String emailAddress) </p>
	 * 
	 * <p> Description: Update the email address name of a user given that user's username and
	 * 		the new email address.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param emailAddress is the new preferred first name for the user
	 *  
	 */
	// update the email address
	public void updateEmailAddress(String username, String emailAddress) {
	    String query = "UPDATE userDB SET emailAddress = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, emailAddress);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentEmailAddress = emailAddress;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	/*******
	 * <p> Method: boolean getUserAccountDetails(String username) </p>
	 * 
	 * <p> Description: Get all the attributes of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return true of the get is successful, else false
	 *  
	 */
	// get the attributes for a specified user
		public boolean getUserAccountDetails(String username) {
			String query = "SELECT * FROM userDB WHERE UPPER(username) = UPPER(?)";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setString(1, username.trim());
		        ResultSet rs = pstmt.executeQuery();			
				if (!rs.next()) return false; // FIX: Prevent crash if user doesn't exist
		    	currentUsername = rs.getString(2);
		    	currentPassword = rs.getString(3);
		    	currentFirstName = rs.getString(4);
		    	currentMiddleName = rs.getString(5);
		    	currentLastName = rs.getString(6);
		    	currentPreferredFirstName = rs.getString(7);
		    	currentEmailAddress = rs.getString(8);
		    	currentAdminRole = rs.getBoolean(9);
		    	currentNewRole1 = rs.getBoolean(10);
		    	currentNewRole2 = rs.getBoolean(11);
				return true;
		    } catch (SQLException e) {
				return false;
		    }
		}
	
	
	/*******
	 * <p> Method: boolean updateUserRole(String username, String role, String value) </p>
	 * 
	 * <p> Description: Update a specified role for a specified user's and set and update all the
	 * 		current user attributes.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param role is string that specifies the role to update
	 * 
	 * @param value is the string that specified TRUE or FALSE for the role
	 * 
	 * @return true if the update was successful, else false
	 *  
	 */
	// Update a users role
		// Update a users role
		public boolean updateUserRole(String username, String role, String value) {
			if (role.equalsIgnoreCase("Admin")) {
				String query = "UPDATE userDB SET adminRole = ? WHERE username = ?";
				try (PreparedStatement pstmt = connection.prepareStatement(query)) {
					pstmt.setString(1, value);
					pstmt.setString(2, username);
					pstmt.executeUpdate();
					currentAdminRole = value.equalsIgnoreCase("true");
					return true;
				} catch (SQLException e) { return false; }
			}
			// CHANGED: Check for "Staff" instead of "Role1"
			if (role.equalsIgnoreCase("Staff")) {
				String query = "UPDATE userDB SET newRole1 = ? WHERE username = ?";
				try (PreparedStatement pstmt = connection.prepareStatement(query)) {
					pstmt.setString(1, value);
					pstmt.setString(2, username);
					pstmt.executeUpdate();
					currentNewRole1 = value.equalsIgnoreCase("true");
					return true;
				} catch (SQLException e) { return false; }
			}
			// CHANGED: Check for "Student" instead of "Role2"
			if (role.equalsIgnoreCase("Student")) {
				String query = "UPDATE userDB SET newRole2 = ? WHERE username = ?";
				try (PreparedStatement pstmt = connection.prepareStatement(query)) {
					pstmt.setString(1, value);
					pstmt.setString(2, username);
					pstmt.executeUpdate();
					currentNewRole2 = value.equalsIgnoreCase("true");
					return true;
				} catch (SQLException e) { return false; }
			}
			return false;
		}
	
	
	
	
	// Attribute getters for the current user
	/*******
	 * <p> Method: String getCurrentUsername() </p>
	 * 
	 * <p> Description: Get the current user's username.</p>
	 * 
	 * @return the username value is returned
	 *  
	 */
	public String getCurrentUsername() { return currentUsername;};

	
	/*******
	 * <p> Method: String getCurrentPassword() </p>
	 * 
	 * <p> Description: Get the current user's password.</p>
	 * 
	 * @return the password value is returned
	 *  
	 */
	public String getCurrentPassword() { return currentPassword;};

	
	/*******
	 * <p> Method: String getCurrentFirstName() </p>
	 * 
	 * <p> Description: Get the current user's first name.</p>
	 * 
	 * @return the first name value is returned
	 *  
	 */
	public String getCurrentFirstName() { return currentFirstName;};

	
	/*******
	 * <p> Method: String getCurrentMiddleName() </p>
	 * 
	 * <p> Description: Get the current user's middle name.</p>
	 * 
	 * @return the middle name value is returned
	 *  
	 */
	public String getCurrentMiddleName() { return currentMiddleName;};

	
	/*******
	 * <p> Method: String getCurrentLastName() </p>
	 * 
	 * <p> Description: Get the current user's last name.</p>
	 * 
	 * @return the last name value is returned
	 *  
	 */
	public String getCurrentLastName() { return currentLastName;};

	
	/*******
	 * <p> Method: String getCurrentPreferredFirstName( </p>
	 * 
	 * <p> Description: Get the current user's preferred first name.</p>
	 * 
	 * @return the preferred first name value is returned
	 *  
	 */
	public String getCurrentPreferredFirstName() { return currentPreferredFirstName;};

	
	/*******
	 * <p> Method: String getCurrentEmailAddress() </p>
	 * 
	 * <p> Description: Get the current user's email address name.</p>
	 * 
	 * @return the email address value is returned
	 *  
	 */
	public String getCurrentEmailAddress() { return currentEmailAddress;};

	
	/*******
	 * <p> Method: boolean getCurrentAdminRole() </p>
	 * 
	 * <p> Description: Get the current user's Admin role attribute.</p>
	 * 
	 * @return true if this user plays an Admin role, else false
	 *  
	 */
	public boolean getCurrentAdminRole() { return currentAdminRole;};

	
	/*******
	 * <p> Method: boolean getCurrentNewRole1() </p>
	 * 
	 * <p> Description: Get the current user's Student role attribute.</p>
	 * 
	 * @return true if this user plays a Student role, else false
	 *  
	 */
	public boolean getCurrentNewRole1() { return currentNewRole1;};

	
	/*******
	 * <p> Method: boolean getCurrentNewRole2() </p>
	 * 
	 * <p> Description: Get the current user's Reviewer role attribute.</p>
	 * 
	 * @return true if this user plays a Reviewer role, else false
	 *  
	 */
	public boolean getCurrentNewRole2() { return currentNewRole2;};

	
	/*******
	 * <p> Debugging method</p>
	 * 
	 * <p> Description: Debugging method that dumps the database of the console.</p>
	 * 
	 * @throws SQLException if there is an issues accessing the database.
	 * 
	 */
	// Dumps the database.
	public void dump() throws SQLException {
		String query = "SELECT * FROM userDB";
		ResultSet resultSet = statement.executeQuery(query);
		ResultSetMetaData meta = resultSet.getMetaData();
		while (resultSet.next()) {
		for (int i = 0; i < meta.getColumnCount(); i++) {
		System.out.println(
		meta.getColumnLabel(i + 1) + ": " +
				resultSet.getString(i + 1));
		}
		System.out.println();
		}
		resultSet.close();
	}


	/*******
	 * <p> Method: void closeConnection()</p>
	 * 
	 * <p> Description: Closes the database statement and connection.</p>
	 * 
	 */
	// Closes the database statement and connection.
	public void closeConnection() {
		try{ 
			if(statement!=null) statement.close(); 
		} catch(SQLException se2) { 
			se2.printStackTrace();
		} 
		try { 
			if(connection!=null) connection.close(); 
		} catch(SQLException se){ 
			se.printStackTrace(); 
		} 
	}
	

	//Checks if a username exists in the database.
		public boolean usernameExists(String username) {
			try {
				String query = "SELECT COUNT(*) as count FROM userDB WHERE userName = ?";
				PreparedStatement pstmt = connection.prepareStatement(query);
				pstmt.setString(1, username);
				ResultSet rs = pstmt.executeQuery();
				if (rs.next()) {
					return rs.getInt("count") > 0;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return false;
		}


		/**
		 * Deletes a user from the database.
		 */
		public void deleteUser(String username) throws SQLException {
			String query = "DELETE FROM userDB WHERE userName = ?";
			PreparedStatement pstmt = connection.prepareStatement(query);
			pstmt.setString(1, username);
			pstmt.executeUpdate();
		}

		// Resets a user's password to a new One-Time Password (OTP).
				public void resetPassword(String username, String newPassword) throws SQLException {
					// Safely add a BIGINT column to bypass all timezone/timestamp bugs in H2 SQL
					try {
						connection.createStatement().execute("ALTER TABLE userDB ADD COLUMN IF NOT EXISTS otpExpiryMs BIGINT DEFAULT 0");
					} catch (Exception ignore) {}
					
					// UPPER() ensures case-insensitivity so "admin" and "Admin" don't mismatch
					String query = "UPDATE userDB SET password = ?, otpExpiryMs = ? WHERE UPPER(userName) = UPPER(?)";
					try (PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setString(1, newPassword.trim());
						// Store exactly 24 hours from right now in pure milliseconds
						pstmt.setLong(2, System.currentTimeMillis() + 86400000L);
						pstmt.setString(3, username.trim());
						
						int rowsAffected = pstmt.executeUpdate();
						if (rowsAffected == 0) {
							throw new SQLException("Update failed: Could not find user.");
						}
					}
				}

				public void addInvitation(String code) throws SQLException {
					String query = "INSERT INTO InvitationCodes (code) VALUES (?)";
					try (PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setString(1, code);
						pstmt.executeUpdate();
					}
				}

				public int getInvitationCount() {
					return getNumberOfInvitations();
				}

				public boolean validInvitation(String code) {
					String query = "SELECT COUNT(*) FROM InvitationCodes WHERE code = ?";
					try (PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setString(1, code);
						ResultSet rs = pstmt.executeQuery();
						if (rs.next()) {
							return rs.getInt(1) > 0;
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
					return false;
				}
				
				// Fetches the active invitations as a formatted list for the UI
				public java.util.List<String> getInvitationList() {
					java.util.List<String> list = new java.util.ArrayList<>();
					String query = "SELECT code, emailAddress, role FROM InvitationCodes"; 
					try (Statement stmt = connection.createStatement();
					     ResultSet rs = stmt.executeQuery(query)) {
						while (rs.next()) {
							String code = rs.getString("code");
							String email = rs.getString("emailAddress");
							String role = rs.getString("role");
							if (email == null || email.isEmpty()) email = "None";
							if (role == null || role.isEmpty()) role = "None";
							
							list.add(String.format("%-8s | %-25s | %s", code, email, role));
						}
					} catch (SQLException e) { e.printStackTrace(); }
					return list;
				}

				public void deleteInvitation(String code) throws SQLException {
					String query = "DELETE FROM InvitationCodes WHERE code = ?";
					try (PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setString(1, code);
						pstmt.executeUpdate();
					}
				}

				// Checks if an OTP has expired (Pure Java LONG math to completely avoid SQL timezone bugs)
				public boolean isAccountExpired(String username) {
					// Catch cases where the column might not exist yet
					try {
						connection.createStatement().execute("ALTER TABLE userDB ADD COLUMN IF NOT EXISTS otpExpiryMs BIGINT DEFAULT 0");
					} catch (Exception ignore) {}

					String query = "SELECT otpExpiryMs FROM userDB WHERE UPPER(userName) = UPPER(?)";
					try (PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setString(1, username.trim());
						ResultSet rs = pstmt.executeQuery();
						if (rs.next()) {
							long expiry = rs.getLong("otpExpiryMs");
							// If expiry is set (greater than 0) and the current time has passed it
							if (expiry > 0 && System.currentTimeMillis() > expiry) {
								return true;
							}
						}
					} catch (SQLException e) { e.printStackTrace(); }
					return false; // Not expired
				}

				// Burns the OTP immediately after successful login so it can NEVER be used again
				public void burnOTP(String username) {
					// Sets the expiration to 1 (which translates to the year 1970) to instantly expire it
					String query = "UPDATE userDB SET otpExpiryMs = 1 WHERE UPPER(userName) = UPPER(?) AND otpExpiryMs > 0";
					try (PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setString(1, username.trim());
						pstmt.executeUpdate();
					} catch (SQLException e) { 
						e.printStackTrace();
					}
				}
				
				// Checks if the user is currently logging in with an active OTP
				public boolean isUsingOTP(String username) {
					String query = "SELECT otpExpiryMs FROM userDB WHERE UPPER(userName) = UPPER(?)";
					try (PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setString(1, username.trim());
						ResultSet rs = pstmt.executeQuery();
						if (rs.next()) {
							long expiry = rs.getLong("otpExpiryMs");
							return (expiry > System.currentTimeMillis());
						}
					} catch (SQLException e) { e.printStackTrace(); }
					return false;
				}

				// Updates the user's password and clears the OTP flag so it becomes a standard password again
				public void updatePassword(String username, String newPassword) {
					String query = "UPDATE userDB SET password = ?, otpExpiryMs = 0 WHERE UPPER(userName) = UPPER(?)";
					try (PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setString(1, newPassword.trim());
						pstmt.setString(2, username.trim());
						pstmt.executeUpdate();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}

				// CREATE Thread
				public void createThread(String title, String topic, String createdBy) throws SQLException {
					String query = "INSERT INTO DiscussionThreads (title, topic, createdBy) VALUES (?, ?, ?)";
					try (PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setString(1, title);
						pstmt.setString(2, topic);
						pstmt.setString(3, createdBy);
						pstmt.executeUpdate();
					}
				}

				// READ Threads
				public java.util.List<String> getThreadList() {
					java.util.List<String> list = new java.util.ArrayList<>();
					String query = "SELECT id, title, topic, createdBy FROM DiscussionThreads";
					try (Statement stmt = connection.createStatement();
						 ResultSet rs = stmt.executeQuery(query)) {
						while (rs.next()) {
							// Format: "ID | Title | Topic | Creator"
							list.add(rs.getInt("id") + " | " + rs.getString("title") + " | " + rs.getString("topic") + " | " + rs.getString("createdBy"));
						}
					} catch (SQLException e) { e.printStackTrace(); }
					return list;
				}

				// UPDATE Thread
				public void updateThread(int id, String newTitle, String newTopic) throws SQLException {
					String query = "UPDATE DiscussionThreads SET title = ?, topic = ? WHERE id = ?";
					try (PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setString(1, newTitle);
						pstmt.setString(2, newTopic);
						pstmt.setInt(3, id);
						pstmt.executeUpdate();
					}
				}

				// DELETE Thread
				public void deleteThread(int id) throws SQLException {
					String query = "DELETE FROM DiscussionThreads WHERE id = ?";
					try (PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setInt(1, id);
						pstmt.executeUpdate();
					}
				}
				
				
				public void createReply(int threadId, int parentReplyId, String content, String createdBy) throws SQLException {
					String query = "INSERT INTO Replies (threadId, parentReplyId, content, createdBy) VALUES (?, ?, ?, ?)";
					try (PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setInt(1, threadId);
						pstmt.setInt(2, parentReplyId);
						pstmt.setString(3, content);
						pstmt.setString(4, createdBy);
						pstmt.executeUpdate();
					}
				}

				// Gets all replies for a specific thread
				public java.util.List<String> getRepliesForThread(int threadId) {
					java.util.List<String> list = new java.util.ArrayList<>();
					String query = "SELECT id, parentReplyId, content, createdBy FROM Replies WHERE threadId = ? ORDER BY id ASC";
					try (PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setInt(1, threadId);
						try (ResultSet rs = pstmt.executeQuery()) {
							while (rs.next()) {
								list.add(rs.getInt("id") + " | " + rs.getInt("parentReplyId") + " | " 
										+ rs.getString("content") + " | " + rs.getString("createdBy"));
							}
						}
					} catch (SQLException e) { e.printStackTrace(); }
					return list;
				}
				
				// Retrieves a single thread's full details by its ID
				public String getThread(int threadId) {
					String query = "SELECT id, title, topic, createdBy FROM DiscussionThreads WHERE id = ?";
					try (java.sql.PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setInt(1, threadId);
						try (java.sql.ResultSet rs = pstmt.executeQuery()) {
							if (rs.next()) {
								return rs.getInt("id") + " | " + rs.getString("title") + " | " 
										+ rs.getString("topic") + " | " + rs.getString("createdBy");
							}
						}
					} catch (java.sql.SQLException e) { e.printStackTrace(); }
					return null;
				}

				// Retrieves all staff members for the dropdown menu
				public java.util.List<String> getStaffUsers() {
					java.util.List<String> list = new java.util.ArrayList<>();
					//The database column is newRole1 
					String query = "SELECT userName FROM userDB WHERE newRole1 = TRUE";
					
					try (java.sql.Statement stmt = connection.createStatement();
						 java.sql.ResultSet rs = stmt.executeQuery(query)) {
						while (rs.next()) {
							list.add(rs.getString("userName"));
						}
					} catch (java.sql.SQLException e) { 
						e.printStackTrace(); 
					}
					return list;
				}

				public void createDirectMessage(String sender, String receiver, String content) throws java.sql.SQLException {
					String query = "INSERT INTO DirectMessages (sender, receiver, content) VALUES (?, ?, ?)";
					try (java.sql.PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setString(1, sender);
						pstmt.setString(2, receiver);
						pstmt.setString(3, content);
						pstmt.executeUpdate();
					}
				}

				public java.util.List<String> getDirectMessages(String user1, String user2) {
					java.util.List<String> list = new java.util.ArrayList<>();
					String query = "SELECT sender, content FROM DirectMessages WHERE (sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?) ORDER BY id ASC";
					try (java.sql.PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setString(1, user1);
						pstmt.setString(2, user2);
						pstmt.setString(3, user2);
						pstmt.setString(4, user1);
						try (java.sql.ResultSet rs = pstmt.executeQuery()) {
							while (rs.next()) {
								list.add(rs.getString("sender") + " | " + rs.getString("content"));
							}
						}
					} catch (java.sql.SQLException e) { e.printStackTrace(); }
					return list;
				}
				
				// Clears all existing direct messages from the database
				public void clearAllMessages() {
					try (java.sql.Statement stmt = connection.createStatement()) {
						stmt.execute("DELETE FROM DirectMessages");
						System.out.println("All messages have been cleared.");
					} catch (java.sql.SQLException e) { e.printStackTrace(); }
				}

				// Retrieves all student members so Staff can message them
				public java.util.List<String> getStudentUsers() {
					java.util.List<String> list = new java.util.ArrayList<>();
					// newRole2 is the database column for Students
					String query = "SELECT userName FROM userDB WHERE newRole2 = TRUE";
					try (java.sql.Statement stmt = connection.createStatement();
						 java.sql.ResultSet rs = stmt.executeQuery(query)) {
						while (rs.next()) {
							list.add(rs.getString("userName"));
						}
					} catch (java.sql.SQLException e) { e.printStackTrace(); }
					return list;
				}
				
				// --- QUESTIONS OPERATIONS ---
				public void createQuestion(String title, String topic, String createdBy) {
					String query = "INSERT INTO Questions (title, topic, createdBy) VALUES (?, ?, ?)";
					try (PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setString(1, title); pstmt.setString(2, topic); pstmt.setString(3, createdBy);
						pstmt.executeUpdate();
					} catch (SQLException e) { e.printStackTrace(); }
				}

				public java.util.List<String> getQuestionList() {
					java.util.List<String> list = new java.util.ArrayList<>();
					String query = "SELECT id, title, topic, createdBy FROM Questions";
					try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
						while (rs.next()) list.add(rs.getInt("id") + " | " + rs.getString("title") + " | " + rs.getString("topic") + " | " + rs.getString("createdBy"));
					} catch (SQLException e) { e.printStackTrace(); }
					return list;
				}

				public String getQuestion(int id) {
					String query = "SELECT id, title, topic, createdBy FROM Questions WHERE id = ?";
					try (PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setInt(1, id);
						try (ResultSet rs = pstmt.executeQuery()) {
							if (rs.next()) return rs.getInt("id") + " | " + rs.getString("title") + " | " + rs.getString("topic") + " | " + rs.getString("createdBy");
						}
					} catch (SQLException e) { e.printStackTrace(); }
					return null;
				}

				public void createQuestionReply(int questionId, int parentReplyId, String content, String createdBy) {
					String query = "INSERT INTO QuestionReplies (questionId, parentReplyId, content, createdBy) VALUES (?, ?, ?, ?)";
					try (PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setInt(1, questionId); pstmt.setInt(2, parentReplyId); pstmt.setString(3, content); pstmt.setString(4, createdBy);
						pstmt.executeUpdate();
					} catch (SQLException e) { e.printStackTrace(); }
				}

				public java.util.List<String> getRepliesForQuestion(int questionId) {
					java.util.List<String> list = new java.util.ArrayList<>();
					String query = "SELECT id, parentReplyId, content, createdBy FROM QuestionReplies WHERE questionId = ? ORDER BY id ASC";
					try (PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setInt(1, questionId);
						try (ResultSet rs = pstmt.executeQuery()) {
							while (rs.next()) list.add(rs.getInt("id") + " | " + rs.getInt("parentReplyId") + " | " + rs.getString("content") + " | " + rs.getString("createdBy"));
						}
					} catch (SQLException e) { e.printStackTrace(); }
					return list;
				}

				// --- UNREAD POST TRACKING ---
				public boolean hasReadPost(String username, int postId, String postType) {
					String query = "SELECT COUNT(*) FROM ReadPosts WHERE username = ? AND postId = ? AND postType = ?";
					try (PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setString(1, username); pstmt.setInt(2, postId); pstmt.setString(3, postType);
						try (ResultSet rs = pstmt.executeQuery()) {
							if (rs.next()) return rs.getInt(1) > 0;
						}
					} catch (SQLException e) { e.printStackTrace(); }
					return false;
				}

				public void markPostAsRead(String username, int postId, String postType) {
					if (hasReadPost(username, postId, postType)) return; // Already read
					String query = "INSERT INTO ReadPosts (username, postId, postType) VALUES (?, ?, ?)";
					try (PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setString(1, username); pstmt.setInt(2, postId); pstmt.setString(3, postType);
						pstmt.executeUpdate();
					} catch (SQLException e) {}
				}
				
				// Automatically updates the database schema safely
				public void setupGradingAndTimestamps() {
					try {
						statement.execute("ALTER TABLE DiscussionThreads ADD COLUMN IF NOT EXISTS createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
						statement.execute("ALTER TABLE Replies ADD COLUMN IF NOT EXISTS createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
						statement.execute("ALTER TABLE Questions ADD COLUMN IF NOT EXISTS createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
						statement.execute("ALTER TABLE QuestionReplies ADD COLUMN IF NOT EXISTS createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP");

						String gradesTable = "CREATE TABLE IF NOT EXISTS Grades ("
								+ "replyId INT PRIMARY KEY, "
								+ "wordScore INT, "
								+ "qualityScore INT, "
								+ "timeScore INT)";
						statement.execute(gradesTable);
					} catch (java.sql.SQLException e) { e.printStackTrace(); }
				}

				public String getTimestampStr(int id, String table) {
					String query = "SELECT createdAt FROM " + table + " WHERE id = ?";
					try (java.sql.PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setInt(1, id);
						try (java.sql.ResultSet rs = pstmt.executeQuery()) {
							if (rs.next() && rs.getTimestamp(1) != null) {
								return new java.text.SimpleDateFormat("MMM dd, yyyy h:mm a").format(rs.getTimestamp(1));
							}
						}
					} catch (Exception e) {}
					return "Just now"; // Default for newly created rows before refresh
				}

				public void saveGrade(int replyId, int wordScore, int qualityScore, int timeScore) {
					String query = "MERGE INTO Grades (replyId, wordScore, qualityScore, timeScore) KEY (replyId) VALUES (?, ?, ?, ?)";
					try (java.sql.PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setInt(1, replyId); pstmt.setInt(2, wordScore); pstmt.setInt(3, qualityScore); pstmt.setInt(4, timeScore);
						pstmt.executeUpdate();
					} catch (Exception e) { e.printStackTrace(); }
				}

				public String getGrade(int replyId) {
					String query = "SELECT wordScore, qualityScore, timeScore FROM Grades WHERE replyId = ?";
					try (java.sql.PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setInt(1, replyId);
						try (java.sql.ResultSet rs = pstmt.executeQuery()) {
							if (rs.next()) return rs.getInt(1) + "|" + rs.getInt(2) + "|" + rs.getInt(3);
						}
					} catch (Exception e) {}
					return null;
				}
				
				public void softDeleteThread(int id) {
					String query = "UPDATE DiscussionThreads SET title = '[Deleted]', topic = '[This post was deleted.]' WHERE id = ?";
					try (PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setInt(1, id); pstmt.executeUpdate();
					} catch (SQLException e) { e.printStackTrace(); }
				}
				public void softDeleteQuestion(int id) {
					String query = "UPDATE Questions SET title = '[Deleted]', topic = '[This post was deleted.]' WHERE id = ?";
					try (PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setInt(1, id); pstmt.executeUpdate();
					} catch (SQLException e) { e.printStackTrace(); }
				}
				public void softDeleteReply(int id) {
					String query = "UPDATE Replies SET content = '[This post was deleted.]' WHERE id = ?";
					try (PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setInt(1, id); pstmt.executeUpdate();
					} catch (SQLException e) { e.printStackTrace(); }
				}
				public void softDeleteQuestionReply(int id) {
					String query = "UPDATE QuestionReplies SET content = '[This post was deleted.]' WHERE id = ?";
					try (PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setInt(1, id); pstmt.executeUpdate();
					} catch (SQLException e) { e.printStackTrace(); }
				}
				
				public void deleteQuestion(int id) {
					String query = "DELETE FROM Questions WHERE id = ?";
					try (PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setInt(1, id); pstmt.executeUpdate();
					} catch (SQLException e) { e.printStackTrace(); }
				}
				public void deleteReply(int id) {
					String query = "DELETE FROM Replies WHERE id = ?";
					try (PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setInt(1, id); pstmt.executeUpdate();
					} catch (SQLException e) { e.printStackTrace(); }
				}
				public void deleteQuestionReply(int id) {
					String query = "DELETE FROM QuestionReplies WHERE id = ?";
					try (PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setInt(1, id); pstmt.executeUpdate();
					} catch (SQLException e) { e.printStackTrace(); }
				}
				
				// --- EDIT & MODERATION OPERATIONS ---
				public void updateReply(int id, String newContent) {
					String query = "UPDATE Replies SET content = ? WHERE id = ?";
					try (PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setString(1, newContent); pstmt.setInt(2, id); pstmt.executeUpdate();
					} catch (SQLException e) { e.printStackTrace(); }
				}
				public void updateQuestionReply(int id, String newContent) {
					String query = "UPDATE QuestionReplies SET content = ? WHERE id = ?";
					try (PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setString(1, newContent); pstmt.setInt(2, id); pstmt.executeUpdate();
					} catch (SQLException e) { e.printStackTrace(); }
				}
				public void updateQuestion(int id, String newTitle, String newTopic) {
					String query = "UPDATE Questions SET title = ?, topic = ? WHERE id = ?";
					try (PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setString(1, newTitle); pstmt.setString(2, newTopic); pstmt.setInt(3, id); pstmt.executeUpdate();
					} catch (SQLException e) { e.printStackTrace(); }
				}
				public void deleteAllRepliesForThread(int threadId) {
					String query = "DELETE FROM Replies WHERE threadId = ?";
					try (PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setInt(1, threadId); pstmt.executeUpdate();
					} catch (SQLException e) { e.printStackTrace(); }
				}
				public void deleteAllRepliesForQuestion(int questionId) {
					String query = "DELETE FROM QuestionReplies WHERE questionId = ?";
					try (PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setInt(1, questionId); pstmt.executeUpdate();
					} catch (SQLException e) { e.printStackTrace(); }
				}
				
				public int getOrCreateGeneralThread() {
					String query = "SELECT id FROM DiscussionThreads WHERE title = 'General'";
					try (PreparedStatement pstmt = connection.prepareStatement(query);
						 ResultSet rs = pstmt.executeQuery()) {
						if (rs.next()) return rs.getInt("id");
					} catch (SQLException e) {}
					
					try (PreparedStatement pstmt = connection.prepareStatement("INSERT INTO DiscussionThreads (title, topic, createdBy) VALUES ('General', 'General Discussion', 'System')")) {
						pstmt.executeUpdate();
					} catch (SQLException e) {}
					
					try (PreparedStatement pstmt = connection.prepareStatement(query);
						 ResultSet rs = pstmt.executeQuery()) {
						if (rs.next()) return rs.getInt("id");
					} catch (SQLException e) {}
					return 1;
				}
				
				// --- ADMIN REQUESTS & TEMP ADMIN OPERATIONS ---
				public boolean isTempAdmin(String username) {
					if (username == null || username.trim().isEmpty()) return false;
					String query = "SELECT expiry_time FROM TempAdmins WHERE username = ?";
					boolean isExpired = false;
					boolean isActive = false;
					
					try (java.sql.PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setString(1, username);
						try (java.sql.ResultSet rs = pstmt.executeQuery()) {
							if (rs.next()) {
								long expiry = rs.getLong("expiry_time");
								if (System.currentTimeMillis() < expiry) {
									isActive = true;
								} else {
									isExpired = true;
								}
							}
						}
					} catch (java.sql.SQLException e) { e.printStackTrace(); }
					
					if (isExpired) {
						try (java.sql.PreparedStatement del = connection.prepareStatement("DELETE FROM TempAdmins WHERE username = ?")) {
							del.setString(1, username);
							del.executeUpdate();
						} catch (java.sql.SQLException e) { e.printStackTrace(); }
					}
					
					return isActive;
				}

				public void submitAdminRequest(String username, String message) {
					String query = "INSERT INTO AdminRequests (username, message, status, admin_notes, was_denied) VALUES (?, ?, 'Pending', '', 0)";
					try (java.sql.PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setString(1, username);
						pstmt.setString(2, message);
						pstmt.executeUpdate();
					} catch (java.sql.SQLException e) { e.printStackTrace(); }
				}
				
				public void resubmitAdminRequest(int id, String message) {
					String query = "UPDATE AdminRequests SET message = ?, status = 'Pending' WHERE id = ?";
					try (java.sql.PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setString(1, message);
						pstmt.setInt(2, id);
						pstmt.executeUpdate();
					} catch (java.sql.SQLException e) { e.printStackTrace(); }
				}
				
				public void acceptAdminRequest(int id, String username, String notes) {
					String query = "UPDATE AdminRequests SET status = 'Accepted', admin_notes = ? WHERE id = ?";
					try (java.sql.PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setString(1, notes);
						pstmt.setInt(2, id);
						pstmt.executeUpdate();
					} catch (java.sql.SQLException e) { e.printStackTrace(); }
					
					long expiry = System.currentTimeMillis() + (24L * 60L * 60L * 1000L); // 24 hours
					// Using standard INSERT OR REPLACE for maximum compatibility
					String tq = "MERGE INTO TempAdmins (username, expiry_time) KEY (username) VALUES (?, ?)";
					try (java.sql.PreparedStatement pstmt = connection.prepareStatement(tq)) {
						pstmt.setString(1, username);
						pstmt.setLong(2, expiry);
						pstmt.executeUpdate();
					} catch (java.sql.SQLException e) {
						try (java.sql.PreparedStatement fallback = connection.prepareStatement("INSERT OR REPLACE INTO TempAdmins (username, expiry_time) VALUES (?, ?)")) {
							fallback.setString(1, username);
							fallback.setLong(2, expiry);
							fallback.executeUpdate();
						} catch (java.sql.SQLException ex) { ex.printStackTrace(); }
					}
				}
				
				public void denyAdminRequest(int id, String notes) {
					String query = "UPDATE AdminRequests SET status = 'Denied', was_denied = 1, admin_notes = ? WHERE id = ?";
					try (java.sql.PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setString(1, notes);
						pstmt.setInt(2, id);
						pstmt.executeUpdate();
					} catch (java.sql.SQLException e) { e.printStackTrace(); }
				}
				
				public void updateAdminRequestNotes(int id, String notes) {
					String query = "UPDATE AdminRequests SET admin_notes = ? WHERE id = ?";
					try (java.sql.PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setString(1, notes);
						pstmt.setInt(2, id);
						pstmt.executeUpdate();
					} catch (java.sql.SQLException e) { e.printStackTrace(); }
				}
				
				public java.util.List<String> getAdminRequests(String statusFilter, String usernameFilter) {
					java.util.List<String> results = new java.util.ArrayList<>();
					String query = "SELECT id, username, status, was_denied, message, admin_notes FROM AdminRequests WHERE 1=1";
					if (statusFilter != null) {
						if (statusFilter.equals("Closed")) query += " AND status != 'Pending'";
						else query += " AND status = '" + statusFilter + "'";
					}
					if (usernameFilter != null) query += " AND username = '" + usernameFilter + "'";
					
					try (java.sql.PreparedStatement pstmt = connection.prepareStatement(query);
						 java.sql.ResultSet rs = pstmt.executeQuery()) {
						while (rs.next()) {
							String req = rs.getInt("id") + "<SEP>" + 
										 rs.getString("username") + "<SEP>" + 
										 rs.getString("status") + "<SEP>" + 
										 rs.getInt("was_denied") + "<SEP>" + 
										 rs.getString("message") + "<SEP>" + 
										 (rs.getString("admin_notes") == null ? "" : rs.getString("admin_notes"));
							results.add(req);
						}
					} catch (java.sql.SQLException e) { e.printStackTrace(); }
					return results;
				}

				public java.util.List<String> getListOfUsers() {
					java.util.List<String> userList = new java.util.ArrayList<>();
					String query = "SELECT userName, adminRole, newRole1, newRole2 FROM userDB";
					try (java.sql.PreparedStatement pstmt = connection.prepareStatement(query);
						 java.sql.ResultSet rs = pstmt.executeQuery()) {
						while (rs.next()) {
							String username = rs.getString("userName");
							boolean adminRole = rs.getBoolean("adminRole");
							boolean newRole1 = rs.getBoolean("newRole1");
							boolean newRole2 = rs.getBoolean("newRole2");
							String roleStr = (adminRole ? "Admin " : "") + (newRole1 ? "Staff " : "") + (newRole2 ? "Student " : "");
							if (roleStr.isEmpty()) roleStr = "None";
							userList.add(username + " - Roles: " + roleStr.trim());
						}
					} catch (java.sql.SQLException e) { e.printStackTrace(); }
					return userList;
				}

				public String generateOTP(String roles, long durationMinutes) {
					String p = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+";
					StringBuilder tempPwd = new StringBuilder();
					for (int i = 0; i < 6; i++) {
						tempPwd.append(p.charAt((int) (Math.random() * p.length())));
					}
					return tempPwd.toString();
				}
		}