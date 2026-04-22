package entityClasses;

/**
 * title: Post Class
 *
 * description: represents a single post in the student discussion system
 * a post is either a "Discussion" thread or a "Question"
 * supports full CRUD for posts as required by the student user stories
 *
 * create:  Post constructor
 * read:    getters - getId, getType, getTitle, getTopic, getAuthor
 * update:  setTitle, setTopic
 * delete:  handled by PostCollection.deletePost()
 *
 * also includes static validation methods to check input before
 * creating or updating a post
 *
 * @author CSE 360 Group 11
 * @version 1.00 2025-11-01
 */
public class Post {

    // unique database ID from H2 AUTO_INCREMENT
    private int id;

    // "Discussion" or "Question" - controls which DB table gets used
    private String type;

    // short headline shown in the tree view
    private String title;

    // full body/content of the post (named topic to match the DB column)
    private String topic;

    // username of whoever created the post
    private String author;

    /**
     * creates a new Post with all required fields
     * called by PostCollection.loadAllPosts() when reading from the database
     *
     * @param id unique integer ID from the database
     * @param type "Discussion" or "Question"
     * @param title short headline for the post
     * @param topic full body content of the post
     * @param author username of the post creator
     */
    public Post(int id, String type, String title, String topic, String author) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.topic = topic;
        this.author = author;
    }

    /**
     * Gets the unique database ID of the post.
     * @return The integer ID.
     */
    public int getId()        { return id; }

    /**
     * Gets the type of the post ("Discussion" or "Question").
     * @return The type of the post.
     */
    public String getType()   { return type; }

    /**
     * Gets the title or short headline of the post.
     * @return The post title.
     */
    public String getTitle()  { return title; }

    /**
     * Gets the full body content of the post.
     * @return The post topic/content.
     */
    public String getTopic()  { return topic; }

    /**
     * Gets the username of the post's author.
     * @return The author's username.
     */
    public String getAuthor() { return author; }

    /**
     * Sets a new title for the post.
     * @param title The new title to set.
     */
    public void setTitle(String title) { this.title = title; }

    /**
     * Sets new body content for the post.
     * @param topic The new topic content to set.
     */
    public void setTopic(String topic) { this.topic = topic; }

    /**
     * returns true if the title is not null and not blank
     * used before creating or saving a post to prevent empty titles
     *
     * @param title the title to check
     * @return true if valid, false otherwise
     */
    public static boolean isValidTitle(String title) {
        return title != null && !title.trim().isEmpty();
    }

    /**
     * returns true if the topic/body is not null and not blank
     * used before creating or saving a post to prevent empty content
     *
     * @param topic the topic to check
     * @return true if valid, false otherwise
     */
    public static boolean isValidTopic(String topic) {
        return topic != null && !topic.trim().isEmpty();
    }

    /**
     * returns true if the author is not null and not blank
     * makes sure every post can be attributed to a user
     *
     * @param author the author username to check
     * @return true if valid, false otherwise
     */
    public static boolean isValidAuthor(String author) {
        return author != null && !author.trim().isEmpty();
    }

    /**
     * returns true only if type is "Discussion" or "Question"
     * anything else would break the database routing in PostCollection
     *
     * @param type the type string to check
     * @return true if valid, false otherwise
     */
    public static boolean isValidType(String type) {
        return "Discussion".equals(type) || "Question".equals(type);
    }
}
