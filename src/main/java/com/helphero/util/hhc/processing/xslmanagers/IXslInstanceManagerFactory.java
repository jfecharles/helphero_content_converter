package com.helphero.util.hhc.processing.xslmanagers;

import com.helphero.util.hhc.processing.IDocumentPostProcessor;
import com.helphero.util.hhc.processing.SupportedOutputDocType;

public interface IXslInstanceManagerFactory {
	public abstract IXslInstanceManager newInstance(SupportedOutputDocType type);
}
