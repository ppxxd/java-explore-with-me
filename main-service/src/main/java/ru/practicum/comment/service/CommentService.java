package ru.practicum.comment.service;

import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.model.Comment;

import java.util.List;

public interface CommentService {
    CommentDto createComment(Long userId, Long eventId, NewCommentDto commentDto);

    CommentDto updateCommentById(Long commentId, Long userId, NewCommentDto commentDto);

    void deleteCommentById(Long commentId, Long userId);

    void deleteComment(Long commentId);

    List<CommentDto> getAllCommentsByEventId(Long eventId, int from, int size);

    Comment checkCommentExistById(Long commentId);
}
