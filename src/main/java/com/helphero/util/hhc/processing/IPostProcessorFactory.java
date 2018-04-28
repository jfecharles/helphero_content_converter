package com.helphero.util.hhc.processing;

public interface IPostProcessorFactory {
	public abstract IDocumentPostProcessor newInstance(SupportedOutputDocType type);
}
