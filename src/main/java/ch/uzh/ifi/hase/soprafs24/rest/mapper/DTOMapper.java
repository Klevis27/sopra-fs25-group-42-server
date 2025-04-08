package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.entity.*;
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
    @Mapping(source = "title", target = "title")
    NotesGetDTO convertEntityToNotesGetDTO(Note note);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    VaultPostDTO convertEntityToVaultPostDTO(Vault vault);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    VaultsGetDTO convertEntityToVaultsGetDTO(Vault vault);
}