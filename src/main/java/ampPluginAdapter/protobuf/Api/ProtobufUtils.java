package ampPluginAdapter.protobuf.Api;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ampPluginAdapter.KeyValuePair;
import ampPluginAdapter.protobuf.Api.LabelOuterClass.Label;
import ampPluginAdapter.protobuf.Api.LabelOuterClass.Label.Parameter;
import ampPluginAdapter.protobuf.Api.LabelOuterClass.Label.Parameter.Value;

public class ProtobufUtils {
	
	
	public static Label mkTypeLabel(String name, 
			String channel, 
			Label.LabelType labelType, 
			List<KeyValuePair<String,String>> parametersNamesAndTypes) {
		
		Label.Builder labBuilder = Label.newBuilder()
				.setLabel(name)
				.setType(labelType) ;
		
		if (channel != null)  {
			labBuilder.setChannel(channel) ;
		}
		
		for (KeyValuePair<String,String> o : parametersNamesAndTypes) {
			Value val = Value.newBuilder()
					.setString(o.val)
					.build() ;
			Parameter param = Parameter.newBuilder()
					.setName(o.key)
					.setValue(val)
					.build() ;
			labBuilder.addParameters(param) ;
		}
		
		return labBuilder.build() ;
	}
	
	public static Label mkStimulusTypeLabel(String name, String channel) {
		
		List<KeyValuePair<String,String>> params = new LinkedList<>() ;
		return mkStimulusTypeLabel(name,channel,params) ;	
	}
	
	public static Label mkStimulusTypeLabel(String name, 
			String channel, 
			KeyValuePair<String,String> ... parametersNamesAndTypes) {
		
		List<KeyValuePair<String,String>> params = new LinkedList<>() ;
		for (int k=0; k<parametersNamesAndTypes.length; k++) {
			params.add(parametersNamesAndTypes[k]) ;
		}
		return mkStimulusTypeLabel(name,channel,params) ;
		
	}
	
	public static Label mkStimulusTypeLabel(String name, 
			String channel, 
			List<KeyValuePair<String,String>> parametersNamesAndTypes) {
		return mkTypeLabel(name,channel,Label.LabelType.STIMULUS,parametersNamesAndTypes) ;
	}
	
	public static Label mkResponseTypeLabel(String name, 
			String channel, 
			KeyValuePair<String,String> ... parametersNamesAndTypes) {
		
		List<KeyValuePair<String,String>> params = new LinkedList<>() ;
		for (int k=0; k<parametersNamesAndTypes.length; k++) {
			params.add(parametersNamesAndTypes[k]) ;
		}
		return mkResponseTypeLabel(name,channel,params) ;
		
	}
	
	public static Label mkResponseTypeLabel(String name, 
			String channel, 
			List<KeyValuePair<String,String>> parametersNamesAndTypes) {
		return mkTypeLabel(name,channel,Label.LabelType.RESPONSE,parametersNamesAndTypes) ;
	}

}
