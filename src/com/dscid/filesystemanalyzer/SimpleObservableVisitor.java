package com.dscid.filesystemanalyzer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Set;


import com.dscid.filesystemanalyzer.analyzers.FileAnalyzer;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class SimpleObservableVisitor extends Observable implements
		FileVisitor<Path> {
	
	private static List<FileAnalyzer> pluggins = new ArrayList<FileAnalyzer>();

	// @Override
	public FileVisitResult postVisitDirectory(Path dir, IOException arg1)
			throws IOException {
		BasicFileAttributes attrs  = Files.readAttributes(dir, BasicFileAttributes.class);
		System.out.println(dir.toString());
		FileContext context = FileContext.valueOf(dir, attrs);
		for (FileAnalyzer da : pluggins) {
			da.analyzeItems(context);
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes arg1)
			throws IOException {
          //TODO: to restart a process.
          //Check here if the process item is already in the  plugins then continue to the next (SKIP?)
		// register(dir);
                if (!Files.isReadable(dir)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes arg1) throws IOException {
        FileContext context = null;
        System.out.println(file.toFile());
        if (arg1.isSymbolicLink()) {
            String syml = Files.readSymbolicLink(file).toString();
            InputStream stream = new ByteArrayInputStream(syml.getBytes(StandardCharsets.UTF_8));
            context = FileContext.valueOf(file, arg1, stream);
        } else {
            InputStream input = new FileInputStream(file.toFile());
            context = FileContext.valueOf(file, arg1, input);
        }
        for (FileAnalyzer da : pluggins) {
            //System.out.format("\t applying processor: %s%n", da.getClass().toString());
            da.analyzeItem(context);
        }
        context.closeResource();
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException arg1)
			throws IOException {
		// TODO Auto-generated method stub
            System.err.println(arg1);
		return FileVisitResult.CONTINUE;
	}

	public static void setPluggings(FileAnalyzer pluggin) {
		if (pluggins == null) {
			pluggins = new ArrayList<FileAnalyzer>();
		}
		pluggins.add(pluggin);
	}

	public static void setPluggings(Set<FileAnalyzer> pl) {
		if (pl == null) {
			throw new IllegalArgumentException();
		}
		pluggins.addAll(pl);
	}

}