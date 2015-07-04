package fileSystemAnalyzer.processors;

import java.util.LinkedHashSet;
import java.util.Set;

public enum DataProcessorsAPI {

    INSTANCE;
    Set<Processors> pl = new LinkedHashSet<Processors>();

    Set<Processors> getDataProcessors() {
        pl.add(Duplicates.INSTANCE);
        return pl;
    }

    /**
     * register the providers
     */
    void setAnalyzerProviders() {

    }

    public void processData() {
        for (Processors p : getDataProcessors()) {
            ((Processors) p).analyzeItems();
        }
    }
}
