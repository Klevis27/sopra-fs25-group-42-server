package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.entity.Note;

public class NoteLinksGetDTO {
    private Long id;
    private Long sourceNoteId;
    private Long targetNoteId;


    public Long getId(){
        return this.id;
    }

    public void setId(Long id){
        this.id = id;
    }

    public Long getSourceNoteId(){
        return this.sourceNoteId;
    }

    public void setSourceNoteId(Note sourceNote){
        this.sourceNoteId = sourceNote.getId();
    }

    public Long getTargetNoteId(){
        return this.targetNoteId;
    }

    public void setTargetNoteId(Note targetNote){
        this.targetNoteId = targetNote.getId();
    }
    
}
