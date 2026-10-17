package id.maetaruna.assessment.gateway;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import tools.jackson.databind.json.JsonMapper;

// The load balancer reports a route whose service has no registered instance with this exception.
// Gateway routes are router functions, which @ControllerAdvice does not cover, so without this
// resolver the exception reaches the servlet container and becomes a generic 500.
@Component
public class ServiceUnavailableResolver implements HandlerExceptionResolver, Ordered {

    private final JsonMapper jsonMapper;

    public ServiceUnavailableResolver(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) {
        if (!(ex instanceof HttpServerErrorException unavailable)
                || unavailable.getStatusCode().value() != HttpStatus.SERVICE_UNAVAILABLE.value()) {
            return null;
        }
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                unavailable.getStatusText());
        try {
            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.getWriter().write(jsonMapper.writeValueAsString(problem));
        } catch (IOException e) {
            return null;
        }
        return new ModelAndView();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
