package org.lahab.clucene.server.utils;

/*
 * #%L
 * server
 * %%
 * Copyright (C) 2012 NTNU
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.Map;

import com.microsoft.windowsazure.services.blob.client.CloudBlobContainer;
import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;
import com.microsoft.windowsazure.services.core.storage.StorageException;

/**
 * Encapsulates the credentials and connection creation
 * It contains a map of every accessible container
 * @author charlymolter
 *
 */
public class CloudStorage {
	public Parametizer _params;
	protected CloudStorageAccount _account = null;
	protected Map<String, CloudBlobContainer> _containers = new HashMap<String, CloudBlobContainer>();
	
	private static Map<String, Object> DEFAULTS = new HashMap<String, Object>();
	static {
		DEFAULTS.put("defaultEndpointsProtocol", null);
		DEFAULTS.put("accountName", null);
		DEFAULTS.put("accountKey", null);
	}
	
	/**
	 * Opens a connection to the cloudStorage service
	 * @param configuration
	 * @throws Exception
	 */
	public CloudStorage(Configuration configuration) throws Exception {
		_params = new Parametizer(DEFAULTS, configuration);
		initAccount();
	}

	/**
	 * Open and add a container to the list of current containers
	 * @param key
	 * @param container
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	public void addContainer(String key, String container) throws URISyntaxException, StorageException {
		CloudBlobContainer cont = _account.createCloudBlobClient().getContainerReference(container);
		_containers.put(key, cont);
	}

	public CloudStorageAccount getAccount() {
		return _account;
	}

	/**
	 * Returns the container identified by key
	 * @param key
	 * @return
	 */
	public CloudBlobContainer getContainer(String key) {
		return _containers.get(key);
	}
	
	protected void initAccount() throws InvalidKeyException, URISyntaxException, ParametizerException {
		_account = CloudStorageAccount.parse("DefaultEndpointsProtocol="+ 
					 _params.getString("defaultEndpointsProtocol") +
					 ";AccountName=" +
					 _params.getString("accountName") +
					 ";AccountKey=" +
					 _params.getString("accountKey") + ";");
	}

}
