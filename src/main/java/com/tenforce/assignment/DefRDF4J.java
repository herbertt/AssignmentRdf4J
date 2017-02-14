package com.tenforce.assignment;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

public class DefRDF4J {

	public static void main(String[] args) {

		List<String[]> emails = getAll();
		for (String[] email : emails) {
			System.out.println(email[0]);
			System.out.println(email[1]);
			System.out.println(email[2]);
		}

	}

	public static String findBySubjectValue(String st, String value) {

		final String endPoint = "http://5.9.241.51:8890/sparql";
		RepositoryConnection connection = null;
		final String query = "select * where { ?s ?p ?o .} limit 100 ";
		String res = "";
		SPARQLRepository repository = new SPARQLRepository(endPoint, endPoint);
		repository.initialize();
		repository.setUsernameAndPassword("dba", "dba");
		/*
		 * Repository repository = new SailRepository(new MemoryStore());
		 * RepositoryConnection connection = null; repository.initialize();
		 */
		try {
			connection = repository.getConnection();

			TupleQueryResult result = connection.prepareTupleQuery(QueryLanguage.SPARQL, query).evaluate();
			try {
				while (result.hasNext()) {

					BindingSet bindingSet = result.next();

					Value valueOfX = bindingSet.getValue("s");
					Value valueOfY = bindingSet.getValue("p");
					Value valueOfZ = bindingSet.getValue("o");

					if (st.equals("s") && valueOfX.stringValue().contains(value)) {
						res = String.valueOf(valueOfX);
					}
					/*
					 * if (st.equals("p") &&
					 * valueOfY.stringValue().contains(value)) { res =
					 * String.valueOf(valueOfX); } if (st.equals("o") &&
					 * valueOfZ.stringValue().contains(value)) { res =
					 * String.valueOf(valueOfX); }
					 */
				}
			}

			finally {

				result.close();

			}

		} finally {

			connection.close();

		}

		return res;
	}

	private static List<String[]> getAll() {

		String[] triples;
		List<String[]> res = new ArrayList<String[]>();
		final String endPoint = "http://5.9.241.51:8890/sparql";
		RepositoryConnection connection = null;
		final String query = "select * where { ?s ?p ?o .} limit 100 ";

		SPARQLRepository repository = new SPARQLRepository(endPoint, endPoint);
		repository.initialize();
		repository.setUsernameAndPassword("dba", "dba");

		// Repository repository = new SailRepository(new MemoryStore());
		// RepositoryConnection connection = null;
		// repository.initialize();
		try {
			connection = repository.getConnection();

			TupleQueryResult result = connection.prepareTupleQuery(QueryLanguage.SPARQL, query).evaluate();
			try {
				while (result.hasNext()) {

					BindingSet bindingSet = result.next();

					Value valueOfX = bindingSet.getValue("s");
					Value valueOfY = bindingSet.getValue("p");
					Value valueOfZ = bindingSet.getValue("o");

					triples = new String[3];
					triples[0] = String.valueOf(valueOfX);
					triples[1] = String.valueOf(valueOfY);
					triples[2] = String.valueOf(valueOfZ);
					res.add(triples);

				}
			}

			finally {

				result.close();

			}

		} finally {

			connection.close();

		}

		return res;
	}
}
