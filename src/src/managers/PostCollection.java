package managers;

import database.Database;
import entityClasses.Post;
import java.util.ArrayList;
import java.util.List;

public class PostCollection {
    private Database db;
    private List<Post> allPosts;
    private List<Post> filteredSubset;

    public PostCollection(Database db) {
        this.db = db;
        this.allPosts = new ArrayList<>();
        this.filteredSubset = new ArrayList<>();
    }

    // --- READ / LOAD ---
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

    // --- FILTER SUBSET ---
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

    public List<Post> getFilteredSubset() { return filteredSubset; }
    public List<Post> getAllPosts() { return allPosts; }

    // --- CREATE ---
    public void createPost(String type, String title, String topic, String author) {
        try {
            if (type.equals("Discussion")) db.createThread(title, topic, author);
            else db.createQuestion(title, topic, author);
            loadAllPosts(); // Refresh collection
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- UPDATE ---
    public void updatePost(int id, String type, String newTitle, String newTopic) {
        try {
            if (type.equals("Discussion")) db.updateThread(id, newTitle, newTopic);
            else db.updateQuestion(id, newTitle, newTopic);
            loadAllPosts();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- DELETE ---
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