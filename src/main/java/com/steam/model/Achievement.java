package com.steam.model;

public class Achievement {
    private String id;
    private String title;
    private String description;
    private String category;
    private String requirementText;

    public Achievement() {}

    public Achievement(String id, String title, String description, String category, String requirementText) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.requirementText = requirementText;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getRequirementText() { return requirementText; }
    public void setRequirementText(String requirementText) { this.requirementText = requirementText; }
}
