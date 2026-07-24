package br.com.cpa.questionario.config;

import br.com.cpa.questionario.model.User;
import br.com.cpa.questionario.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class MustChangePasswordFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public MustChangePasswordFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())
                || rotaLivre(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        User user = userRepository.findByUsername(authentication.getName());
        if (user != null && user.isMustChangePassword()) {
            response.sendRedirect(request.getContextPath() + "/minha-conta/senha");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean rotaLivre(String uri) {
        return uri.startsWith("/minha-conta/senha")
                || uri.startsWith("/logout")
                || uri.startsWith("/login")
                || uri.startsWith("/perform_login")
                || uri.startsWith("/css/")
                || uri.startsWith("/js/")
                || uri.startsWith("/images/")
                || uri.startsWith("/webjars/")
                || uri.startsWith("/actuator/")
                || uri.startsWith("/error");
    }
}
