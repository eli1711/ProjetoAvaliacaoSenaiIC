package br.com.cpa.questionario.exception;

public class ResultadoRestritoException extends RuntimeException {

    private final long totalEnvios;
    private final int minimoExigido;

    public ResultadoRestritoException(String message, long totalEnvios, int minimoExigido) {
        super(message);
        this.totalEnvios = totalEnvios;
        this.minimoExigido = minimoExigido;
    }

    public long getTotalEnvios() {
        return totalEnvios;
    }

    public int getMinimoExigido() {
        return minimoExigido;
    }
}
