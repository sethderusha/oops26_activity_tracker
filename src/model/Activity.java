package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public class Activity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;                  // unique identifier (important for edit/delete)
    private String type;                // Running, Gym, etc.
    private int duration;               // minutes
    private List<String> collaborators; // names
    private int quality;                // rating (e.g., 1–5)
    private String notes;               // optional
    private LocalDate date;             // when activity happened

    public Activity(String id, String type, int duration,
                    List<String> collaborators, int quality,
                    String notes, LocalDate date) {
        this.id = id;
        this.type = type;
        this.duration = duration;
        this.collaborators = collaborators;
        this.quality = quality;
        this.notes = notes;
        this.date = date;
    }

    public String getId() { return id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public List<String> getCollaborators() { return collaborators; }
    public void setCollaborators(List<String> collaborators) { this.collaborators = collaborators; }

    public int getQuality() { return quality; }
    public void setQuality(int quality) { this.quality = quality; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDate getDate() { return date; }

}