package com.helphero.util.hhc.processing.xslmanagers;

import com.helphero.util.hhc.processing.DocumentPostProcessor;
import com.helphero.util.hhc.processing.IDocumentPostProcessor;
import com.helphero.util.hhc.processing.SupportedOutputDocType;
import com.helphero.util.hhc.processing.postprocessors.HelpHeroPostProcessor;

public class XslInstanceManagerFactoryImpl implements
		IXslInstanceManagerFactory {

	public XslInstanceManagerFactoryImpl() {
	}

	public IXslInstanceManager newInstance(SupportedOutputDocType type) {
			IXslInstanceManager xslInstanceManager = null;
			
			switch (type)
			{
				case SPSV1:
					xslInstanceManager = new SupportPointXslInstanceManager();
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
					xslInstanceManager = new HelpHeroXslInstanceManager();
					break;
				case NOT_SET:
					break;
				default:
					break;		
			}
			
			return xslInstanceManager;
	}

}
