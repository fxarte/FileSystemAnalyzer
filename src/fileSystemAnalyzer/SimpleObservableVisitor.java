package fileSystemAnalyzer;

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


import fileSystemAnalyzer.analyzers.FileAnalyzer;

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
	public FileVisitResult visitFile(Path file, BasicFileAttributes arg1)
			throws IOException {
		/*
		long start_time = System.nanoTime();  
		// create a temp instance in memory for the file, if it is not too big
		//System.out.format("processing file %s with Tika:%n", file.getFileName().toString());
		try (final InputStream  input = new FileInputStream(file.toFile())) {
			Metadata metadata = new Metadata();
			BodyContentHandler handler = new BodyContentHandler();
			AutoDetectParser parser = new AutoDetectParser();
			String mimeType = new Tika().detect(input);
			metadata.set(Metadata.CONTENT_TYPE, mimeType);
			parser.parse(input, handler, metadata);
			
			for (FileAnalyzer da : pluggins) {
				//System.out.format("\t applying processor: %s%n", da.getClass().toString());
				da.analyzeItem(FileContext.valueOf(file, arg1, metadata, input));
			}
		}
		catch (Exception ex) {
			//System.out.format("tika failed (%s), using regurlar filestram reader%n", ex.getMessage(), file.getFileName().toString());
			for (FileAnalyzer da : pluggins) {
				//System.out.format("\t applying processor: %s%n", da.getClass().toString());
				da.analyzeItem(FileContext.valueOf(file, arg1, null));
			}
		}
		double d = (System.nanoTime() - start_time)/1e6;
		long s = arg1.size();
		//System.out.format("Time/size = (%s)/(%s) = (%s)%n", d, s, (d/s) );
		*/
		try (final InputStream  input = new FileInputStream(file.toFile())) {
			FileContext context = FileContext.valueOf(file, arg1, input);
			System.out.println(file.toFile());
			for (FileAnalyzer da : pluggins) {
				//System.out.format("\t applying processor: %s%n", da.getClass().toString());
				da.analyzeItem(context);
			}
		}
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
