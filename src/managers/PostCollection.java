package managers;

import database.Database;
import entityClasses.Post;
import java.util.ArrayList;
import java.util.List;

/**
 * Collection manager for Post entities.
 * Handles loading, filtering, creating, updating, and deleting posts.
 */
public class PostCollection {
    private Database db;
    private List<Post> allPosts;
    private List<Post> filteredSubset;

    /**
     * Constructor for PostCollection.
     * @param db The database instance used to interact with data.
     */
    public PostCollection(Database db) {
        this.db = db;
        this.allPosts = new ArrayList<>();
        this.filteredSubset = new ArrayList<>();
    }

    /**
     * Loads all discussions and questions from the database into the collection.
     */
    public void loadAllPosts() {
        allPosts.clear();
        
        // Load Discussions
        for (String data : db.getThreadList()) {
            String[] parts = data.split("\\|");
            if (parts.length >= 4) {
                allPosts.add(new Post(
                    Integer.parseInt(parts[0].trim()), "Discussion", 
                    parts[1].trim(), parts[2].trim(), parts[3].trim()
                ));
            }
        }
        
        // Load Questions
        for (String data : db.getQuestionList()) {
            String[] parts = data.split("\\|");
            if (parts.length >= 4) {
                allPosts.add(new Post(
                    Integer.parseInt(parts[0].trim()), "Question", 
                    parts[1].trim(), parts[2].trim(), parts[3].trim()
                ));
            }
        }
        filteredSubset = new ArrayList<>(allPosts); // Default subset is all
    }

    /**
     * Filters the posts based on keyword, type, ownership, and read status.
     * @param keyword The keyword to search for in titles and topics.
     * @param typeFilter The type of post to filter by ("Discussion" or "Question").
     * @param myPostsOnly True to only show posts authored by the current user.
     * @param unreadOnly True to only show posts the current user hasn't read.
     * @param currentUser The username of the user performing the filter.
     */
    public void filterPosts(String keyword, String typeFilter, boolean myPostsOnly, boolean unreadOnly, String currentUser) {
        filteredSubset.clear();
        for (Post post : allPosts) {
            // Type & Keyword Filter
            if (typeFilter != null && post.getType().equals(typeFilter) && !keyword.isEmpty()) {
                if (!post.getTitle().toLowerCase().contains(keyword.toLowerCase()) &&
                    !post.getTopic().toLowerCase().contains(keyword.toLowerCase())) {
                    continue;
                }
            }

            // My Posts Filter
            boolean isMine = post.getAuthor().equalsIgnoreCase(currentUser);
            if (myPostsOnly && !isMine) continue;

            // Unread Filter
            boolean isRead = db.hasReadPost(currentUser, post.getId(), post.getType());
            if (unreadOnly && isRead) continue;

            filteredSubset.add(post);
        }
    }

    /**
     * Gets the current subset of posts after applying filters.
     * @return The list of filtered posts.
     */
    public List<Post> getFilteredSubset() { return filteredSubset; }

    /**
     * Gets the complete list of all loaded posts.
     * @return The complete list of posts.
     */
    public List<Post> getAllPosts() { return allPosts; }

    /**
     * Creates a new post in the database and refreshes the collection.
     * @param type The type of post ("Discussion" or "Question").
     * @param title The title of the post.
     * @param topic The main content/body of the post.
     * @param author The username of the author creating the post.
     */
    public void createPost(String type, String title, String topic, String author) {
        try {
            if (type.equals("Discussion")) db.createThread(title, topic, author);
            else db.createQuestion(title, topic, author);
            loadAllPosts(); // Refresh collection 
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Updates an existing post in the database and refreshes the collection.
     * @param id The unique identifier of the post.
     * @param type The type of post ("Discussion" or "Question").
     * @param newTitle The new title for the post.
     * @param newTopic The new content/body for the post.
     */
    public void updatePost(int id, String type, String newTitle, String newTopic) {
        try {
            if (type.equals("Discussion")) db.updateThread(id, newTitle, newTopic);
            else db.updateQuestion(id, newTitle, newTopic);
            loadAllPosts();
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Deletes a post and all of its associated replies from the database.
     * @param id The unique identifier of the post to delete.
     * @param type The type of post ("Discussion" or "Question").
     */
    public void deletePost(int id, String type) {
        try {
            if (type.equals("Discussion")) {
                db.deleteThread(id);
                db.deleteAllRepliesForThread(id);
            } else {
                db.deleteQuestion(id);
                db.deleteAllRepliesForQuestion(id);
            }
            loadAllPosts();
        } catch (Exception e) { e.printStackTrace(); }
    }
}