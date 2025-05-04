package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.entity.Note;

public class NoteStatePutDTO {
    private Long documentId;

    private Long noteId;
    
    private byte[] content;

    public Long getDocId(){
        return this.documentId;
    }

    public void setDocId(Long documentId){
        this.documentId = documentId;
    }

    public Long getNoteId(){
        return this.noteId;
    }

    public void setNote(Long noteId){
        this.noteId = noteId;
    }

    public byte[] getContent(){
        return this.content;
    }
    public void setContent(byte[] content){
        this.content = content;
    }

    public void setNoteId(Long noteId) {
    }
}
