package entityClasses;

/**
 * Represents a reply to a post (Discussion or Question) or another reply.
 */
public class Reply {
    private int id;
    private int parentId; // 0 if replying to the main post
    private int postId;
    private String type; // "Discussion" or "Question" 
    private String content;
    private String author;

    /**
     * Creates a new Reply instance.
     * @param id The unique identifier of the reply.
     * @param parentId The ID of the parent reply (0 if replying directly to the main post).
     * @param postId The ID of the post this reply belongs to.
     * @param type The type of post ("Discussion" or "Question").
     * @param content The text content of the reply.
     * @param author The username of the author who wrote the reply.
     */
    public Reply(int id, int parentId, int postId, String type, String content, String author) {
        this.id = id;
        this.parentId = parentId;
        this.postId = postId;
        this.type = type;
        this.content = content;
        this.author = author;
    }

    // Getters

    /**
     * Gets the unique database ID of the reply.
     * @return The integer ID.
     */
    public int getId() { return id; }

    /**
     * Gets the parent ID to determine threading.
     * @return The parent reply ID, or 0 if it replies directly to the original post.
     */
    public int getParentId() { return parentId; }

    /**
     * Gets the ID of the post this reply belongs to.
     * @return The parent post ID.
     */
    public int getPostId() { return postId; }

    /**
     * Gets the type of the parent post.
     * @return The type string ("Discussion" or "Question").
     */
    public String getType() { return type; }

    /**
     * Gets the text content of the reply.
     * @return The reply content.
     */
    public String getContent() { return content; }

    /**
     * Gets the username of the reply's author.
     * @return The author's username.
     */
    public String getAuthor() { return author; }

    // Setters

    /**
     * Sets the text content of the reply.
     * @param content The new content to set.
     */
    public void setContent(String content) { this.content = content; }
}