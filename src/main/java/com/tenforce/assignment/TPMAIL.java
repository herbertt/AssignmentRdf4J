package com.tenforce.assignment;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class TPMAIL {

	public static final String NAMESPACE = "http://tenforce.example.com/email/";

	public static final String PREFIX = "mail";

	public static final IRI MAIL = getIRI("Mail");

	public static final IRI TITLE = getIRI("title");

	public static final IRI BODY = getIRI("body");

	public static final IRI FROM = getIRI("from");

	public static final IRI TO = getIRI("to");

	private static IRI getIRI(String localName) {
		return SimpleValueFactory.getInstance().createIRI(NAMESPACE, localName);
	}

}
