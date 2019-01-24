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
package com.qlangtech.tis.common.protocol;

import java.util.Collection;
import java.util.List;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import com.qlangtech.tis.common.TerminatorServiceException;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public interface TerminatorService {

    public QueryResponse query(TerminatorQueryRequest query) throws TerminatorServiceException;

    public UpdateResponse add(Collection<SolrInputDocument> docs) throws TerminatorServiceException;

    public UpdateResponse add(SolrInputDocument doc) throws TerminatorServiceException;

    public UpdateResponse commit() throws TerminatorServiceException;

    public UpdateResponse optimize() throws TerminatorServiceException;

    public UpdateResponse commit(boolean waitFlush, boolean waitSearcher) throws TerminatorServiceException;

    public UpdateResponse optimize(boolean waitFlush, boolean waitSearcher) throws TerminatorServiceException;

    public UpdateResponse optimize(boolean waitFlush, boolean waitSearcher, int maxSegments) throws TerminatorServiceException;

    public UpdateResponse rollback() throws TerminatorServiceException;

    public UpdateResponse deleteById(String id) throws TerminatorServiceException;

    public UpdateResponse deleteById(List<String> ids) throws TerminatorServiceException;

    public UpdateResponse deleteByQuery(String query) throws TerminatorServiceException;

    public SolrPingResponse ping() throws TerminatorServiceException;
}
