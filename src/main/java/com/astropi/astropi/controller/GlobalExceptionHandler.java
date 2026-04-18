package com.astropi.astropi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Maneja errores comunes de la API para devolver respuestas claras en Postman.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Pattern CAMPO_NO_RECONOCIDO_PATTERN =
            Pattern.compile("Unrecognized (?:field|property) \"([^\"]+)\"");

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {

        Map<String, String> errores = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("error", "Datos invalidos");
        response.put("campos", errores);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadableBody(HttpMessageNotReadableException ex) {

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("error", "JSON invalido");

        String campoNoPermitido = obtenerCampoNoPermitido(ex);

        if (campoNoPermitido != null) {
            response.put("mensaje", "Campo no permitido: " + campoNoPermitido);
        } else {
            response.put("mensaje", "Revisa el formato del body y los valores enviados");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    private String obtenerCampoNoPermitido(HttpMessageNotReadableException exception) {

        UnrecognizedPropertyException unrecognizedPropertyException = buscarCampoNoReconocido(exception);

        if (unrecognizedPropertyException != null) {
            return unrecognizedPropertyException.getPropertyName();
        }

        String campoDesdeMensaje = extraerCampoNoReconocido(exception.getMessage());

        if (campoDesdeMensaje != null) {
            return campoDesdeMensaje;
        }

        Throwable causaEspecifica = exception.getMostSpecificCause();

        if (causaEspecifica != null) {
            String campoDesdeCausaEspecifica = extraerCampoNoReconocido(causaEspecifica.getMessage());

            if (campoDesdeCausaEspecifica != null) {
                return campoDesdeCausaEspecifica;
            }
        }

        return extraerCampoNoReconocido(exception.getMessage());
    }

    private UnrecognizedPropertyException buscarCampoNoReconocido(Throwable exception) {

        Throwable causa = exception;

        while (causa != null) {
            if (causa instanceof UnrecognizedPropertyException unrecognizedPropertyException) {
                return unrecognizedPropertyException;
            }

            causa = causa.getCause();
        }

        return null;
    }

    private String extraerCampoNoReconocido(String mensaje) {

        if (mensaje == null) {
            return null;
        }

        Matcher matcher = CAMPO_NO_RECONOCIDO_PATTERN.matcher(mensaje);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("error", ex.getStatusCode().toString());
        response.put("mensaje", ex.getReason());

        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleRequestParamTypeMismatch(MethodArgumentTypeMismatchException ex) {

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("error", "Parametro invalido");
        response.put("mensaje", crearMensajeParametroInvalido(ex));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    private String crearMensajeParametroInvalido(MethodArgumentTypeMismatchException ex) {

        String nombreParametro = ex.getName();
        Class<?> tipoEsperado = ex.getRequiredType();

        if (tipoEsperado == null) {
            return "El parametro " + nombreParametro + " no tiene un formato valido";
        }

        if (tipoEsperado.isEnum()) {
            return "Valor no permitido para " + nombreParametro + ". Valores permitidos: "
                    + obtenerValoresEnum(tipoEsperado);
        }

        if (tipoEsperado.equals(LocalDate.class)) {
            return "Formato no valido para " + nombreParametro + ". Usa yyyy-MM-dd";
        }

        if (tipoEsperado.equals(Integer.class) || tipoEsperado.equals(int.class)
                || tipoEsperado.equals(Long.class) || tipoEsperado.equals(long.class)) {
            return "El parametro " + nombreParametro + " debe ser numerico";
        }

        return "El parametro " + nombreParametro + " no tiene un formato valido";
    }

    private String obtenerValoresEnum(Class<?> enumClass) {

        return Arrays.stream(enumClass.getEnumConstants())
                .map(Object::toString)
                .collect(Collectors.joining(", "));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials() {

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("error", "Credenciales invalidas");
        response.put("mensaje", "Username o password incorrectos");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, Object>> handleDisabledUser() {

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("error", "Usuario desactivado");
        response.put("mensaje", "El usuario no esta activo");

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
}
