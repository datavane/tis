/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.offline.pojo;

import java.util.List;

/**
 * git仓库两个版本的diff
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class GitCommitVersionDiff {

    private GitRepositoryCommitPojo commit;

    private List<GitRepositoryCommitPojo> commits;

    private List<GitFileDiff> diffs;

    private boolean compareTimeout;

    private boolean compareSameRef;

    public GitCommitVersionDiff() {
    }

    public GitRepositoryCommitPojo getCommit() {
        return commit;
    }

    public void setCommit(GitRepositoryCommitPojo commit) {
        this.commit = commit;
    }

    public List<GitRepositoryCommitPojo> getCommits() {
        return commits;
    }

    public void setCommits(List<GitRepositoryCommitPojo> commits) {
        this.commits = commits;
    }

    public List<GitFileDiff> getDiffs() {
        return diffs;
    }

    public void setDiffs(List<GitFileDiff> diffs) {
        this.diffs = diffs;
    }

    public boolean isCompareTimeout() {
        return compareTimeout;
    }

    public void setCompareTimeout(boolean compareTimeout) {
        this.compareTimeout = compareTimeout;
    }

    public boolean isCompareSameRef() {
        return compareSameRef;
    }

    public void setCompareSameRef(boolean compareSameRef) {
        this.compareSameRef = compareSameRef;
    }
}
