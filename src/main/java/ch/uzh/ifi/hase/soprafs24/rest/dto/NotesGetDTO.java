package ch.uzh.ifi.hase.soprafs24.rest.dto;

public class NotesGetDTO {

    private Long id;
    private String title;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
