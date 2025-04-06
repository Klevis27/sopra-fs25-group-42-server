package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.entity.Note;
import ch.uzh.ifi.hase.soprafs24.entity.NoteLink;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.Vault;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * DTOMapper
 * This class is responsible for generating classes that will automatically
 * transform/map the internal representation
 * of an entity (e.g., the User) to the external/API representation (e.g.,
 * UserGetDTO for getting, UserPostDTO for creating)
 * and vice versa.
 * Additional mappers can be defined for new entities.
 * Always created one mapper for getting information (GET) and one mapper for
 * creating information (POST).
 */
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
    NotesGetDTO convertEntityToNotesGetDTO(Note note);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    VaultsGetDTO convertEntityToVaultsGetDTO(Vault vault);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "sourceNote", target = "sourceNoteId")
    @Mapping(source = "targetNote", target = "targetNoteId")
    NoteLinksGetDTO convertEntityToNoteLinksGetDTO(NoteLink noteLink);

}
