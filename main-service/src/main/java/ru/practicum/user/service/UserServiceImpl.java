package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ObjectNotFoundException;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        PageRequest pageable = PageRequest.of(from / size, size);

        if (ids != null) {
            return userRepository.findByIdIn(ids, pageable).stream()
                    .map(UserMapper::toDto)
                    .collect(Collectors.toList());
        } else {
            return userRepository.findAll(pageable).stream()
                    .map(UserMapper::toDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public UserDto createUser(UserShortDto userShortDto) {
        Optional<User> userOpt = userRepository.findByName(userShortDto.getName());
        if (userOpt.isPresent()) {
            throw new ConflictException(String.format("User with name %s already exists",
                    userShortDto.getName()));
        }

        User user = userRepository.save(UserMapper.fromDto(userShortDto));
        return UserMapper.toDto(user);
    }

    @Override
    public void deleteUser(Long userId) {
        checkUserExistsById(userId);
        userRepository.deleteById(userId);
    }

    @Override
    public User checkUserExistsById(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new ObjectNotFoundException(String.format("User with id=%s was not found", userId)
        ));
    }
}
