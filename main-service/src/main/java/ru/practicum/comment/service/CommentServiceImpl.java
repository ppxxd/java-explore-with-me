package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.service.EventService;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ObjectNotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final EventService eventService;
    private final UserService userService;
    private final RequestRepository requestRepository;

    @Transactional
    @Override
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        User user = userService.checkUserExistsById(userId);
        Event event = eventService.checkEventExistsById(eventId);

        checkCommentConditions(event, user);

        Comment comment = CommentMapper.fromNewDto(newCommentDto);
        comment.setCreatedOn(LocalDateTime.now());
        comment.setAuthor(user);
        comment.setEvent(event);
        return CommentMapper.toDto(commentRepository.save(comment));
    }

    @Override
    public void deleteCommentById(Long commentId, Long userId) {
        Comment comment = CommentMapper.fromDto(checkCommentExistById(commentId), userService, eventService);

        checkUserIsAuthorComment(comment.getAuthor().getId(), userId, commentId);

        commentRepository.deleteById(commentId);
    }

    @Override
    public void deleteComment(Long commentId) {
        checkCommentExistById(commentId);
        commentRepository.deleteById(commentId);
    }

    @Transactional
    @Override
    public CommentDto updateCommentById(Long commentId, Long userId, NewCommentDto newCommentDto) {
        Comment foundComment = CommentMapper.fromDto(checkCommentExistById(commentId), userService, eventService);

        checkUserIsAuthorComment(foundComment.getAuthor().getId(), userId, commentId);

        foundComment.setText(newCommentDto.getText());

        return CommentMapper.toDto(commentRepository.save(foundComment));
    }

    @Override
    public List<CommentDto> getAllCommentsByEventId(Long eventId, int from, int size) {
        eventService.checkEventExistsById(eventId);

        PageRequest pageRequest = PageRequest.of(from, size);
        List<Comment> comments = commentRepository.findAllByEventIdOrderByCreatedOnDesc(eventId, pageRequest);

        return comments.stream().map(CommentMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public CommentDto checkCommentExistById(Long commentId) {
        return CommentMapper.toDto(commentRepository.findById(commentId).orElseThrow(
                () -> new ObjectNotFoundException(String.format("Comment %d not found", commentId)))
        );
    }

    private void checkCommentConditions(Event event, User user) {
        if (!Objects.equals(event.getInitiator().getId(), user.getId())) {
            Request result = requestRepository.findByRequesterIdAndEventId(user.getId(), event.getId())
                    .orElseThrow(() -> new ValidationException(String.format("User %d does not participate in event %d!",
                            user.getId(), event.getId())));
            if (result.getStatus() != Request.RequestStatus.CONFIRMED) {
                throw new ValidationException(String.format("User %d does not participate in event %d!",
                        user.getId(), event.getId()));
            }
        }

        Optional<Comment> foundComment = commentRepository.findByEventIdAndAuthorId(event.getId(), user.getId());
        if (foundComment.isPresent()) {
            throw new ConflictException(String.format("User id=%s has already published a comment to event " +
                    "id=%s", user.getId(), event.getId()));
        }
    }

    private void checkUserIsAuthorComment(Long authorId, Long userId, Long commentId) {
        if (!Objects.equals(authorId, userId)) {
            throw new ValidationException(String.format(
                    "User %d is not owner of comment %d",
                    userId, commentId));
        }
    }
}
