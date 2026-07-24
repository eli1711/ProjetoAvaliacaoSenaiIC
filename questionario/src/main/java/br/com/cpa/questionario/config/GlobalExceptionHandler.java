package br.com.cpa.questionario.config;

import br.com.cpa.questionario.dto.ApiErrorResponse;
import br.com.cpa.questionario.exception.ResultadoRestritoException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.UUID;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResultadoRestritoException.class)
    public Object handleResultadoRestrito(ResultadoRestritoException ex,
                                          HttpServletRequest request,
                                          Model model) {
        return responderErro(ex, request, model, HttpStatus.FORBIDDEN, "Resultado protegido", ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDenied(AccessDeniedException ex,
                                     HttpServletRequest request,
                                     Model model) {
        return responderErro(ex, request, model, HttpStatus.FORBIDDEN, "Acesso negado", ex.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Object handleResponseStatus(ResponseStatusException ex,
                                       HttpServletRequest request,
                                       Model model) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        return responderErro(ex, request, model, status, status.getReasonPhrase(), message);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Object handleNoResource(NoResourceFoundException ex,
                                   HttpServletRequest request,
                                   Model model) {
        return responderErro(ex, request, model, HttpStatus.NOT_FOUND,
                "Nao encontrado", "Recurso nao encontrado.");
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public Object handleBusiness(Exception ex,
                                 HttpServletRequest request,
                                 Model model) {
        return responderErro(ex, request, model, HttpStatus.BAD_REQUEST, "Requisicao invalida", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Object handleUnexpected(Exception ex,
                                   HttpServletRequest request,
                                   Model model) {
        return responderErro(ex, request, model, HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno", "Nao foi possivel concluir a operacao.");
    }

    private Object responderErro(Exception ex,
                                 HttpServletRequest request,
                                 Model model,
                                 HttpStatus status,
                                 String error,
                                 String message) {
        String traceId = UUID.randomUUID().toString();
        if (status.is5xxServerError()) {
            log.error("Erro tratado traceId={} path={}", traceId, request.getRequestURI(), ex);
        } else {
            log.warn("Erro tratado traceId={} status={} path={} message={}",
                    traceId, status.value(), request.getRequestURI(), message);
        }

        if (isRespostaApi(request)) {
            ApiErrorResponse body = new ApiErrorResponse(
                    Instant.now(),
                    status.value(),
                    error,
                    message,
                    request.getRequestURI(),
                    traceId);
            return ResponseEntity.status(status).body(body);
        }

        model.addAttribute("statusCode", status.value());
        model.addAttribute("errorTitle", error);
        model.addAttribute("errorMessage", message);
        model.addAttribute("traceId", traceId);
        return new ModelAndView("error", model.asMap(), status);
    }

    private boolean isRespostaApi(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String accept = request.getHeader("Accept");
        String requestedWith = request.getHeader("X-Requested-With");

        return uri.startsWith("/api/")
                || uri.contains("/export-")
                || uri.contains("/csv")
                || "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || (accept != null && (accept.contains("application/json") || accept.contains("text/csv")));
    }
}
