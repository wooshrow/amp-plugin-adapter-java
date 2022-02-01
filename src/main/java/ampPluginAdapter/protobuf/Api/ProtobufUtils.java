package ampPluginAdapter.protobuf.Api;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ampPluginAdapter.KeyValuePair;
import ampPluginAdapter.protobuf.Api.LabelOuterClass.Label;
import ampPluginAdapter.protobuf.Api.LabelOuterClass.Label.Parameter;
import ampPluginAdapter.protobuf.Api.LabelOuterClass.Label.Parameter.Value;
import ampPluginAdapter.protobuf.Api.LabelOuterClass.Label.Parameter.Value.Array;

public class ProtobufUtils {
	
	
	
	public static Label mkTypeLabel(String name, 
			String channel, 
			Label.LabelType labelType, 
			List<KeyValuePair<String,String>> parametersNamesAndTypes) {
		
		return mkValueLabel(name,channel,labelType,parametersNamesAndTypes,null) ;
	}
	
	public static Label mkValueLabel(String name, 
			String channel, 
			Label.LabelType labelType, 
			List<KeyValuePair<String,String>> parametersNamesAndTypes,
			Map<String,Object> values) {
		
		Label.Builder labBuilder = Label.newBuilder()
				.setLabel(name)
				.setType(labelType) ;
		
		if (channel != null)  {
			labBuilder.setChannel(channel) ;
		}
		
		for (KeyValuePair<String,String> o : parametersNamesAndTypes) {
			Value.Builder valBuilder = Value.newBuilder() ;
			switch (o.val) {
			    case "integer" : 
			    	int x = 0 ;
			    	if(values != null) {
			    		x = (Integer) values.get(o.key) ;
			    	}
			    	valBuilder.setInteger(x) 
			    	; break ;
			    case "string"  : 
			    	String s = "" ;
			    	if(values != null) {
			    		s = (String) values.get(o.key) ;
			    	}
			    	valBuilder.setString(s) ; break ;
			    case "[integer]" : 
			    	int[] a = {0} ;
			    	if(values != null) {
			    		a = (int[]) values.get(o.key) ;
			    	}
			    	Array.Builder abuilder = Array.newBuilder() ;
			    	for (int k=0; k<a.length; k++) {
			    		Value elem = Value.newBuilder()
						    	   .setInteger(a[k])
						    	   .build() ;
			    		abuilder.addValues(elem) ;
			    	}
			    	valBuilder.setArray(abuilder.build()) 
			    	; break ;
			}
			Parameter param = Parameter.newBuilder()
					.setName(o.key)
					.setValue(valBuilder.build())
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
