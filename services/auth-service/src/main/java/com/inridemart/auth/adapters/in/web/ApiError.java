package com.inridemart.auth.adapters.in.web;

import java.time.Instant;
import java.util.List;

public record ApiError(Instant timestamp, int status, String error, List<String> details) {
}

