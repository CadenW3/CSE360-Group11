package entityClasses;

public class Post {
    private int id;
    private String type; // "Discussion" or "Question"
    private String title;
    private String topic;
    private String author;

    public Post(int id, String type, String title, String topic, String author) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.topic = topic;
        this.author = author;
    }

    // Getters
    public int getId() { return id; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getTopic() { return topic; }
    public String getAuthor() { return author; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setTopic(String topic) { this.topic = topic; }
}