package com.tenforce.assignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

public class ReadEmail {

	// defines statistics constants
	final static List<Integer> setAVG = new ArrayList<Integer>();
	final static List<String> sendersTop = new ArrayList<String>();
	final static List<String> tagsTop5 = new ArrayList<String>();
	// defines triple constants
	final static Set<String> listEmails = new HashSet<String>();
	final static List<Email> allEmails = new ArrayList<Email>();

	public static void main(String[] args) {

		getEmailsBatchProcess();
		/*
		 * for (Email email : allEmails) { System.out.println(email.toString());
		 * }
		 */
	}

	public static void getEmailsBatchProcess() {

		Email email = null;
		Properties props = new Properties();
		props.setProperty("mail.store.protocol", "imaps");

		try {
			Session session = Session.getInstance(props, null);
			Store store = session.getStore();
			store.connect("imap.gmail.com", "maildude.tenforce@gmail.com", "TenforceS3cr3tZ");
			Folder inbox = store.getFolder("INBOX");
			inbox.open(Folder.READ_ONLY);
			for (Message msg : inbox.getMessages()) {

				Address[] in = msg.getFrom();

				if (msg.isMimeType("multipart/ALTERNATIVE")) {
					email = new Email();
					// get idmessage
					email.setId(msg.getMessageNumber());

					// get senders
					for (Address address : in) {
						listEmails.add(address.toString());
						String[] arr = address.toString().split("((\\<)|(\\>))");
						email.setFrom(arr[1]);
						sendersTop.add(arr[1]);
					}
					Multipart mp = (Multipart) msg.getContent();
					BodyPart bp = mp.getBodyPart(0);
					// get title
					email.setTitle(String.valueOf(msg.getSubject()));
					String str = String.valueOf(msg.getSubject());
					if (str.indexOf("[") >= 0) {
						tagsTop5.add(str.substring(str.indexOf("[") + 1, str.indexOf("]")));

					}
					List<String> s = new ArrayList<String>();
					boolean first = true;

					// get receivers
					for (Address addr : msg.getAllRecipients()) {
						listEmails.add(addr.toString());
						String[] arr2 = addr.toString().split("((\\<)|(\\>))");
						if (first) {
							// get first
							email.setName(arr2[0]);
						}
						s.add(String.valueOf(addr));
						first = false;
					}
					email.setTo(s);

					// get body
					email.setBody(String.valueOf(bp.getContent()));
					setAVG.add(Integer.valueOf(bp.getSize()));

					allEmails.add(email);
				} else {
					/*
					 * for (Address address : in) { System.out.println("FROM:" +
					 * address); }
					 * 
					 * System.out.
					 * println("************ IGNORED *****************");
					 * System.out.println(msg.getContent());
					 * System.out.println("*****************************\n");
					 */

				}

			}
			getStatistics();
			insertTriples(TypeTriple.USERS.name());
			insertTriples(TypeTriple.EMAILS.name());

		} catch (Exception mex) {
			mex.printStackTrace();
		}

	}

	public static void getStatistics() {

		System.out.println("************* TOP 5 SENDERS ****************");
		getTopFive(sendersTop, false);
		System.out.println("");

		System.out.println("************* AVG BODY SIZE ****************");
		System.out.println(setAVG.stream().mapToInt(a -> a).average().getAsDouble());
		System.out.println("");

		System.out.println("************* TOP TAGS TITLE ***************");
		getTopFive(tagsTop5, false);
		System.out.println("");

		System.out.println("************* NUM_EMAIL/NUM_ADDR **********");
		getTopFive(sendersTop, true);
		System.out.println("*******************************************\n");
		System.out.println("");
		System.out.println("");
		System.out.println("");

	}

