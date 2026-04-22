package managers;

import database.Database;
import entityClasses.Reply;
import java.util.ArrayList;
import java.util.List;

/**
 * Collection manager for Reply entities.
 * Handles loading, cleaning orphaned replies, creating, updating, and deleting replies.
 */
public class ReplyCollection {
    private Database db;
    private List<Reply> allReplies;
    private List<Reply> activeSubset;

    /**
     * Constructor for ReplyCollection.
     * @param db The database instance used to interact with data.
     */
    public ReplyCollection(Database db) {
        this.db = db;
        this.allReplies = new ArrayList<>();
        this.activeSubset = new ArrayList<>();
    }

    /**
     * Loads all replies for a specific post and processes orphaned deletes.
     * @param postId The unique identifier of the parent post.
     * @param type The type of post ("Discussion" or "Question").
     */
    public void loadRepliesForPost(int postId, String type) {
        allReplies.clear();
        List<String> rawReplies = type.equals("Discussion") ? 
                                  db.getRepliesForThread(postId) : 
                                  db.getRepliesForQuestion(postId);

        for (String data : rawReplies) {
            String[] parts = data.split("\\|");
            if (parts.length >= 4) {
                allReplies.add(new Reply(
                    Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()), 
                    postId, type, parts[2].trim(), parts[3].trim()
                ));
            }
        }
        cleanupOrphanedDeletes(); // Automatically process the active subset
    }

    // --- FILTER SUBSET (Clean Orphaned Deletes) ---
    private void cleanupOrphanedDeletes() {
        boolean changed = true;
        List<Reply> currentList = new ArrayList<>(allReplies);

        while (changed) {
            changed = false;
            List<Reply> toKeep = new ArrayList<>();

            for (Reply reply : currentList) {
                boolean hasChildren = false;
                for (Reply checkR : currentList) {
                    if (checkR.getParentId() == reply.getId()) {
                        hasChildren = true;
                        break;
                    }
                }

                if (reply.getContent().equals("[This post was deleted.]") && !hasChildren) {
                    try {
                        if (reply.getType().equals("Discussion")) db.deleteReply(reply.getId());
                        else db.deleteQuestionReply(reply.getId());
                    } catch (Exception e) { e.printStackTrace(); }
                    changed = true;
                } else {
                    toKeep.add(reply);
                }
            }
            currentList = toKeep;
        }
        this.activeSubset = currentList;
    }

    /**
     * Gets the active subset of replies (excluding orphaned soft-deleted replies).
     * @return The list of active replies.
     */
    public List<Reply> getActiveSubset() { return activeSubset; }

    /**
     * Creates a new reply in the database and refreshes the collection.
     * @param postId The ID of the main post.
     * @param parentId The ID of the parent reply (0 if replying to the main post).
     * @param type The type of post ("Discussion" or "Question").
     * @param content The text content of the reply.
     * @param author The username of the author.
     */
    public void createReply(int postId, int parentId, String type, String content, String author) {
        try {
            if (type.equals("Discussion")) db.createReply(postId, parentId, content, author);
            else db.createQuestionReply(postId, parentId, content, author);
            loadRepliesForPost(postId, type);
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Updates an existing reply in the database and refreshes the collection.
     * @param replyId The unique identifier of the reply.
     * @param type The type of post ("Discussion" or "Question").
     * @param newContent The new content for the reply.
     * @param postId The ID of the main post to refresh the list.
     */
    public void updateReply(int replyId, String type, String newContent, int postId) {
        try {
            if (type.equals("Discussion")) db.updateReply(replyId, newContent);
            else db.updateQuestionReply(replyId, newContent);
            loadRepliesForPost(postId, type);
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Deletes a reply (soft delete if it has children, hard delete otherwise) and refreshes the collection.
     * @param replyId The unique identifier of the reply to delete.
     * @param type The type of post ("Discussion" or "Question").
     * @param postId The ID of the main post to refresh the list.
     */
    public void deleteReply(int replyId, String type, int postId) {
        try {
            boolean hasChildren = false;
            for (Reply r : activeSubset) {
                if (r.getParentId() == replyId) { hasChildren = true; break; }
            }

            if (hasChildren) {
                if (type.equals("Discussion")) db.softDeleteReply(replyId);
                else db.softDeleteQuestionReply(replyId);
            } else {
                if (type.equals("Discussion")) db.deleteReply(replyId);
                else db.deleteQuestionReply(replyId);
            }
            loadRepliesForPost(postId, type);
        } catch (Exception e) { e.printStackTrace(); }
    }
}