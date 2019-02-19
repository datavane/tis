/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.solrextend.cloud;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.cloud.ZkController;
import org.apache.solr.cloud.ZkSolrResourceLoader;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.core.SolrResourceNotFoundException;
import org.apache.solr.handler.IndexFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qlangtech.tis.common.utils.TSearcherConfigFetcher;
import com.qlangtech.tis.manage.biz.dal.pojo.UploadResource;
import com.qlangtech.tis.manage.common.ConfigFileReader;
import com.qlangtech.tis.manage.common.HttpConfigFileReader;
import com.qlangtech.tis.manage.common.PropteryGetter;
import com.qlangtech.tis.manage.common.SnapshotDomain;
import com.qlangtech.tis.manage.common.TerminatorRepositoryException;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisSolrResourceLoader extends ZkSolrResourceLoader {

	private final String collectionName;

	public static final Map<String, PropteryGetter> configFileNames;

	private static final Logger log = LoggerFactory.getLogger(TisSolrResourceLoader.class);

	static {
		Map<String, PropteryGetter> names = new HashMap<String, PropteryGetter>();
		for (PropteryGetter getter : ConfigFileReader.getConfigList()) {
			names.put(getter.getFileName(), getter);
		}
		configFileNames = Collections.unmodifiableMap(names);
	}

	public TisSolrResourceLoader(Path instanceDir, String configSet, ClassLoader parent //
			, Properties coreProperties, ZkController zooKeeperController, String collection) {
		super(instanceDir, configSet, parent, coreProperties, zooKeeperController);
		this.collectionName = collection;
	}

	@Override
	public String getConfigDir() {
		// throw new UnsupportedOperationException();
		return "repository:" + TSearcherConfigFetcher.get().getTerminatorConsoleHostAddress();
	}

	@Override
	public String[] listConfigDir() {
		return new String[] {};
	}

	@Override
	public InputStream openResource(String resource) throws IOException {
		// 希望在服务端不需要校验schema的正确性
		if (StringUtils.equals("dtd/solrschema.dtd", resource)) {
			return new ByteArrayInputStream(new byte[0]);
		}
		PropteryGetter getter = null;
		if ((getter = configFileNames.get(resource)) != null) {
			return loadConfigResource(getter,
					getConfigSnapshotId(getCollectionConfigDir(this.getInstancePath(), this.collectionName)));
		}
		InputStream is = null;
		try {
			// delegate to the class loader (looking into $INSTANCE_DIR/lib
			// jars)
			is = classLoader.getResourceAsStream(resource.replace(File.separatorChar, '/'));
		} catch (Exception e) {
			throw new IOException("Error opening " + resource, e);
		}
		if (is == null) {
			throw new SolrResourceNotFoundException("Can't find resource '" + resource + "' in classpath or '"
					+ collectionName + "', cwd=" + System.getProperty("user.dir"));
		}
		return is;
	}

	/**
	 * 下载配置资源，如果本地已经有这个配置文件，那直接从本地加载就行了<br>
	 * 如果本地没有文件，那就要从远端下载下来
	 *
	 * @param configFetcher
	 * @param snapshotid
	 *            ，期望
	 * @return
	 * @throws IOException
	 * @throws SolrResourceNotFoundException
	 */
	private InputStream loadConfigResource(PropteryGetter getter, final long snapshotid)
			throws IOException, SolrResourceNotFoundException {
		TSearcherConfigFetcher configFetcher = TSearcherConfigFetcher.get();
		synchronized (TisSolrResourceLoader.class) {
			File collectionConfigDir = getCollectionConfigDir(this.getInstancePath(), this.collectionName);
			File configFile = null;
			try {
				configFile = new File(collectionConfigDir, getter.getFileName() + snapshotid);
				if (snapshotid > 0 && configFile.exists()) {
					return FileUtils.openInputStream(configFile);
				}
			} catch (IOException e) {
				throw new SolrException(ErrorCode.SERVER_ERROR, "can not find configfile:" + getter.getFileName()
						+ snapshotid + " in " + collectionConfigDir.getAbsolutePath(), e);
			}
			try {
				SnapshotDomain snapshotDomain = downConfigFromConsoleRepository(snapshotid, this.collectionName,
						collectionConfigDir, configFileNames.values().toArray(new PropteryGetter[0]), true);
				// 返回需要的文件
				return new ByteArrayInputStream(getter.getContent(snapshotDomain));
			} catch (TerminatorRepositoryException e1) {
				throw new SolrResourceNotFoundException("repository:" + configFetcher.getTerminatorConsoleHostAddress()
						+ ",collection:" + collectionName, e1);
			}
		}
	}

	/**
	 * 取得远端仓库中的snapshotid
	 *
	 * @param collectionName
	 * @return
	 * @throws Exception
	 */
	public static int getRemoteSnapshotId(String collectionName) throws Exception {
		SnapshotDomain snapshotDomain = downConfigFromConsoleRepository(-1, collectionName, null,
				new PropteryGetter[] { ConfigFileReader.FILE_SOLOR }, false);
		return snapshotDomain.getSnapshot().getSnId();
	}

	public static void main(String[] args) throws Exception {
		System.out.println(getRemoteSnapshotId("search4totalpay"));
	}

	/**
	 * @param targetSnapshotid
	 *            ,配置文件目标版本，期望得到的版本
	 * @param configFetcher
	 * @param collectionConfigDir
	 * @return
	 * @throws TerminatorRepositoryException
	 * @throws IOException
	 */
	public static SnapshotDomain downConfigFromConsoleRepository(final long targetSnapshotid, String collectionName,
			File collectionConfigDir, PropteryGetter[] fileGetter, boolean modifyFileSys)
			throws TerminatorRepositoryException, IOException {
		TSearcherConfigFetcher configFetcher = TSearcherConfigFetcher.get();
		SnapshotDomain snapshotDomain = HttpConfigFileReader.getResource(
				configFetcher.getTerminatorConsoleHostAddress(), collectionName, targetSnapshotid,
				configFetcher.getRuntime(), fileGetter);
		// 期望下载的 和远端的配置文件版本不同则肯定有问题了，需要抛出异常
		if (targetSnapshotid > 0 && snapshotDomain.getSnapshot().getSnId() != targetSnapshotid) {
			throw new SolrException(ErrorCode.SERVER_ERROR, "local config snapshotid:" + targetSnapshotid
					+ ",config repository snapshotid:" + snapshotDomain.getSnapshot().getSnId() + " is not match");
		}
		if (// && localSnapshotid < 1
		modifyFileSys) {
			// 修改配置文件
			saveConfigFileSnapshotId(collectionConfigDir, snapshotDomain.getSnapshot().getSnId());
		}
		// 将文件存入本地
		UploadResource content = null;
		if (modifyFileSys) {
			for (PropteryGetter g : fileGetter) {
				content = g.getUploadResource(snapshotDomain);
				if (content != null && content.getContent() != null) {
					File newFile = new File(collectionConfigDir,
							g.getFileName() + snapshotDomain.getSnapshot().getSnId());
					// 只有不存在的情况下才会去更新
					if (!newFile.exists()) {
						FileUtils.writeByteArrayToFile(newFile, content.getContent(), false);
					}
				}
			}
		}
		return snapshotDomain;
	}

	/**
	 * @return
	 */
	public static File getCollectionConfigDir(Path instanceDir, String collectionName) {
		File parent = instanceDir.toFile().getParentFile();
		File collectionConfigDir = new File(parent, "configsets" + File.separator + collectionName);
		return collectionConfigDir;
	}

	private static final String configsnapshotid = "configsnapshotid";

	public static final String CONFIG_FILE_NAME = "config.properties";

	/**
	 * If the index is stale by any chance, load index from a different dir in
	 * the data dir.
	 */
	public static void saveConfigFileSnapshotId(File collectionConfigDir, long snapshotId) {
		log.info("New index installed. Updating index properties... config snapshotid=" + snapshotId);
		File configFile = new File(collectionConfigDir, CONFIG_FILE_NAME);
		Properties p = new Properties();
		try {
			if (configFile.exists()) {
				final InputStream is = FileUtils.openInputStream(configFile);
				try {
					p.load(new InputStreamReader(is, StandardCharsets.UTF_8));
				} catch (Exception e) {
					log.error("Unable to load " + IndexFetcher.INDEX_PROPERTIES, e);
				} finally {
					IOUtils.closeQuietly(is);
				}
				try {
					// dir.deleteFile(IndexFetcher.INDEX_PROPERTIES);
					FileUtils.forceDelete(configFile);
				} catch (IOException e) {
					// no problem
				}
			}
			p.put(configsnapshotid, String.valueOf(snapshotId));
			Writer os = null;
			try {
				os = new OutputStreamWriter(FileUtils.openOutputStream(configFile, false), StandardCharsets.UTF_8);
				p.store(os, IndexFetcher.INDEX_PROPERTIES);
			} catch (Exception e) {
				throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
						"Unable to write " + IndexFetcher.INDEX_PROPERTIES, e);
			} finally {
				IOUtils.closeQuietly(os);
			}
			// return true;
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		} finally {
		}
	}

	/**
	 * 取得现在目录下的配置文件版本
	 *
	 * @param collectionConfigDir
	 * @return
	 */
	public static int getConfigSnapshotId(final File collectionConfigDir) {
		// 先要尝试到本地目录中去找文件
		if (!collectionConfigDir.exists()) {
			collectionConfigDir.mkdirs();
			return 0;
		}
		File propFile = new File(collectionConfigDir, CONFIG_FILE_NAME);
		if (!propFile.exists()) {
			return 0;
		}
		Properties p = new Properties();
		try (final InputStream is = FileUtils.openInputStream(propFile)) {
			p.load(new InputStreamReader(is, StandardCharsets.UTF_8));
			return Integer.parseInt(p.getProperty(configsnapshotid));
		} catch (Exception e) {
			log.error("Unable to load " + IndexFetcher.INDEX_PROPERTIES, e);
		}
		return 0;
	}
	// /**
	// * Returns true if the file exists (can be opened), false if it cannot be
	// * opened, and (unlike Java's File.exists) throws IOException if there's
	// * some unexpected error.
	// */
	// private static boolean slowFileExists(Directory dir, String fileName)
	// throws IOException {
	// try {
	// dir.openInput(fileName, IOContext.DEFAULT).close();
	// return true;
	// } catch (NoSuchFileException | FileNotFoundException e) {
	// return false;
	// }
	// }
}
