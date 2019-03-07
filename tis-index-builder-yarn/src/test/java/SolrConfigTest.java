import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.solr.core.SolrConfig;
import org.xml.sax.InputSource;

/**
 * 
 */


public class SolrConfigTest {

	public static void main(String[] args) throws Exception {

		FileInputStream inputStream = FileUtils.openInputStream(new File(
				"D:\\tmp\\solr-config-template.xml"));

		SolrConfig solrConfig = new SolrConfig("solrconfigtest",
				new InputSource(inputStream));

		System.out.println(solrConfig.luceneMatchVersion);

		IOUtils.closeQuietly(inputStream);
	}
}
