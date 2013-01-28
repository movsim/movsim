package org.movsim.network;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.opendrive.jaxb.ObjectFactory;
import org.opendrive.jaxb.OpenDRIVE;
import org.opendrive.jaxb.OpenDRIVE.Header;

public class NetworkUnMarshaller {

	
	public void marshall() {
		try {
			org.opendrive.jaxb.ObjectFactory of = new ObjectFactory();
			Header createOpenDRIVEHeader = of.createOpenDRIVEHeader();
			JAXBContext jaxbContext = JAXBContext.newInstance("org.opendrive.jaxb");
			JAXBContext jc = JAXBContext.newInstance("hello");
			Marshaller m = jc.createMarshaller();
			m.marshal(createOpenDRIVEHeader, System.out);
		} catch (JAXBException jbe) {
			// ...
		}
	}

	public void unMarshall(File xmlDocument) {
		try {

			ClassLoader cl = org.opendrive.jaxb.ObjectFactory.class
					.getClassLoader();
			// JAXBContext jc = JAXBContext.newInstance("my.package.name", cl);

			JAXBContext jaxbContext = JAXBContext.newInstance(
					"org.opendrive.jaxb", cl);

			Unmarshaller unMarshaller = jaxbContext.createUnmarshaller();
			
			System.out.println(unMarshaller.getSchema());
			Object unmarshal = unMarshaller.unmarshal(xmlDocument);
			System.out.println(unmarshal);
//			OpenDRIVE openDriveNetwork = openDriveJAXBElement.getValue();

			// System.out.println("Section: " + catalog.getSection());
			// System.out.println("Publisher: " + catalog.getPublisher());
			// List<JournalType> journalList = catalog.getJournal();
			// for (int i = 0; i < journalList.size(); i++) {
			//
			// JournalType journal = (JournalType) journalList.get(i);
			//
			// List<ArticleType> articleList = journal.getArticle();
			// for (int j = 0; j < articleList.size(); j++) {
			// ArticleType article = (ArticleType)articleList.get(j);
			//
			// System.out.println("Article Date: " + article.getDate());
			// System.out.println("Level: " + article.getLevel());
			// System.out.println("Title: " + article.getTitle());
			// System.out.println("Author: " + article.getAuthor());
			//
			// }
			// }
		} catch (JAXBException e) {
			System.out.println(e.toString());
		}

	}
}
