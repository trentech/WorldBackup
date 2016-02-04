package com.gmail.trentech.worldbackup.utils;

import java.io.File;
import java.util.Comparator;

public class FileComparator implements Comparator<File> {

	public int compare(File f0, File f1) {
		long date1 = f0.lastModified();
		long date2 = f1.lastModified();

		if (date1 > date2)
			return 1;
		else if (date2 > date1)
			return -1;

		return 0;
	}
}
