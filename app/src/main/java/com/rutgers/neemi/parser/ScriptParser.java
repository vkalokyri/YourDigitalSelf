package com.rutgers.neemi.parser;

import android.util.Xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.rutgers.neemi.util.ConfigReader;
import com.rutgers.neemi.util.PROPERTIES;

public class ScriptParser {


	public ScriptParser(){

		String filename = InitiateScript.config.getStr(PROPERTIES.SCRIPT_FILE);
		System.out.println(filename);
		System.out.println(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
		InputStream fis = this.getClass().getResourceAsStream("eatingOut.xml");
		try {
			parse(fis);
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static final String ns = null;

	public List parse(InputStream in) throws XmlPullParserException, IOException {
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			return readFeed(parser);
		} finally {
			in.close();
		}
	}


	private List readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
		List entries = new ArrayList();

		parser.require(XmlPullParser.START_TAG, ns, "definitions");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			System.out.println(name);
//			// Starts by looking for the entry tag
//			if (name.equals("entry")) {
//				entries.add(readEntry(parser));
//			} else {
//				skip(parser);
//			}
		}
		return entries;
	}


	public static void main(String[] args) {
		new ScriptParser();

	}



//	static Map <String, JAXBElement> scriptElements;
//
//	public ScriptParser() throws JAXBException{
//		this.scriptElements=new HashMap<String, JAXBElement>();
//		this.extractProcess(InitiateScript.config.getStr(PROPERTIES.SCRIPT_FILE));
//
//	}
//
//	public static void main(String[] args) throws JAXBException {
//
//		ScriptParser sp = new ScriptParser();
//	}
//
//
//	public List<String> getAllScriptTaskNames(){
//		List<String> taskNames = new ArrayList<String>();
//		for (Entry<String, JAXBElement> entry : scriptElements.entrySet()) {
//			if (entry.getValue().getValue() instanceof TTask){
//				taskNames.add(entry.getKey());
//			}
//		}
//		return taskNames;
//
//	}
//
	public List<Object> extractLocalsFromProcess(String elementId) {

		return null;
	}
//
//		System.out.println(elementId);
//
//		 //for (Entry<String, JAXBElement> entry : scriptElements.entrySet()) {
//		//	 System.out.println("hereee= "+entry.getKey());
//		 //}
//
//		JAXBElement element = scriptElements.get(elementId);
//		if (element!=null){
//			//System.out.println(elementId+" is not null");
//			if (element.getValue() instanceof TProcess){
//				List<JAXBElement<? extends TFlowElement>> processElements = ((TProcess)element.getValue()).getFlowElement();
//				for(JAXBElement processElement:processElements){
//					if (processElement.getValue() instanceof TLocals){
//						List<Object> locals = ((TLocals)processElement.getValue()).getWhoOrWhatOrWhere();
//						return locals;
//					}
//				}
//			}else if (element.getValue() instanceof TTask){
//				List<TLocals> locals = ((TTask)element.getValue()).getLocals();
//				for(TLocals w5h:locals){
//					List<Object> w5hSublocals = w5h.getWhoOrWhatOrWhere();
//					return w5hSublocals;
//				}
//			}
//		}else{
//			System.out.println("The element:"+elementId+" doesn't exist!");
//		}
//		return null;
//	}
//
//	public void extractProcess(String filename) throws JAXBException{
//		System.out.println(filename);
//		InputStream fis = this.getClass().getResourceAsStream("/resources/"+filename);
//
//		JAXBContext jc = JAXBContext.newInstance("org.omg.spec.bpmn._20100524.model");
//		Unmarshaller unmarshaller = jc.createUnmarshaller();
//
//		InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
//
//		Source source = new StreamSource(isr);
//
//		JAXBElement<TDefinitions> root = unmarshaller.unmarshal(source, TDefinitions.class);
//		TDefinitions definitions = root.getValue();
//
//		for (int i=0;i<((ArrayList)definitions.getRootElement()).size();i++){
//			System.out.println(((JAXBElement) ((ArrayList) definitions.getRootElement()).get(i)).getValue());
//			JAXBElement element = (JAXBElement) ((ArrayList)definitions.getRootElement()).get(i);
//			if (element.getValue() instanceof TProcess){
//				scriptElements.put(((TProcess)element.getValue()).getId(), element);
//				List<JAXBElement<? extends TFlowElement>> processElements = ((TProcess)element.getValue()).getFlowElement();
//				for(JAXBElement processElement:processElements){
//					System.out.println("Check--->"+processElement.getValue());
//					if (processElement.getValue() instanceof TCallActivity){
//						QName calledActivity = ((TCallActivity)processElement.getValue()).getCalledElement();
//						System.out.println(calledActivity.getLocalPart());
//						extractProcess(calledActivity.getLocalPart()+".xml");
//					}
//
//					if (processElement.getValue() instanceof TTask){
//						scriptElements.put(((TTask)processElement.getValue()).getId(), processElement);
//						System.out.println("It's a task--->"+((TTask)processElement.getValue()).getId());
//						extractLocalsFromProcess(((TTask)processElement.getValue()).getId());
//
//					}
//
//					if (processElement.getValue() instanceof TLocals){
//						List<Object> locals = ((TLocals)processElement.getValue()).getWhoOrWhatOrWhere();
//						for(Object w5h:locals){
//							if (w5h instanceof Who){
//								System.out.println(((Who)w5h).getName());
//							}
//						}
//					}
//				}
//			}
//		}
//	}

}
