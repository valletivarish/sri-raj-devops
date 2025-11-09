package com.lostfound.dto;

import com.lostfound.entity.Item.ItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ItemDto {
	
	@NotBlank(message = "Title is required")
	@Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
	private String title;
	
	@Size(max = 2000, message = "Description must not exceed 2000 characters")
	private String description;
	
	@NotNull(message = "Type is required")
	private ItemType type;
	
	@Size(max = 500, message = "Tags must not exceed 500 characters")
	private String tags;
	
	@Size(max = 200, message = "Location must not exceed 200 characters")
	private String location;

	public ItemDto() {
		super();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title != null ? title.trim() : null;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description != null ? description.trim() : null;
	}

	public ItemType getType() {
		return type;
	}

	public void setType(ItemType type) {
		this.type = type;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags != null ? tags.trim() : null;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location != null ? location.trim() : null;
	}
}

