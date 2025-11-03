package com.lostfound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ReportDto {
	
	@NotBlank(message = "Contact is required")
	@Size(min = 3, max = 100, message = "Contact must be between 3 and 100 characters")
	@Pattern(regexp = "^[a-zA-Z0-9@.\\s\\-+()]+$", message = "Contact contains invalid characters")
	private String reporterContact;
	
	@Size(max = 1000, message = "Reason must not exceed 1000 characters")
	private String reason;

	public ReportDto() {
		super();
	}

	public String getReporterContact() {
		return reporterContact;
	}

	public void setReporterContact(String reporterContact) {
		this.reporterContact = reporterContact != null ? reporterContact.trim() : null;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason != null ? reason.trim() : null;
	}
}

