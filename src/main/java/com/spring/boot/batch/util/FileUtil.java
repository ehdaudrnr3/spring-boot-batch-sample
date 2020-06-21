package com.spring.boot.batch.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FileUtil {
	
	public static boolean moveDirectory(Path src, Path dest) {
		if (src.toFile().isDirectory()) {
			for (File file : src.toFile().listFiles()) {
				moveDirectory(file.toPath(), dest.resolve(src.relativize(file.toPath())));
			}
		}

		try {
			Files.move(src, dest, StandardCopyOption.REPLACE_EXISTING);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
}
