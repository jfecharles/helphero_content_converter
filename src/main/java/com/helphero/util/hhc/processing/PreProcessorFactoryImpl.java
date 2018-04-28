package com.helphero.util.hhc.processing;

/**
 * Pre-processor factory implementation class allowing the creation of Document specific PreProcessors.
 * @author jcharles
 *
 */
public class PreProcessorFactoryImpl implements IPreProcessorFactory {

	public PreProcessorFactoryImpl()  {
	}

	public IDocumentPreProcessor newInstance(SupportedDocType type) {
		IDocumentPreProcessor preProcessor = null;
		
		switch (type)
		{
			case DOCX:
			case DOTX:				
				preProcessor = new WordPreProcessor();
				break;
			case PPTX:
				preProcessor = new PowerPointPreProcessor();
				break;
			case XLSX:
				break;
			case PDF:
				break;
			case XHTML:
				break;			
			case XML:
				break;
			case NOT_SET:
				break;
			default:
				break;		
		}
		
		return preProcessor;
	}

}
