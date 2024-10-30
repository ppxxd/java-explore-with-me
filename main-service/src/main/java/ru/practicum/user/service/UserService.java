package ru.practicum.user.service;

import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<UserDto> getUsers(List<Long> ids, int from, int size);

    UserDto createUser(UserShortDto userShortDto);

    void deleteUser(Long userId);

    User checkUserExistsById(Long userId);
}
