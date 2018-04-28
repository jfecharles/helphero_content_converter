package com.helphero.util.hhc.processing;

import com.helphero.util.hhc.processing.postprocessors.HelpHeroPostProcessor;

public class PostProcessorFactoryImpl implements IPostProcessorFactory {

	public PostProcessorFactoryImpl() {
	}
	
	public IDocumentPostProcessor newInstance(SupportedOutputDocType type) {
		IDocumentPostProcessor postProcessor = null;
		
		switch (type)
		{
			case SPSV1:
				postProcessor = new DocumentPostProcessor();
				break;
			case DOCX:
				break;
			case PDF:
				break;
			case XHTML:
				break;			
			case XML:
				break;
			case HHV1:
				postProcessor = new HelpHeroPostProcessor();
				break;
			case NOT_SET:
				break;
			default:
				break;		
		}
		
		return postProcessor;
	}
}
