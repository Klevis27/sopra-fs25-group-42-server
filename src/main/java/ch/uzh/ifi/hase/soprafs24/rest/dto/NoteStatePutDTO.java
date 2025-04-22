package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.math.BigInteger;

public class NoteStatePutDTO {
    private BigInteger documentId;

    private BigInteger noteId;
    
    private byte[] content;

    public BigInteger getDocId(){
        return this.documentId;
    }

    public void setDocId(BigInteger documentId){
        this.documentId = documentId;
    }

    public BigInteger getNoteId(){
        return this.noteId;
    }

    public void setNoteId(BigInteger noteId){
        this.noteId = noteId;
    }

    public byte[] getContent(){
        return this.content;
    }
    public void setContent(byte[] content){
        this.content = content;
    }

}
