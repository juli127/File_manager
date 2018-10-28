package ua.itea.homework.filemanager.src;

import java.io.File;

public class Node {

	private File file;
	private int level;
	private String sign;
	
	public Node(File file, int level, String sign) {
		this.file = file;
		this.level = level;
		this.sign = sign;
		//System.out.println("new Node: " + file + ", " + sign);
	}
	
	public int getLevel() {
		return level;
	}
	
	public File getFile() {
		return file;
	}

	public String getSign() {
		return sign;
	}
	
	public void setSign(String sign) {
		this.sign = sign;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		result = prime * result + level;
		result = prime * result + ((sign == null) ? 0 : sign.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		if (level != other.level)
			return false;
		if (sign == null) {
			if (other.sign != null)
				return false;
		} else if (!sign.equals(other.sign))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[" + file.getPath() + ", level: " + level+ ", "+ sign + "]";
	}
	
	

}
