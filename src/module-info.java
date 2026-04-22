/**
 * Main module for the Foundations Fall 2025 application.
 * Manages UI, database, and core logic.
 */
module FoundationsF25 {
	requires javafx.controls;
	requires java.sql;
	
	exports database;
	opens applicationMain to javafx.graphics, javafx.fxml;
}