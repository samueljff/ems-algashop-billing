package com.fonseca.algashop.billing.domain.model;

public class ErrorMessages {
    public static final String INVOICE_CANNOT_BE_MARKED_AS_PAID = "Invoice %s with status %s cannot be marked as paid";
    public static final String INVOICE_ALREADY_CANCELED = "Invoice %s is already canceled";
    public static final String INVOICE_CANNOT_BE_EDITED = "Invoice %s with status %s cannot be edited";
    public static final String INVOICE_ALREADY_EXISTS_FOR_ORDER = "Invoice already exists for order %s";
}
