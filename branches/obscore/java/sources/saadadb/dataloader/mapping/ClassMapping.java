package saadadb.dataloader.mapping;

import saadadb.command.ArgsParser;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;

public class ClassMapping {
	private ClassifierMode classifier;
	private String className;
	
	ClassMapping(ArgsParser ap) throws SaadaException {
		this.className = ap.getClassName();

		if( this.className == null ) {
			this.className = "";
		}
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Setting the mapping type for class " + this.className);
		this.classifier = (ap.getMappingType() == ClassifierMode.NOCLASSIFICATION)? ClassifierMode.CLASSIFIER: ap.getMappingType();
	}

	public ClassifierMode getClassifier() {
		return classifier;
	}

	public String getClassName() {
		return className;
	}
	
	public String toString() {
		return classifier + " " + className;
	}
}
