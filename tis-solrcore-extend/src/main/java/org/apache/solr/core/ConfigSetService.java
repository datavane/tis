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
package org.apache.solr.core;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import org.apache.solr.cloud.ZkController;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.IndexSchemaFactory;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qlangtech.tis.solrextend.cloud.TisConfigSetService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/*
 * Service class used by the CoreContainer to load ConfigSets for use in
 * SolrCore creation.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class ConfigSetService {

	public static ConfigSetService createConfigSetService(NodeConfig nodeConfig, SolrResourceLoader loader,
			ZkController zkController) {
		if ("true".equalsIgnoreCase(System.getProperty("config.load.local"))) {
			return new Default(loader, nodeConfig.getConfigSetBaseDirectory());
		}
		// 百岁修改，记得要将solr依赖包中的solr core中对应的这个类要删除掉
		TisConfigSetService configSetService = new TisConfigSetService(loader, zkController);
		return configSetService;
	}

	protected final SolrResourceLoader parentLoader;

	/**
	 * Create a new ConfigSetService
	 *
	 * @param loader
	 *            the CoreContainer's resource loader
	 */
	public ConfigSetService(SolrResourceLoader loader) {
		this.parentLoader = loader;
	}

	/**
	 * Load the ConfigSet for a core
	 *
	 * @param dcore
	 *            the core's CoreDescriptor
	 * @return a ConfigSet
	 */
	public final ConfigSet getConfig(CoreDescriptor dcore) {
		SolrResourceLoader coreLoader = createCoreResourceLoader(dcore);
		try {
			SolrConfig solrConfig = createSolrConfig(dcore, coreLoader);
			IndexSchema schema = createIndexSchema(dcore, solrConfig);
			NamedList properties = createConfigSetProperties(dcore, coreLoader);
			return new ConfigSet(configName(dcore), solrConfig, schema, properties, true/* trusted */);
		} catch (Exception e) {
			throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
					"Could not load conf for core " + dcore.getName() + ": " + e.getMessage(), e);
		}
	}

	/**
	 * Create a SolrConfig object for a core
	 *
	 * @param cd
	 *            the core's CoreDescriptor
	 * @param loader
	 *            the core's resource loader
	 * @return a SolrConfig object
	 */
	protected SolrConfig createSolrConfig(CoreDescriptor cd, SolrResourceLoader loader) {
		return SolrConfig.readFromResourceLoader(loader, cd.getConfigName());
	}

	/**
	 * Create an IndexSchema object for a core
	 *
	 * @param cd
	 *            the core's CoreDescriptor
	 * @param solrConfig
	 *            the core's SolrConfig
	 * @return an IndexSchema
	 */
	protected IndexSchema createIndexSchema(CoreDescriptor cd, SolrConfig solrConfig) {
		return IndexSchemaFactory.buildIndexSchema(cd.getSchemaName(), solrConfig);
	}

	/**
	 * Return the ConfigSet properties
	 *
	 * @param cd
	 *            the core's CoreDescriptor
	 * @param loader
	 *            the core's resource loader
	 * @return the ConfigSet properties
	 */
	protected NamedList createConfigSetProperties(CoreDescriptor cd, SolrResourceLoader loader) {
		return ConfigSetProperties.readFromResourceLoader(loader, cd.getConfigSetPropertiesName());
	}

	/**
	 * Create a SolrResourceLoader for a core
	 *
	 * @param cd
	 *            the core's CoreDescriptor
	 * @return a SolrResourceLoader
	 */
	protected abstract SolrResourceLoader createCoreResourceLoader(CoreDescriptor cd);

	/**
	 * Return a name for the ConfigSet for a core
	 *
	 * @param cd
	 *            the core's CoreDescriptor
	 * @return a name for the core's ConfigSet
	 */
	public abstract String configName(CoreDescriptor cd);

	/**
	 * The default ConfigSetService.
	 *
	 * Loads a ConfigSet defined by the core's configSet property, looking for a
	 * directory named for the configSet property value underneath a base
	 * directory. If no configSet property is set, loads the ConfigSet instead
	 * from the core's instance directory.
	 */
	public static class Default extends ConfigSetService {

		private final Path configSetBase;

		/**
		 * Create a new ConfigSetService.Default
		 *
		 * @param loader
		 *            the CoreContainer's resource loader
		 * @param configSetBase
		 *            the base directory under which to look for config set
		 *            directories
		 */
		public Default(SolrResourceLoader loader, Path configSetBase) {
			super(loader);
			this.configSetBase = configSetBase;
		}

		@Override
		public SolrResourceLoader createCoreResourceLoader(CoreDescriptor cd) {
			Path instanceDir = locateInstanceDir(cd);
			return new SolrResourceLoader(instanceDir, parentLoader.getClassLoader(), cd.getSubstitutableProperties());
		}

		@Override
		public String configName(CoreDescriptor cd) {
			return (cd.getConfigSet() == null ? "instancedir " : "configset ") + locateInstanceDir(cd);
		}

		protected Path locateInstanceDir(CoreDescriptor cd) {
			String configSet = cd.getConfigSet();
			if (configSet == null)
				return cd.getInstanceDir();
			Path configSetDirectory = configSetBase.resolve(configSet);
			if (!Files.isDirectory(configSetDirectory))
				throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
						"Could not load configuration from directory " + configSetDirectory);
			return configSetDirectory;
		}
	}

	/**
	 * A ConfigSetService that shares schema objects between cores
	 */
	public static class SchemaCaching extends Default {

		private static final Logger logger = LoggerFactory.getLogger(SchemaCaching.class);

		private final Cache<String, IndexSchema> schemaCache = CacheBuilder.newBuilder().build();

		public SchemaCaching(SolrResourceLoader loader, Path configSetBase) {
			super(loader, configSetBase);
		}

		public static final DateTimeFormatter cacheKeyFormatter = DateTimeFormat.forPattern("yyyyMMddHHmmss");

		public static String cacheName(File schemaFile) {
			return String.format(Locale.ROOT, "%s:%s", schemaFile.getAbsolutePath(),
					cacheKeyFormatter.print(schemaFile.lastModified()));
		}

		@Override
		public IndexSchema createIndexSchema(final CoreDescriptor cd, final SolrConfig solrConfig) {
			final String resourceNameToBeUsed = IndexSchemaFactory.getResourceNameToBeUsed(cd.getSchemaName(),
					solrConfig);
			File schemaFile = new File(resourceNameToBeUsed);
			if (!schemaFile.isAbsolute()) {
				schemaFile = new File(solrConfig.getResourceLoader().getConfigDir(), schemaFile.getPath());
			}
			if (schemaFile.exists()) {
				try {
					return schemaCache.get(cacheName(schemaFile), new Callable<IndexSchema>() {

						@Override
						public IndexSchema call() throws Exception {
							logger.info("Creating new index schema for core {}", cd.getName());
							return IndexSchemaFactory.buildIndexSchema(cd.getSchemaName(), solrConfig);
						}
					});
				} catch (ExecutionException e) {
					throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
							"Error creating index schema for core " + cd.getName(), e);
				}
			}
			return IndexSchemaFactory.buildIndexSchema(cd.getSchemaName(), solrConfig);
		}
	}
}
