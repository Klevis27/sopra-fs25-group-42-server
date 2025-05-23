package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.entity.Note;
import ch.uzh.ifi.hase.soprafs24.entity.NoteLink;
import ch.uzh.ifi.hase.soprafs24.entity.NoteState;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.Vault;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DTOMapper {

    DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "password", target = "password")
    User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

    @Mapping(source = "username", target = "username")
    @Mapping(source = "password", target = "password")
    UserLoginDTO convertUserLoginDTOtoEntity(UserLoginDTO userLoginDTO);

    @Mapping(source = "id", target = "id")
    UserLogoutDTO convertUserLogoutDTOtoEntity(UserLogoutDTO userLogoutDTO);

    @Mapping(source = "content", target = "yjsState")
    NoteState convertNoteStatePutDTOtoEntity(NoteStatePutDTO noteStatePutDTO);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "creationDate", target = "creationDate")
    UserGetDTO convertEntityToUserGetDTO(User user);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "creationDate", target = "creationDate")
    UserProfileDTO convertEntityToUserProfileDTO(User user);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "username", target = "username")
    UserEditDTO convertEntityToUserEditDTO(User user);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "owner", target = "owner")
    VaultPostDTO convertEntityToVaultPostDTO(Vault vault);
    
    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    NotesPostDTO convertEntityToNotesPostDTO(Note note);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "owner.id", target = "ownerId")
    VaultsGetDTO convertEntityToVaultsGetDTO(Vault vault);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "sourceNote", target = "sourceNoteId")
    @Mapping(source = "targetNote", target = "targetNoteId")
    NoteLinksGetDTO convertEntityToNoteLinksGetDTO(NoteLink noteLink);

    default NotesGetDTO convertEntityToNotesGetDTO(Note note) {
        if (note == null) {
            return null;
        }
    
        NotesGetDTO dto = new NotesGetDTO();
        dto.setId(note.getId());
        dto.setTitle(note.getTitle());
    
        if (note.getVault() != null) {
            dto.setVaultId(note.getVault().getId());
        } else {
            System.err.println("⚠️ Warning: Note " + note.getId() + " has no vault!");
            dto.setVaultId(null); // or throw an exception if you want to force this
        }
    
        return dto;
    }
    

}