	public static void getTopFive(List<String> list, boolean isEmailAddr) {
		List<String> distinctObjects = list.stream().distinct().collect(Collectors.toList());
		Map<String, Integer> unsortMap = new HashMap<>();
		for (String string : distinctObjects) {
			long numberOfOccurences = list.stream().filter(p -> p.equalsIgnoreCase(string)).count();
			unsortMap.put(string, (int) numberOfOccurences);

		}
		Map<String, Integer> result = new LinkedHashMap<>();

		unsortMap.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
				.forEachOrdered(x -> result.put(x.getKey(), x.getValue()));
		int count = 0;
		String key = "";
		Integer value = 0;
		for (Entry<String, Integer> entry : result.entrySet()) {

			key = entry.getKey();
			value = entry.getValue();
			// do stuff
			if (isEmailAddr) {
				System.out.println(key + " " + value);
			} else if (count <= 5) {
				System.out.println(key);
			}

			count++;
		}
	}

	public static void insertTriples(String type) {

		Repository rep = new SailRepository(new MemoryStore());
		rep.initialize();
		/*
		 * final String endPoint = "http://5.9.241.51:8890/sparql";
		 * SPARQLRepository rep = new SPARQLRepository(endPoint, endPoint);
		 * rep.initialize(); rep.setUsernameAndPassword("dba", "dba");
		 */
		try (RepositoryConnection conn = rep.getConnection()) {

			int i = 1;
			// insert users triples
			if (type.equalsIgnoreCase(TypeTriple.USERS.name())) {

				for (String sList : listEmails) {

					String[] arr = getObejctMAil(sList);
					String sMailName = arr[0];
					String sMail2 = arr[1];
					createUser(String.valueOf(i), sMailName, sMail2, rep, conn);
					i++;

				}
			}
			if (type.equalsIgnoreCase(TypeTriple.EMAILS.name())) {
				// insert emails triples

				for (Email email : allEmails) {
					createMail(String.valueOf(i), email.getTitle(), email.getBody(), email.getFrom(), email.getTo(),
							rep, conn);
					i++;
				}

			}
			System.out.println("************* INSERT TRTRIPLES ****************");
			RepositoryResult<Statement> statements = conn.getStatements(null, null, null);
			Model model = QueryResults.asModel(statements);
			if (type.equalsIgnoreCase(TypeTriple.USERS.name())) {
				model.setNamespace(FOAF.PREFIX, FOAF.NAMESPACE);
			} else {
				model.setNamespace(TPMAIL.PREFIX, TPMAIL.NAMESPACE);
			}
			Rio.write(model, System.out, RDFFormat.TURTLE);
			conn.close();
			System.out.println("*******************************************\n");
		}

	}

	private static void createUser(String id, String name, String email, Repository repository,
			RepositoryConnection conn) {

		ValueFactory vf = repository.getValueFactory();
		IRI subj = vf.createIRI("http://tenforce.example.com/user/", id);
		conn.add(subj, RDF.TYPE, FOAF.PERSON);
		conn.add(subj, FOAF.NAME, vf.createLiteral(name));
		conn.add(subj, FOAF.MBOX, vf.createIRI("mailto:", email));

	}

	private static void createMail(String id, String title, String body, String from, List<String> mailto,
			Repository repository, RepositoryConnection conn) {
		ValueFactory vf = repository.getValueFactory();
		IRI mail = vf.createIRI("http://tenforce.example.com/email/", id);
		conn.setNamespace(TPMAIL.PREFIX, TPMAIL.NAMESPACE);
		conn.add(mail, RDF.TYPE, TPMAIL.MAIL);
		conn.add(mail, TPMAIL.TITLE, vf.createLiteral(title));
		conn.add(mail, TPMAIL.BODY, vf.createLiteral(body));
		conn.add(mail, TPMAIL.FROM, vf.createLiteral(from));

		for (String to : mailto) {
			// String m = DefRDF4J.findBySubjectValue("0",
			// getObejctMAil(to)[1]);

			conn.add(mail, TPMAIL.TO, vf.createLiteral(to));
		}

	}

	private static String[] getObejctMAil(String str) {

		String[] arr = new String[2];
		String[] arr2 = new String[2];
		arr = str.split("((\\<)|(\\>))");
		if (arr.length == 1) {
			arr2[1] = arr[0];
			arr2[0] = "";
			return arr2;
		}

		return arr;

	}

}
