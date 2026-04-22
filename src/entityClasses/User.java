package entityClasses;

/**
 * <p> Title: User Class </p>
 * * <p> Description: This User class represents a user entity in the system.  It contains the user's
 * details such as userName, password, and roles being played. </p>
 * * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * * @author Lynn Robert Carter 
 */ 
public class User {
	
	/*
	 * These are the private attributes for this entity object
	 */
    private String userName;
    private String password;
    private String firstName;
    private String middleName;
    private String lastName;
    private String preferredFirstName;
    private String emailAddress;
    private boolean adminRole;
    private boolean role1;
    private boolean role2;
    
    
    /**
     * <p> Method: User() </p>
     * * <p> Description: This default constructor is not used in this system. </p>
     */
    public User() {
    	
    }

    
    /**
     * <p> Method: User(String userName, String password, String fn, String mn, String ln, String pfn, String ea, boolean r1, boolean r2, boolean r3) </p>
     * * <p> Description: This constructor is used to establish user entity objects with full details. </p>
     * @param userName specifies the account userName for this user
     * @param password specifies the account password for this user
     * @param fn specifies the first name
     * @param mn specifies the middle name
     * @param ln specifies the last name
     * @param pfn specifies the preferred first name
     * @param ea specifies the email address
     * @param r1 specifies the Admin role (TRUE or FALSE)
     * @param r2 specifies the Staff/Role1 attribute (TRUE or FALSE)
     * @param r3 specifies the Student/Role2 attribute (TRUE or FALSE)
     */
    public User(String userName, String password, String fn, String mn, String ln, String pfn, 
    		String ea, boolean r1, boolean r2, boolean r3) {
        this.userName = userName;
        this.password = password;
        this.firstName = fn;
        this.middleName = mn;
        this.lastName = ln;
        this.preferredFirstName = pfn;
        this.emailAddress = ea;
        this.adminRole = r1;
        this.role1 = r2;
        this.role2 = r3;
    }

    
    /**
     * <p> Method: void setAdminRole(boolean role) </p>
     * * <p> Description: This setter defines the Admin role attribute. Sets the role of the Admin user. </p>
     * @param role is a boolean that specifies if this user in playing the Admin role.
     */
    public void setAdminRole(boolean role) {
    	this.adminRole = role;
    }

    
    /**
     * <p> Method: void setRole1User(boolean role) </p>
     * * <p> Description: This setter defines the role1 attribute. Sets the role1 user. </p>
     * @param role is a boolean that specifies if this user in playing role1.
     */
    public void setRole1User(boolean role) {
    	this.role1 = role;
    }

    
    /**
     * <p> Method: void setRole2User(boolean role) </p>
     * * <p> Description: This setter defines the role2 attribute. Sets the role2 user. </p>
     * @param role is a boolean that specifies if this user in playing role2.
     */
    public void setRole2User(boolean role) {
    	this.role2 = role;
    }

    
    /**
     * <p> Method: String getUserName() </p>
     * * <p> Description: This getter returns the UserName. </p>
     * @return a String of the UserName
     */
    public String getUserName() { return userName; }

    
    /**
     * <p> Method: String getPassword() </p>
     * * <p> Description: This getter returns the Password. </p>
     * @return a String of the password
     */
    public String getPassword() { return password; }

    
    /**
     * <p> Method: String getFirstName() </p>
     * * <p> Description: This getter returns the FirstName. </p>
     * @return a String of the FirstName
     */
    public String getFirstName() { return firstName; }

    
    /**
     * <p> Method: String getMiddleName() </p>
     * * <p> Description: This getter returns the MiddleName. </p>
     * @return a String of the MiddleName
     */
    public String getMiddleName() { return middleName; }

    
    /**
     * <p> Method: String getLastName() </p>
     * * <p> Description: This getter returns the LastName. </p>
     * @return a String of the LastName
     */
    public String getLastName() { return lastName; }

    
    /**
     * <p> Method: String getPreferredFirstName() </p>
     * * <p> Description: This getter returns the PreferredFirstName. </p>
     * @return a String of the PreferredFirstName
     */
    public String getPreferredFirstName() { return preferredFirstName; }

    
    /**
     * <p> Method: String getEmailAddress() </p>
     * * <p> Description: This getter returns the EmailAddress. </p>
     * @return a String of the EmailAddress
     */
    public String getEmailAddress() { return emailAddress; }

    /**
     * <p> Method: void setUserName(String s) </p>
     * * <p> Description: This setter defines the user name attribute. </p>
     * @param s is a String that specifies the user name.
     */
    public void setUserName(String s) { userName = s; }

    /**
     * <p> Method: void setPassword(String s) </p>
     * * <p> Description: This setter defines the password attribute. </p>
     * @param s is a String that specifies the password.
     */
    public void setPassword(String s) { password = s; }

    /**
     * <p> Method: void setFirstName(String s) </p>
     * * <p> Description: This setter defines the first name attribute. </p>
     * @param s is a String that specifies the first name.
     */
    public void setFirstName(String s) { firstName = s; }

    /**
     * <p> Method: void setMiddleName(String s) </p>
     * * <p> Description: This setter defines the middle name attribute. </p>
     * @param s is a String that specifies the middle name.
     */
    public void setMiddleName(String s) { middleName = s; }

    /**
     * <p> Method: void setLastName(String s) </p>
     * * <p> Description: This setter defines the last name attribute. </p>
     * @param s is a String that specifies the last name.
     */
    public void setLastName(String s) { lastName = s; }

    /**
     * <p> Method: void setPreferredFirstName(String s) </p>
     * * <p> Description: This setter defines the preferred first name attribute. </p>
     * @param s is a String that specifies the preferred first name.
     */
    public void setPreferredFirstName(String s) { preferredFirstName = s; }

    /**
     * <p> Method: void setEmailAddress(String s) </p>
     * * <p> Description: This setter defines the email address attribute. </p>
     * @param s is a String that specifies the email address.
     */
    public void setEmailAddress(String s) { emailAddress = s; }

    
    /**
     * <p> Method: boolean getAdminRole() </p>
     * * <p> Description: This getter returns the value of the Admin role attribute. </p>
     * @return a boolean representing the state of the attribute
     */
    public boolean getAdminRole() { return adminRole; }

    
    /**
     * <p> Method: boolean getNewRole1() </p>
     * * <p> Description: This getter returns the value of the role1 attribute. </p>
     * @return a boolean representing the state of the attribute
     */
	public boolean getNewRole1() { return role1; }

    
    /**
     * <p> Method: boolean getNewRole2() </p>
     * * <p> Description: This getter returns the value of the role2 attribute. </p>
     * @return a boolean representing the state of the attribute
     */
    public boolean getNewRole2() { return role2; }

        
    /**
     * <p> Method: int getNumRoles() </p>
     * * <p> Description: This getter returns the number of roles this user plays (0 - 5). </p>
     * @return a value 0 - 5 of the number of roles this user plays
     */
    public int getNumRoles() {
    	int numRoles = 0;
    	if (adminRole) numRoles++;
    	if (role1) numRoles++;
    	if (role2) numRoles++;
    	return numRoles;
    }
}