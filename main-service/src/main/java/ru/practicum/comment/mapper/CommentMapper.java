package ru.practicum.comment.mapper;

import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.event.service.EventService;
import ru.practicum.user.service.UserService;

public class CommentMapper {
    public static Comment fromDto(CommentDto dto, UserService userService, EventService eventService) {
        if (dto == null) {
            return null;
        }

        return Comment.builder()
                .author(userService.checkUserExistsById(dto.getAuthor()))
                .event(eventService.checkEventExistsById(dto.getEvent()))
                .text(dto.getText())
                .createdOn(dto.getCreatedOn())
                .build();
    }

    public static CommentDto toDto(Comment comment) {
        if (comment == null) {
            return null;
        }

        return CommentDto.builder()
                .id(comment.getId())
                .author(comment.getAuthor().getId())
                .event(comment.getEvent().getId())
                .text(comment.getText())
                .createdOn(comment.getCreatedOn())
                .build();
    }
}
