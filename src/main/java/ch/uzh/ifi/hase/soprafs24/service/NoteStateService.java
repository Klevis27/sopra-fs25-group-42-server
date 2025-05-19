package ch.uzh.ifi.hase.soprafs24.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.uzh.ifi.hase.soprafs24.entity.Note;
import ch.uzh.ifi.hase.soprafs24.entity.NoteState;
import ch.uzh.ifi.hase.soprafs24.repository.NoteStatesRepository;
import ch.uzh.ifi.hase.soprafs24.repository.NoteRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.NoteStatePostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.NoteStatePutDTO;

@Service
public class NoteStateService {

    private final NoteRepository noteRepository;
    private final NoteStatesRepository noteStateRepository;

    public NoteStateService(
            NoteRepository noteRepository,
            NoteStatesRepository noteStateRepository
    ) {
        this.noteRepository = noteRepository;
        this.noteStateRepository = noteStateRepository;
    }

    @Transactional(readOnly = true)
    public byte[] loadState(Long noteId) {
        return noteRepository.findById(noteId)
                .flatMap(noteStateRepository::findByNote)
                .map(NoteState::getYjsState)
                .orElse(new byte[0]);
    }

    @Transactional
    public boolean createNoteState(NoteStatePostDTO dto) {
        Long noteId = dto.getNoteId();
        Optional<Note> optNote = noteRepository.findById(noteId);
        if (optNote.isEmpty()) {
            return false;
        }
        Note note = optNote.get();

        if (noteStateRepository.findByNote(note).isPresent()) {
            return false;
        }

        NoteState state = new NoteState();
        state.setNote(note);
        state.setYjsState(dto.getContent());
        noteStateRepository.save(state);
        return true;
    }

    @Transactional
    public boolean updateNoteStateContent(NoteStatePutDTO dto) {
        Long noteId = dto.getNoteId();
        Optional<Note> optNote = noteRepository.findById(noteId);
        if (optNote.isEmpty()) {
            return false;
        }
        Note note = optNote.get();

        Optional<NoteState> optState = noteStateRepository.findByNote(note);
        if (optState.isEmpty()) {
            return false;
        }
        NoteState state = optState.get();

        state.setYjsState(dto.getContent());
        noteStateRepository.save(state);
        return true;
    }
}