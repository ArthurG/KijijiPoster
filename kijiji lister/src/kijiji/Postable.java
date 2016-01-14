package kijiji;

import java.util.ArrayList;
import java.util.List;

public class Postable {

	ArrayList<String> details;
	ArrayList<String> photos;
	
	public Postable(ArrayList<String>  details, ArrayList<String>   photos) {
		super();
		this.details = details;
		this.photos = photos;
	}
	
	public String getTitle() {
		return details.get(5);
	}

	public String getEmail() {
		return details.get(0);
	}

	public String getPass() {
		return details.get(1);
	}

	public void setTitle(String replace) {
		details.set(5,replace);
	}

	public String getPrice() {
		return details.get(2);
	}

	public String getDropdown1() {
		return details.get(3);
	}

	public String getDropdown2() {
		return details.get(4);
	}

	public String getDescription() {
		return details.get(6);
	}

	public boolean hasPhotos() {
		return photos.size() != 0;
	}

	public ArrayList<String> getPhotos() {
		return photos;
	}

	public String getCategory1() {
		return details.get(7);
	}

	public String getCategory2() {
		return details.get(8);
	}
	
}
