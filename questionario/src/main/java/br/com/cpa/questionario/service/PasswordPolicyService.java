package br.com.cpa.questionario.service;

import br.com.cpa.questionario.exception.PasswordPolicyException;
import br.com.cpa.questionario.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Service
public class PasswordPolicyService {

    private final int minLength;
    private final int maxLength;
    private final int minCategories;

    public PasswordPolicyService(@Value("${app.security.password.min-length:10}") int minLength,
                                 @Value("${app.security.password.max-length:128}") int maxLength,
                                 @Value("${app.security.password.min-categories:3}") int minCategories) {
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.minCategories = minCategories;
    }

    public void validar(String rawPassword, User user) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new PasswordPolicyException("Informe uma senha.");
        }
        if (!rawPassword.equals(rawPassword.trim())) {
            throw new PasswordPolicyException("A senha nao pode iniciar ou terminar com espacos.");
        }
        if (rawPassword.length() < minLength) {
            throw new PasswordPolicyException("A senha deve ter pelo menos " + minLength + " caracteres.");
        }
        if (rawPassword.length() > maxLength) {
            throw new PasswordPolicyException("A senha deve ter no maximo " + maxLength + " caracteres.");
        }
        if (categorias(rawPassword) < minCategories) {
            throw new PasswordPolicyException("A senha deve combinar letras maiusculas, minusculas, numeros ou simbolos.");
        }
        if (contemDadoDoUsuario(rawPassword, user)) {
            throw new PasswordPolicyException("A senha nao deve conter login, nome, e-mail ou matricula.");
        }
        if (ehSenhaComum(rawPassword)) {
            throw new PasswordPolicyException("Escolha uma senha menos comum.");
        }
    }

    public String resumoRegras() {
        return "Use pelo menos " + minLength + " caracteres e combine letras maiusculas, minusculas, numeros ou simbolos.";
    }

    public String gerarSenhaInicialAluno(String ra, String cpf) {
        return somenteDigitos(cpf);
    }

    private int categorias(String password) {
        int count = 0;
        if (password.chars().anyMatch(Character::isUpperCase)) {
            count++;
        }
        if (password.chars().anyMatch(Character::isLowerCase)) {
            count++;
        }
        if (password.chars().anyMatch(Character::isDigit)) {
            count++;
        }
        if (password.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch))) {
            count++;
        }
        return count;
    }

    private boolean contemDadoDoUsuario(String rawPassword, User user) {
        if (user == null) {
            return false;
        }
        String password = rawPassword.toLowerCase(Locale.ROOT);
        return Stream.of(user.getUsername(), user.getName(), user.getEmail(), user.getRa())
                .filter(value -> value != null && value.trim().length() >= 4)
                .map(value -> value.toLowerCase(Locale.ROOT).trim())
                .anyMatch(password::contains);
    }

    private boolean ehSenhaComum(String rawPassword) {
        String value = rawPassword.toLowerCase(Locale.ROOT);
        return List.of(
                "12345678",
                "123456789",
                "1234567890",
                "password",
                "senha123",
                "admin123",
                "qwerty",
                "instituicao"
        ).contains(value);
    }

    private String somenteDigitos(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("\\D", "");
    }
}
