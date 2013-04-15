/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jdavisonc.camel.gdrive;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.ScheduledBatchPollingConsumer;
import org.apache.camel.spi.Synchronization;
import org.apache.camel.util.CastUtils;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.URISupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files.List;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

/**
 * The Google Drive consumer.
 *
 * @author Jorge Davison (jdavisonc)
 */
public class GDriveConsumer extends ScheduledBatchPollingConsumer {

	private static final transient Logger LOG = LoggerFactory.getLogger(GDriveConsumer.class);
	
    public GDriveConsumer(GDriveEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
    }

    @Override
    protected int poll() throws Exception {
    	
    	LOG.trace("Quering objects in drive...");
    	List listMethod = getGDriveClient().files().list();
    	listMethod.setMaxResults(getMaxMessagesPerPoll());
    	FileList fileList = listMethod.execute();
    	
    	LOG.trace("Found {} objects in drive...", fileList.getItems().size());
    	
    	Queue<Exchange> exchanges = createExchanges(fileList.getItems());
    	return processBatch(CastUtils.cast(exchanges));
    }
    
    protected Queue<Exchange> createExchanges(java.util.List<File> fileList) throws Exception {
        LOG.trace("Received {} messages in this poll", fileList.size());
        
        Queue<Exchange> answer = new LinkedList<Exchange>();
        for (File file : fileList) {
            Exchange exchange = getEndpoint().createExchange(file);
            answer.add(exchange);
        }

        return answer;
    }

	@Override
	public int processBatch(Queue<Object> exchanges) throws Exception {
        int total = exchanges.size();

        for (int index = 0; index < total && isBatchAllowed(); index++) {
        	// only loop if we are started (allowed to run)
            Exchange exchange = ObjectHelper.cast(Exchange.class, exchanges.poll());
            // add current index and total as properties
            exchange.setProperty(Exchange.BATCH_INDEX, index);
            exchange.setProperty(Exchange.BATCH_SIZE, total);
            exchange.setProperty(Exchange.BATCH_COMPLETE, index == total - 1);

            // update pending number of exchanges
            pendingExchanges = total - index - 1;

            // add on completion to handle after work when the exchange is done
            exchange.addOnCompletion(new Synchronization() {
                @Override
				public void onComplete(Exchange exchange) {
                    processCommit(exchange);
                }

                @Override
				public void onFailure(Exchange exchange) {
                    processRollback(exchange);
                }

                @Override
                public String toString() {
                    return "GDriveConsumerOnCompletion";
                }
            });
            LOG.trace("Processing exchange [{}]...", exchange);
            getProcessor().process(exchange);
        }
        return total;
	}
    
    /**
     * Strategy to delete the message after being processed.
     *
     * @param exchange the exchange
     */
    protected void processCommit(Exchange exchange) {
        try {
            if (getConfiguration().isDeleteAfterRead()) {
                String fileId = exchange.getIn().getHeader(GDriveConstants.FILE_ID, String.class);
                LOG.trace("Deleting file with id {}...", fileId);

                getGDriveClient().files().delete(fileId).execute();
                LOG.trace("File deleted");
            }
        } catch (IOException e) {
            LOG.warn("Error occurred during deleting file", e);
            exchange.setException(e);
        }
    }

    /**
     * Strategy when processing the exchange failed.
     *
     * @param exchange the exchange
     */
    protected void processRollback(Exchange exchange) {
        Exception cause = exchange.getException();
        if (cause != null) {
            LOG.warn("Exchange failed, so rolling back message status: " + exchange, cause);
        } else {
            LOG.warn("Exchange failed, so rolling back message status: {}", exchange);
        }
    }

    protected GDriveConfiguration getConfiguration() {
        return getEndpoint().getConfiguration();
    }
    
    protected Drive getGDriveClient() {
        return getEndpoint().getGDriveClient();
    }

    @Override
    public String toString() {
        return "GDriveConsumer[" + URISupport.sanitizeUri(getEndpoint().getEndpointUri()) + "]";
    }

    @Override
    public GDriveEndpoint getEndpoint() {
        return (GDriveEndpoint) super.getEndpoint();
    }
    
}
