package id.maetaruna.assessment.postapi.dto;

import id.maetaruna.assessment.postapi.entity.Post;

public record PostRow(Integer userId, Integer id, String title, String body) {

    public static PostRow from(Post post) {
        return new PostRow(post.getUserId(), post.getId(), post.getTitle(), post.getBody());
    }
}
