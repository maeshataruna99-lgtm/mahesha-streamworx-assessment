package id.maetaruna.assessment.userapi.client;

public class PostsUnavailableException extends RuntimeException {

    public PostsUnavailableException(Throwable cause) {
        super("Posts service is unavailable", cause);
    }
}
