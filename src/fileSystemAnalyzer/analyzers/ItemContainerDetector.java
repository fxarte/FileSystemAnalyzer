package fileSystemAnalyzer.analyzers;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;

import fileSystemAnalyzer.Analyzer;
import fileSystemAnalyzer.FileContext;
import fileSystemAnalyzer.DB.DBLayer;
import fileSystemAnalyzer.DB.DBSingleStorage;

/**
 * Container file types: zip (all compressed files), JPG, JAR, see other,
 * excluding Folders
 * 
 * @author felix
 * 
 */
public enum ItemContainerDetector implements FileAnalyzer, DBSingleStorage {
	INSTANCE;
	private final DBLayer DBInstance;
	private final Set<Class<? extends FileAnalyzer>> dependencies;

	private ItemContainerDetector() {
		DBInstance = DBLayer.getDbInstanceOf(ItemContainerDetector.class);
		dependencies = new LinkedHashSet<Class<? extends FileAnalyzer>>();
		dependencies.add(ItemCore.class);
	}

	Set<Class<? extends Analyzer>> getDependencies() {
		// TODO Auto-generated method stub
		Set<Class<? extends Analyzer>> defensiveCopyMap = new LinkedHashSet<Class<? extends Analyzer>>();
		defensiveCopyMap.addAll(dependencies);
		return defensiveCopyMap;
	}

	public void analyzeItem(FileContext context) {
		try {
			TikaConfig config = TikaConfig.getDefaultConfig();
			Detector detector = config.getDetector();
			TikaInputStream stream;
			if (context.getInMemoryFileContent() != null) {
				stream = TikaInputStream.get(context.getInMemoryFileContent());
			} else {
				stream = TikaInputStream.get(context.getPath().toFile());
			}

			Metadata metadata = new Metadata();

			// metadata.add(Metadata.RESOURCE_NAME_KEY,
			// context.getPath().toFile().toString());
			metadata.add(Metadata.MIME_TYPE_MAGIC, context.getPath().toFile()
					.toString());
			// http://stackoverflow.com/questions/7137634/getting-mimetype-subtype-with-apache-tika
			// producing the old fashion format
			// MediaType mediaType = detector.detect(stream, metadata);
			MediaType mediaType = detector.detect(stream, metadata);

			// org.apache.tika.detect.CompositeDetector

			// DBInstance.updateValue(context.getPath().toString(),context.getPath().getParent().toString(),
			// metadata.get(Metadata.CONTENT_TYPE));
			System.out.println(context.getPath().getFileName() + " Mime: "
					+ metadata.get(Metadata.CONTENT_TYPE));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void analyzeItems(FileContext context) {
		// TODO Auto-generated method stub

	}

}
