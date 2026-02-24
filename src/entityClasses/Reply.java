package entityClasses;

public class Reply {
    private int id;
    private int parentId; // 0 if replying to the main post
    private int postId;
    private String type; // "Discussion" or "Question"
    private String content;
    private String author;

    public Reply(int id, int parentId, int postId, String type, String content, String author) {
        this.id = id;
        this.parentId = parentId;
        this.postId = postId;
        this.type = type;
        this.content = content;
        this.author = author;
    }

    // Getters
    public int getId() { return id; }
    public int getParentId() { return parentId; }
    public int getPostId() { return postId; }
    public String getType() { return type; }
    public String getContent() { return content; }
    public String getAuthor() { return author; }

    // Setters
    public void setContent(String content) { this.content = content; }
}