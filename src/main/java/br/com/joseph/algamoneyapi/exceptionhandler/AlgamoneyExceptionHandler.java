package br.com.joseph.algamoneyapi.exceptionhandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class AlgamoneyExceptionHandler extends ResponseEntityExceptionHandler {

	@Autowired
	private MessageSource messageSource;

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		String userMessage = messageSource.getMessage("mensagem.invalida", null, LocaleContextHolder.getLocale());
		String devMessage = Optional.ofNullable(ex.getCause()).orElse(ex).toString();
		List<Error> erros = Arrays.asList(new Error(userMessage, devMessage));

		return handleExceptionInternal(ex, erros, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		List<Error> erros = createListOfErrors(ex.getBindingResult());
		return handleExceptionInternal(ex, erros, headers, HttpStatus.BAD_REQUEST, request);
	}

	@ExceptionHandler({ EmptyResultDataAccessException.class })
	public ResponseEntity<Object> handleEmptyResultDataAccessException(EmptyResultDataAccessException ex, WebRequest request) {
		String userMessage = messageSource.getMessage("recurso.nao-encontrado", null, LocaleContextHolder.getLocale());
		String devMessage = ex.toString();
		List<Error> erros = Arrays.asList(new Error(userMessage, devMessage));

		return handleExceptionInternal(ex, erros, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
	}
	
	@ExceptionHandler({ DataIntegrityViolationException.class })
	public ResponseEntity<Object> handleDataIntegrityViolationException(DataIntegrityViolationException ex, WebRequest request) {
		String userMessage = messageSource.getMessage("recurso.operacao-nao-permitida", null, LocaleContextHolder.getLocale());
		String devMessage = ExceptionUtils.getRootCauseMessage(ex);
		List<Error> erros = Arrays.asList(new Error(userMessage, devMessage));

		return handleExceptionInternal(ex, erros, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
	}
	
	

	private List<Error> createListOfErrors(BindingResult bindingResult) {
		List<Error> errors = new ArrayList<>();

		for (FieldError fe : bindingResult.getFieldErrors()) {
			String userMessage = messageSource.getMessage(fe, LocaleContextHolder.getLocale());
			String devMessage = fe.toString();
			errors.add(new Error(userMessage, devMessage));
		}

		return errors;
	}

	public static class Error {
		private String userMessage;
		private String devMessage;

		public Error(String userMessage, String devMessage) {
			this.userMessage = userMessage;
			this.devMessage = devMessage;
		}

		public String getUserMessage() {
			return userMessage;
		}

		public String getDevMessage() {
			return devMessage;
		}
	}

}
