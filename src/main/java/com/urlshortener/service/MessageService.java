package com.urlshortener.service;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    private final MessageSource messageSource;

    public MessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String code) {
        try {
            return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            return code;
        }
    }

    public String getMessage(String code, Object... args) {
        try {
            return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            return code;
        }
    }
}
