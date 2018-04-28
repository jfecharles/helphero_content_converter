package com.helphero.util.hhc.processing;

/**
 * Pre-processor factory interface to facilitate the creation of a new DocumentPreProcesor instance. 
 * @author jcharles
 *
 */
public interface IPreProcessorFactory {
	public abstract IDocumentPreProcessor newInstance(SupportedDocType type);
}
