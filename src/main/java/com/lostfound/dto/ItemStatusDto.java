package com.lostfound.dto;

import com.lostfound.entity.Item.ItemStatus;
import jakarta.validation.constraints.NotNull;

public class ItemStatusDto {
	
	@NotNull(message = "Status is required")
	private ItemStatus status;

	public ItemStatusDto() {
		super();
	}

	public ItemStatus getStatus() {
		return status;
	}

	public void setStatus(ItemStatus status) {
		this.status = status;
	}
}

